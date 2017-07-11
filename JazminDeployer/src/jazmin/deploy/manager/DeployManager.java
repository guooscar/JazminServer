/**
 * 
 */
package jazmin.deploy.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.tmatesoft.svn.core.SVNException;

import jazmin.core.Jazmin;
import jazmin.core.job.CronExpression;
import jazmin.deploy.DeployStartServlet;
import jazmin.deploy.domain.AppPackage;
import jazmin.deploy.domain.Application;
import jazmin.deploy.domain.GraphVizRenderer;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.JavaScriptSource;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.domain.MachineJob;
import jazmin.deploy.domain.OutputListener;
import jazmin.deploy.domain.PackageDownloadInfo;
import jazmin.deploy.domain.RepoItem;
import jazmin.deploy.domain.TopSearch;
import jazmin.deploy.domain.User;
import jazmin.deploy.domain.ant.AntManager;
import jazmin.deploy.domain.svn.WorkingCopy;
import jazmin.deploy.manager.RobotDeployManagerContext.RobotDeployManagerContextImpl;
import jazmin.deploy.util.DateUtil;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.web.WebServer;
import jazmin.server.websockify.HostInfoProvider.HostInfo;
import jazmin.server.websockify.WebsockifyServer;
import jazmin.server.webssh.ConnectionInfoProvider.ConnectionInfo;
import jazmin.server.webssh.JavaScriptChannelRobot;
import jazmin.server.webssh.OutputStreamEndpoint;
import jazmin.server.webssh.SendCommandChannelRobot;
import jazmin.server.webssh.WebSshChannel;
import jazmin.server.webssh.WebSshServer;
import jazmin.util.BeanUtil;
import jazmin.util.FileUtil;
import jazmin.util.IOUtil;
import jazmin.util.JSONUtil;
import jazmin.util.JSONUtil.JSONPropertyFilter;
import jazmin.util.SshUtil;

/**
 * @author yama
 * 6 Jan, 2015
 */
public class DeployManager {
	private static Logger logger=LoggerFactory.get(DeployManager.class);
	//
	private static Map<String,Instance>instanceMap;
	private static Map<String,User>userMap;
	private static Map<String,Machine>machineMap;
	private static Map<String,AppPackage>packageMap;
	private static Map<String,Application>applicationMap;
	private static Map<String,MachineJob>jobMap;
	private static List<PackageDownloadInfo>downloadInfos;
	private static GraphVizRenderer graphVizRenderer;
	private static StringBuffer errorMessage;
	private static Map<String, ConnectionInfo>oneTimeSSHConnectionMap=new ConcurrentHashMap<>();
	private static Map<String, HostInfo>oneTimeVncHostInfoMap=new ConcurrentHashMap<>();
	//
	private static Map<String, BenchmarkSession>benchmarkSessions=new ConcurrentHashMap<>();
	//
	static{
		instanceMap=new ConcurrentHashMap<String, Instance>();
		machineMap=new ConcurrentHashMap<String, Machine>();
		packageMap=new ConcurrentHashMap<String, AppPackage>();
		applicationMap=new ConcurrentHashMap<String, Application>();
		userMap=new ConcurrentHashMap<String, User>();
		jobMap=new ConcurrentHashMap<String, MachineJob>();
		downloadInfos=Collections.synchronizedList(new LinkedList<PackageDownloadInfo>());
		graphVizRenderer=new GraphVizRenderer();
	}
	//
	public static String workSpaceDir="";
	public static String deployHostname="";
	public static String repoPath="";
	public static String antPath="";
	public static String antCommonLibPath="";
	//
	public static void setup() throws Exception {
		workSpaceDir=Jazmin.environment.getString("deploy.workspace","./workspace/");
		deployHostname=Jazmin.environment.getString("deploy.hostname","localhost");
		repoPath=Jazmin.environment.getString("deploy.repo.dir","./repo");
		antPath=Jazmin.environment.getString("deploy.ant","ant");
		antCommonLibPath=Jazmin.environment.getString("deploy.ant.lib","./lib");
		
		checkWorkspace();
		Velocity.init();
		//
		WebSshServer webSshServer=Jazmin.getServer(WebSshServer.class);
		if(webSshServer!=null){
			webSshServer.setConnectionInfoProvider(DeployManager::getOneTimeHostInfo);
		}
		WebsockifyServer websockifyServer=Jazmin.getServer(WebsockifyServer.class);
		if(websockifyServer!=null){
			websockifyServer.setHostInfoProvider(DeployManager::getOneTimeVncInfo);
		}
		//
		startJobCronThread();
	}
	//
	private static void startJobCronThread(){
		Thread jobThead=new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					checkJob();
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						logger.catching(e);
					}
				}
			}
		});
		jobThead.setName("DeployManager-CronJob");
		jobThead.start();
	}
	//
	private static void checkJob(){
		jobMap.values().forEach((job)->{
			try {
				if(job.lastRunTime==null){
					job.lastRunTime=new Date();
				}
				CronExpression ce=new CronExpression(job.cron);
				Date nextRunTime=ce.getNextValidTimeAfter(job.lastRunTime);
				job.nextRunTime=nextRunTime;
				Date now=new Date();
				if(nextRunTime!=null&&nextRunTime.before(now)){
					runJob(job,null);
				}
			} catch (Exception e) {
				logger.error(e);
				job.errorMessage=e.getMessage();
			}
			
		});
	}
	//
	private static class FileOutputStreamWrapper extends FileOutputStream{
		OutputListener ol;
		public FileOutputStreamWrapper(String path,OutputListener ol) throws FileNotFoundException {
			super(path);
			this.ol=ol;
		}
		@Override
		public void write(byte[] b) throws IOException {
			super.write(b);
			if(ol!=null){
				ol.onOutput(new String(b));
			}
		}
	}
	//
	public static BenchmarkSession addBenchmarkSession(){
		List<String>finishedSessions=new ArrayList<>();
		benchmarkSessions.forEach((k,s)->{
			if(s.finished){
				finishedSessions.add(k);
			}
		});
		finishedSessions.forEach(s->benchmarkSessions.remove(s));
		BenchmarkSession session=new BenchmarkSession();
		session.id=UUID.randomUUID().toString();
		benchmarkSessions.put(session.id, session);		
		return session;
	}
	//
	public static BenchmarkSession getBenchmarkSession(String id){
		return benchmarkSessions.get(id);
	}
	//
	public static List<String> getJobLogNames(String jobId){
		List<String>result=new ArrayList<>();
		File dir=new File(workSpaceDir+"joblog/"+jobId);
		for(File f:dir.listFiles(f->f.getName().endsWith("log"))){
			result.add(f.getName());
		}
		return result;
	}
	//
	public static void deleteJobLog(String jobId, String name) {
		File file=new File(workSpaceDir+"joblog/"+jobId,name);
		file.delete();	
	}
	//
	public static String getJobLog(String jobId,String logName) throws IOException{
		File file=new File(workSpaceDir+"joblog/"+jobId,logName);
		return FileUtil.getContent(file);
	}
	//
	public static void runJob(MachineJob job,OutputListener ol)throws Exception{
		job.runTimes++;
		job.lastRunTime=new Date();
		WebSshServer webSshServer=Jazmin.getServer(WebSshServer.class);
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
		File outputFile=new File(workSpaceDir+"joblog/"+job.id,job.id+"_"+sdf.format(new Date())+".log");
		if(!outputFile.getParentFile().exists()){
			outputFile.getParentFile().mkdirs();
		}
		FileOutputStreamWrapper fos=new FileOutputStreamWrapper(outputFile.getAbsolutePath(),ol);
		OutputStreamEndpoint fe=new OutputStreamEndpoint(fos);
		WebSshChannel channel=new WebSshChannel(webSshServer);
		channel.endpoint=fe;
		Machine machine=machineMap.get(job.machine);
		if(machine==null){
			throw new IllegalArgumentException("can not find machine:"+job.machine);
		}
		if(machine.sshPort==0||
				machine.sshUser==null||
				machine.sshUser.isEmpty()){
			throw new IllegalArgumentException("ssh info not set");
		}
		ConnectionInfo info=new ConnectionInfo();
		info.name=machine.id;
		info.host=machine.publicHost;
		info.port=machine.sshPort;
		info.user=job.root?"root":machine.sshUser;
		info.password=job.root?machine.rootSshPassword:machine.sshPassword;
		Map<String,Object>context=new HashMap<>();
		context.put("deployer", new RobotDeployManagerContextImpl(machine));
		info.channelListener=new JavaScriptChannelRobot(getRobotScriptRunContent(job.robot),context);
		channel.setConnectionInfo(info);
		webSshServer.addChannel(channel);
		channel.startShell();
	}
	//
	public static String createOneVncToken(Machine machine){
		if(machine.vncPort==0){
			throw new IllegalArgumentException("vnc info not set");
		}
		HostInfo info=new HostInfo();
		info.host=machine.publicHost;
		info.port=machine.vncPort;
		String uuid=UUID.randomUUID().toString();
		oneTimeVncHostInfoMap.put(uuid,info);
		return uuid;
	}
	//
	public static HostInfo getOneTimeVncInfo(String token){
		HostInfo info=oneTimeVncHostInfoMap.get(token);
		oneTimeVncHostInfoMap.remove(token);
		return info;
	}
	//
	public static String createOneTimeSSHToken(
			Machine machine,
			boolean root,
			boolean enableInput,
			String cmd){
		if(machine.sshPort==0||
				machine.sshUser==null||
				machine.sshUser.isEmpty()){
			throw new IllegalArgumentException("ssh info not set");
		}
		ConnectionInfo info=new ConnectionInfo();
		info.name=machine.id;
		info.host=machine.publicHost;
		info.port=machine.sshPort;
		info.user=root?"root":machine.sshUser;
		info.password=root?machine.rootSshPassword:machine.sshPassword;
		info.enableInput=enableInput;
		if(cmd!=null){
			info.channelListener=new SendCommandChannelRobot(cmd);
		}
		String uuid=UUID.randomUUID().toString();
		oneTimeSSHConnectionMap.put(uuid,info);
		return uuid;
	}
	//
	public static String createOneTimeRobotToken(
			Machine machine,
			boolean root,
			String robot)throws Exception{
		if(machine.sshPort==0||
				machine.sshUser==null||
				machine.sshUser.isEmpty()){
			throw new IllegalArgumentException("ssh info not set");
		}
		ConnectionInfo info=new ConnectionInfo();
		info.name=machine.id;
		info.host=machine.publicHost;
		info.port=machine.sshPort;
		info.user=root?"root":machine.sshUser;
		info.password=root?machine.rootSshPassword:machine.sshPassword;
		Map<String,Object>context=new HashMap<>();
		context.put("deployer", new RobotDeployManagerContextImpl(machine));
		info.channelListener=new JavaScriptChannelRobot(getRobotScriptRunContent(robot),context);
		String uuid=UUID.randomUUID().toString();
		oneTimeSSHConnectionMap.put(uuid,info);
		return uuid;
	}
	//
	public static ConnectionInfo getOneTimeHostInfo(String token){
		ConnectionInfo info=oneTimeSSHConnectionMap.get(token);
		oneTimeSSHConnectionMap.remove(token);
		return info;
	}
	//
	public static String getErrorMessage(){
		if(errorMessage==null){
			return "";
		}
		return errorMessage.toString();
	}
	//
	public static void reload(){
		String configDir=workSpaceDir;
		configDir+="config";
		try{
			errorMessage=new StringBuffer();
			reloadUserConfig(configDir);
			reloadApplicationConfig(configDir);
			checkApplicationConfig();
			reloadMachineConfig(configDir);
			reloadInstanceConfig(configDir);
			reloadJobConfig(configDir);
			setInstancePrioriy();
			reloadPackage();
		}catch(Exception e){
			logErrorMessage(e.getMessage());
			logger.error(e.getMessage(),e);
		}
	}
	//
	private static void logErrorMessage(String msg){
		errorMessage.append(msg+"\n");
	}
	//
	public static String getConfigFile(String file){
		String configDir=workSpaceDir+"config";
		try {
			return FileUtil.getContent(new File(configDir,file));
		} catch (IOException e) {
			logger.catching(e);
			return null;
		}
	}
	//
	public static List<JavaScriptSource>getRobotScripts(){
		return getScripts0("script");
	}
	//
	public static List<JavaScriptSource>getBenchmarkScripts(){
		return getScripts0("benchmark");
	}
	//
	private static List<JavaScriptSource>getScripts0(String dirName){
		String configDir=workSpaceDir+dirName;
		File dir=new File(configDir);
		List<JavaScriptSource>result=new ArrayList<JavaScriptSource>();
		for(File f:dir.listFiles()){
			if(f.isFile()){
				JavaScriptSource s=new JavaScriptSource();
				s.name=f.getName();
				s.lastModifiedTime=new Date(f.lastModified());
				result.add(s);
			}
		}
		return result;
	}
	public static void deleteBenchmarkScript(String name){
		File scriptFile=new File(workSpaceDir+"benchmark/"+name);
		scriptFile.delete();
	}
	//
	public static void deleteScript(String name){
		File scriptFile=new File(workSpaceDir+"script/"+name);
		scriptFile.delete();
	}
	//
	public static boolean existsRobotScript(String name){
		File scriptFile=new File(workSpaceDir+"script/"+name);
		return scriptFile.exists();
	}
	//
	public static String getRobotScriptContent(String name) throws IOException{
		File scriptFile=new File(workSpaceDir+"script/"+name);
		return FileUtil.getContent(scriptFile);
	}
	public static String getBenchmarkScriptContent(String name) throws IOException{
		File scriptFile=new File(workSpaceDir+"benchmark/"+name);
		return FileUtil.getContent(scriptFile);
	}
	//
	public static String getRobotScriptRunContent(String name)throws IOException{
		String fileContent=getRobotScriptContent(name);
		StringBuilder result=new StringBuilder();
		StringReader sr=new StringReader(fileContent);
		BufferedReader br=new BufferedReader(sr);
		String line=null;
		while((line=br.readLine())!=null){
			line=line.trim();
			if(line.startsWith("//include")){
				String includeName=line.substring(10);
				includeName=includeName.trim();
				result.append("\n"+getRobotScriptContent(includeName)+"\n");
			}
		}
		result.append(fileContent);
		return result.toString();
	}
	//
	public static JavaScriptSource getRobotScript(String name) throws IOException{
		return getScript0("script",name);
	}
	//
	public static JavaScriptSource getBenchmarkScript(String name) throws IOException{
		return getScript0("benchmark",name);
	}
	public static JavaScriptSource getScript0(String dirName,String name) throws IOException{
		String configDir=workSpaceDir+dirName;
		File dir=new File(configDir);
		for(File f:dir.listFiles()){
			if(f.isFile()){
				if(f.getName().equals(name)){
					JavaScriptSource s=new JavaScriptSource();
					s.name=f.getName();
					s.lastModifiedTime=new Date(f.lastModified());
					return s;
				};
			}
		}
		return null;
	}
	//
	public static void saveRobotScript(String name,String content) throws IOException{
		saveScript0("script",name,content);
	}
	//
	public static void saveBenhmarkScript(String name,String content) throws IOException{
		saveScript0("benchmark",name,content);
	}
	//
	private static void saveScript0(String dir,String name,String content) throws IOException{
		File scriptFile=new File(workSpaceDir+dir+"/"+name);
		if(!scriptFile.exists()){
			scriptFile.createNewFile();
		}
		FileUtil.saveContent(content, scriptFile);
	}
	//
	public static void saveConfigFile(String file,String value){
		String configDir=workSpaceDir+"config";
		try {
			//save to log/BACK_UP_PATH/ first for backup
			File f=new File(configDir,file);
			backupConfigFile(f);
			FileUtil.saveContent(value,f);
			reload();
		} catch (IOException e) {
			logger.catching(e);
		}
	}
	//
	private static void backupConfigFile(File file){
		try {
			Date now=new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String date = sdf.format(now);
			File dir=new File(workSpaceDir+"backup",date);
			if(!dir.exists()){
				dir.mkdirs();
			}
			//
			sdf = new SimpleDateFormat("yyyyMMddhhmmss");
			String dateTime = sdf.format(new Date());
			File backupFile=new File(dir.getAbsolutePath(),file.getName()+"-"+dateTime);
			//
			if(!backupFile.exists()){
				backupFile.createNewFile();
			}
			FileUtil.saveContent(FileUtil.getContent(file),backupFile);
			logger.debug("backupConfigFile file:{} backupFile:{}",
					file.getName(),backupFile.getAbsolutePath());
			//
			//delete 7 days ago log
			sdf = new SimpleDateFormat("yyyyMMdd");
			date = sdf.format(DateUtil.getNextDay(-7));
			File folder = new File(workSpaceDir+"backup",date);
			if(folder.exists()){
				FileUtil.deleteDirectory(folder);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}
	//
	public static void setPackageVersion(List<Instance>instances,String version){
		if(version==null||version.trim().isEmpty()){
			return;
		}
		if(!Pattern.matches("\\d\\d*\\.\\d\\d*\\.\\d\\d*",version)){
			throw new IllegalArgumentException("version format just like :1.0.0");
		}
		instances.forEach(i->i.packageVersion=(version));
	}
	//
	public static void setScmTag(List<Instance>instances,String tag){
		if(tag==null||tag.trim().isEmpty()){
			return;
		}
		instances.forEach(i->i.scmTag=(tag));
	}
	//
	public static void saveInstanceConfig()throws Exception{
		String configDir=workSpaceDir;
		configDir+="config";
		File configFile=new File(configDir,"instance.json");
		List<Instance>list=getInstances();
		Collections.sort(list,(o1,o2)->o1.cluster.compareTo(o2.cluster));
		if(configFile.exists()){
			String result=JSONUtil.toJson(
					list,
					new JSONPropertyFilter(){
						@Override
						public boolean apply(Object arg0, String name,
								Object arg2) {
							String t=name.toLowerCase();
							if(t.endsWith("lastAliveTime")||
									t.endsWith("isAlive")||
									t.endsWith("application")||
									t.endsWith("machine")||
									t.endsWith("priority")){
								return false;
							}
							return true;
						}
					},true);
			FileUtil.saveContent(result, configFile);
		}
	}
	//
	private static void reloadPackage(){
		packageMap.clear();
		String packageDir=workSpaceDir;
		packageDir+="package";
		
		File packageFolder=new File(packageDir);
		if(packageFolder.exists()&&packageFolder.isDirectory()){
			for(File ff:packageFolder.listFiles()){
				if(ff.isFile()&&!ff.isHidden()){
					AppPackage pkg=new AppPackage();
					String fileName=ff.getName();
					pkg.id=(fileName);
					pkg.file=(ff.getAbsolutePath());
					pkg.lastModifiedTime=new Date(ff.lastModified());
					packageMap.put(pkg.id,pkg);
				}
			}	
		}
	}
	//
	public static User validate(String u,String p){
		User ui=userMap.get(u);
		if(ui==null){
			return null;
		}
		if(ui.password.equals(p)){
			return ui;
		}
		return null;
	}
	//
	public static User getUser(String uid){
		return userMap.get(uid);
	}
	//
	public static List<PackageDownloadInfo>getPackageDownloadInfos(){
		return downloadInfos;
	}
	//
	public static void addPackageDownloadInfo(PackageDownloadInfo info){
		downloadInfos.add(info);
	}
	//
	public static void removePackageDownloadInfo(PackageDownloadInfo info){
		downloadInfos.remove(info);
	}
	//
	public static List<AppPackage>getPackages(){
		return new ArrayList<AppPackage>(packageMap.values());
	}
	//
	public static List<Application>getApplications(String uid,String search){
		if(search==null||search.trim().isEmpty()){
			return new ArrayList<Application>();
		}
		
		String queryBegin="select * from "+Application.class.getName()+" where";
		User user=getUser(uid);
		String sql=queryBegin;
		if(!uid.equals(User.ADMIN)){
			StringBuilder cc=new StringBuilder();
			for(String q:user.applicationSystems){
				cc.append("'"+q+"',");
			}
			if(cc.length()>0){
				cc.deleteCharAt(cc.length()-1);
			}
			sql+=" 1=1 and system in("+cc+") and ";
		}else{
			sql+=" 1=1 and ";
		}
		sql+=search;
		return BeanUtil.query(getApplications(),sql);
	}
	//
	//
	public static List<MachineJob>getJobs(String search){
		if(search==null||search.trim().isEmpty()){
			return new ArrayList<MachineJob>();
		}
		
		String queryBegin="select * from "+MachineJob.class.getName()+" where";
		String sql=queryBegin;
		sql+=" 1=1 and ";
		sql+=search;
		return BeanUtil.query(getJobs(),sql);
	}
	//
	public static List<MachineJob>getJobs(){
		return new ArrayList<MachineJob>(jobMap.values());
	}
	//
	public static List<Application>getApplications(){
		return new ArrayList<Application>(applicationMap.values());
	}
	//
	public static List<Application>getApplicationBySystem(String system){
		List<Application>result=new ArrayList<Application>();
		for(Application a:applicationMap.values()){
			if(a.system.equals(system)){
				result.add(a);
			}
		}
		return result;
	}
	//
	public static List<Application>getApplicationByPrefix(String prefix){
		List<Application>result=new ArrayList<Application>();
		for(Application a:applicationMap.values()){
			if(a.id.toLowerCase().startsWith(prefix.toLowerCase())){
				result.add(a);
			}
		}
		return result;
	}
	//
	public static Application getApplicationById(String id){
		for(Application a:applicationMap.values()){
			if(a.id.equalsIgnoreCase(id)){
				return a;
			}
		}
		return null;
	}
	//
	//
	public static List<Instance>getInstanceByPrefix(String prefix){
		List<Instance>result=new ArrayList<Instance>();
		for(Instance a:instanceMap.values()){
			if(a.id.toLowerCase().startsWith(prefix.toLowerCase())){
				result.add(a);
			}
		}
		return result;
	}
	//
	public static Instance getInstanceById(String id){
		for(Instance a:instanceMap.values()){
			if(a.id.equalsIgnoreCase(id)){
				return a;
			}
		}
		return null;
	}
	//
	public static List<AppPackage>getPackages(String search)throws Exception{
		if(search==null||search.trim().isEmpty()){
			return new ArrayList<AppPackage>();
		}
		String queryBegin="select * from "+AppPackage.class.getName()+" where 1=1 and ";
		return BeanUtil.query(getPackages(),queryBegin+search);
	}
	//
	//
	public static List<RepoItem>getRepoItems(String search)throws Exception{
		if(search==null||search.trim().isEmpty()){
			return new ArrayList<RepoItem>();
		}
		String queryBegin="select * from "+RepoItem.class.getName()+" where 1=1 and ";
		return BeanUtil.query(getRepoItems(),queryBegin+search);
	}
	//	
	public static List<RepoItem>getRepoItems(){
		List<RepoItem>items= new ArrayList<RepoItem>();
		String repoDir=workSpaceDir+"repo";
		File ff=new File(repoDir);
		if(ff.isDirectory()){
			for(File f:ff.listFiles()){
				if(f.isFile()){
					RepoItem i=new RepoItem();
					i.file=f.getAbsolutePath();
					i.id=f.getName();
					i.lastModifiedTime=new Date(f.lastModified());
					items.add(i);
				}
			}
		}
		return items;
	}
	//
	public static List<Instance>getInstances(String uid,String search)throws Exception{
		if(search==null||search.trim().isEmpty()){
			return new ArrayList<Instance>();
		}
		String queryBegin="select * from "+Instance.class.getName()+" where";
		User user=getUser(uid);
		String sql=queryBegin;
		if(!uid.equals(User.ADMIN)){
			StringBuilder cc=new StringBuilder();
			for(String q:user.instanceClusters){
				cc.append("'"+q+"',");
			}
			if(cc.length()>0){
				cc.deleteCharAt(cc.length()-1);
			}
			sql+=" 1=1 and cluster in("+cc+") and ";
		}else{
			sql+=" 1=1 and ";
		}
		sql+=search;
		return BeanUtil.query(getInstances(),sql);
	}
	//
	public static Instance getInstance(String id){
		return instanceMap.get(id);
	}
	//
	public static List<Machine>getMachines(String uid,String search){
		if(search==null||search.trim().isEmpty()){
			return new ArrayList<Machine>();
		}
		String queryBegin="select * from "+Machine.class.getName()+" where";
		User user=getUser(uid);
		String sql=queryBegin;
		if(!uid.equals(User.ADMIN)){
			StringBuilder cc=new StringBuilder();
			for(String q:user.machines){
				cc.append("'"+q+"',");
			}
			if(cc.length()>0){
				cc.deleteCharAt(cc.length()-1);
			}
			sql+=" 1=1 and id in("+cc+") and ";
		}else{
			sql+=" 1=1 and ";
		}
		sql+=search;
		return BeanUtil.query(getMachines(),sql);
	}
	//
	public static List<Machine>getMachines(){
		return new ArrayList<Machine>(machineMap.values());
	}
	public static Machine getMachine(String id){
		return machineMap.get(id);
	}
	//
	public static List<Instance>getInstances(){
		return new ArrayList<Instance>(instanceMap.values());
	}
	//
	private static void reloadMachineConfig(String configDir)throws Exception{
		File configFile=new File(configDir,"machine.json");
		if(configFile.exists()){
			machineMap.clear();
			logger.info("load config from:"+configFile.getAbsolutePath());
			String ss=FileUtil.getContent(configFile);
			List<Machine>machines= JSONUtil.fromJsonList(ss,Machine.class);
			machines.forEach(in->machineMap.put(in.id,in));
		}else{
			logErrorMessage("can not find :"+configFile);
		}
	}
	private static void reloadApplicationConfig(String configDir)throws Exception{
		File configFile=new File(configDir,"application.json");
		if(configFile.exists()){
			applicationMap.clear();
			logger.info("load application from:"+configFile.getAbsolutePath());
			String ss=FileUtil.getContent(configFile);
			List<Application>apps= JSONUtil.fromJsonList(ss,Application.class);
			logger.info("load total:"+apps.size()+" applications");
			apps.forEach(in->{
				applicationMap.put(in.id,in);
			});
		}else{
			logErrorMessage("can not find :"+configFile);
		}
	}
	//
	private static void reloadUserConfig(String configDir)throws Exception{
		File configFile=new File(configDir,"user.json");
		if(configFile.exists()){
			userMap.clear();
			logger.info("load user from:"+configFile.getAbsolutePath());
			String ss=FileUtil.getContent(configFile);
			List<User>apps= JSONUtil.fromJsonList(ss,User.class);
			apps.forEach(in->{
				logger.debug("add user:"+in.id);
				userMap.put(in.id,in);
			});
		}else{
			logErrorMessage("can not find :"+configFile);
		}
	}
	//
	private static void reloadJobConfig(String configDir)throws Exception{
		File configFile=new File(configDir,"job.json");
		if(configFile.exists()){
			jobMap.clear();
			logger.info("load job from:"+configFile.getAbsolutePath());
			String ss=FileUtil.getContent(configFile);
			List<MachineJob>jobs= JSONUtil.fromJsonList(ss,MachineJob.class);
			jobs.forEach(in->{
				jobMap.put(in.id,in);
			});
		}else{
			logErrorMessage("can not find :"+configFile);
		}
	}
	//
	private static void checkApplicationConfig(){
		for(Application a:applicationMap.values()){
			for(String depend:a.depends){
				if(!applicationMap.containsKey(depend)){
					logErrorMessage("can not find depend application "+depend+" for "+a.id);
				}
				//
				if(depend.equals(a)){
					logErrorMessage("can not depend self "+depend);	
				}
			}
		}
		//do topsearch and cal priority
		int idx=0;
		for(Application a:TopSearch.topSearch(getApplications())){
			logger.debug("application:"+a.id);
			a.priority=idx++;
		}
	}
	
	//
	private static void reloadInstanceConfig(String configDir)throws Exception{
		File configFile=new File(configDir,"instance.json");
		if(configFile.exists()){
			instanceMap.clear();
			logger.info("load config from:"+configFile.getAbsolutePath());
			String ss=FileUtil.getContent(configFile);
			List<Instance>instances= JSONUtil.fromJsonList(ss,Instance.class);
			AtomicInteger ai=new AtomicInteger();
			instances.forEach(in->{
				if(in.user==null){
					in.user=("");
				}
				if(in.password==null){
					in.password=("");
				}
				if(in.packageVersion==null){
					in.packageVersion=("1.0.0");
				}
				in.priority=(ai.incrementAndGet());
				Machine m=machineMap.get(in.machineId);
				if(m==null){
					logErrorMessage("can not find machine "+in.machineId+" for instance "+in.id+"");
				}else{
					in.machine=(m);
				}
				Application app=applicationMap.get(in.appId);
				if(app==null){
					logErrorMessage("can not find application "+in.appId+" for instance "+in.id+"");
				}else{
					in.application=(app);
				}
				instanceMap.put(in.id,in);			
			});
		}else{
			logErrorMessage("can not find :"+configFile);
		}
	}
	//
	private static void setInstancePrioriy(){
		for(Instance i:getInstances()){
			i.priority=i.application.priority;
		}
	}
	//
	public static String renderTemplate(String instanceName){
		Instance instance=instanceMap.get(instanceName);
		if(instance==null){
			return null;
		}
		return renderTemplate(instance);
	}
	//
	//
	public static void saveTemplate(String appId,String value){
		String templateDir=workSpaceDir;
		templateDir+="template";
		File file=new File(templateDir+"/"+appId+".vm");
		try{
			if(!file.exists()){
				file.createNewFile();
			}
			FileUtil.saveContent(value, file);
		}catch(Exception e){
			logger.catching(e);
		}
	}
	//
	public static String getTemplate(String appId){
		String templateDir=workSpaceDir;
		templateDir+="template";
		File file=new File(templateDir+"/"+appId+".vm");
		if(!file.exists()){
			return null;
		}
		try {
			return FileUtil.getContent(file);
		} catch (IOException e) {
			logger.catching(e);
			return null;
		}
	}
	//
	private static String renderTemplate(Instance instance){
		VelocityContext ctx=new VelocityContext();
		WebServer ws=Jazmin.getServer(WebServer.class);
		if(ws!=null){
			ctx.put("deployServerPort",ws.getPort());	
		}
		ctx.put("env",Jazmin.environment.envs());
		ctx.put("instances",getInstances());
		ctx.put("instanceMap",instanceMap);
		ctx.put("machines",getMachines());
		ctx.put("machineMap",machineMap);
		ctx.put("applications",getApplications());
		ctx.put("applicationMap",applicationMap);
		ctx.put("instance", instance);
		//
		Map<String,String>properties=new HashMap<String, String>();
		properties.putAll(instance.properties);
		properties.putAll(instance.application.properties);
		properties.putAll(instance.machine.properties);
		//
		ctx.put("properties", properties);
		
		StringWriter sw=new StringWriter();
		String templateDir=workSpaceDir;
		templateDir+="template";
		
		File file=new File(templateDir+"/"+instance.appId+".vm");
		if(!file.exists()){
			logger.info("can not find {} use Default.vm to render",file);
			file=new File(templateDir+"/Default.vm");
		}
		if(!file.exists()){
			logger.warn("can not find template {}",file);
			return null;
		}
		Velocity.mergeTemplate(file.getPath(),"UTF-8", ctx, sw);
		return sw.toString();
	}
	//
	private static boolean testPort(String host, int port) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), 1000);
			socket.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	//
	public static void testMachine(Machine machine){
		machine.isAlive=(pingHost(machine.publicHost));
	}
	//
	public static String runCmdOnMachine(Machine m,boolean root,String cmd){
		StringBuilder sb=new StringBuilder();
		try{
			SshUtil.execute(
					m.publicHost,
					m.sshPort,
					root?"root":m.sshUser,
					root?m.rootSshPassword:m.sshPassword,
					cmd,
					m.getSshTimeout(),
					(out,err)->{
						sb.append(out+"\n");
						if(err!=null&&!err.isEmpty()){
							sb.append(err+"\n");			
						}
					});
		}catch(Exception e){
			e.printStackTrace();
			sb.append(e.getMessage()+"\n");
		}
		return sb.toString();
	}
	
	//
	private static boolean pingHost(String host){
		try {
			return InetAddress.getByName(host).isReachable(3000);
		} catch (Exception e) {
			return false;
		}  
	}
	//
	//
	public static void testInstance(Instance instance){
		instance.isAlive=(testPort(
					instance.machine.publicHost,
					instance.port));
	}
	//
	private static String exec(Instance instance,boolean root,String cmd)
	throws Exception{
		StringBuilder sb=new StringBuilder();
		try{
			Machine m=instance.machine;
			SshUtil.execute(
					m.publicHost,
					m.sshPort,
					root?"root":m.sshUser,
					root?m.rootSshPassword:m.sshPassword,
					cmd,
					m.getSshTimeout(),
					(out,err)->{
						sb.append(out+"\n");
						if(err!=null&&!err.isEmpty()){
							sb.append(err+"\n");			
						}
					});
		}catch(Exception e){
			logger.catching(e);
			sb.append(e.getMessage()+"\n");
			throw e;
		}
		return sb.toString();
	}
	//
	public static String createInstance(Instance instance)throws Exception{
		StringBuilder sb=new StringBuilder();
		if(instance.application.type.startsWith("jazmin")){
			String instanceDir=instance.machine.jazminHome+"/instance/"+instance.id;
			sb.append(exec(instance,false,
					"mkdir "+instanceDir)+"");
			//
			String hostname=deployHostname;
			String jsFile="jazmin.include('http://"+hostname+"/srv/deploy/boot/'+jazmin.getServerName());";
			jsFile=jsFile.replaceAll("'","\\'").replaceAll("\"","\\\\\"");
			//
			sb.append(exec(instance,false,
					"echo \""+jsFile+"\" > "+instanceDir+"/jazmin.js")+"\n");
		}
		//
		if(instance.application.type.equals(Application.TYPE_HAPROXY)){
			String haproxyHome=instance.machine.haproxyHome;
			String instanceDir=haproxyHome+""+instance.id;
			sb.append(exec(instance,false,
					"mkdir -p "+instanceDir)+"");
		}
		//
		if(instance.application.type.equals(Application.TYPE_MEMCACHED)){
			String memcachedHome=instance.machine.memcachedHome;
			String instanceDir=memcachedHome+""+instance.id;
			sb.append(exec(instance,false,
					"mkdir -p "+instanceDir)+"");
		}
		return sb.toString();
		
	}
	//
	public static String startInstance(Instance instance) throws Exception{
		StringBuilder sb=new StringBuilder();
		if(instance.application.type.startsWith("jazmin")){
			sb.append(exec(instance,false,
					instance.machine.jazminHome
				+"/jazmin startbg "+instance.id));
			return sb.toString();
		}
		if(instance.application.type.equals(Application.TYPE_MEMCACHED)){
			String size=instance.getProperties().getOrDefault(Instance.P_MEMCACHED_SIZE,"64m");
			String memcachedHome=instance.machine.memcachedHome;
			String instanceDir=memcachedHome+"/"+instance.id;
			String pidFile=instanceDir+"/memcached_pid";
			String memcachedCmd="memcached -d -m "+size+" -l 0.0.0.0 -p "
					+instance.port+" -P "+pidFile;
			sb.append(exec(instance,false,
					memcachedCmd));
			return sb.toString();
		}
		if(instance.application.type.equals(Application.TYPE_HAPROXY)){
			String configFile=renderTemplate(instance);
			configFile=configFile.replaceAll("'","\\'").replaceAll("\"","\\\\\"");
			//
			String haproxyHome=instance.machine.haproxyHome;
			String instanceDir=haproxyHome+"/"+instance.id;
			String configPath=instanceDir+"/haproxy_cfg";
			sb.append(exec(instance,false,
					"echo \""+configFile+"\" > "+configPath)+"\n");
			String pidFile=instanceDir+"/haproxy_pid";
			sb.append(exec(instance,true,
					"haproxy -p "+pidFile+" -f "+configPath));
			return sb.toString();
		}
		if(instance.application.type.equals(Application.TYPE_MYSQL)){
			String targetSqlFile="/tmp/"+instance.id+".sql";
			String downloadCommand="wget -N http://"+deployHostname+"/srv/deploy/pkg/"+instance.id+" -O "+targetSqlFile;
			sb.append("target sql file "+targetSqlFile+"\n");
			sb.append(exec(instance,false,downloadCommand));
			String sourceCommand="mysql -u "+instance.user+" -p"+instance.password+" "+instance.id+" <"+targetSqlFile;
			sb.append("source file on instance "+instance.id+"\n");
			sb.append(exec(instance,false,sourceCommand));
			return sb.toString();
		}
		sb.append("not support");
		return sb.toString();
	}
	//
	public static String stopInstance(Instance instance) throws Exception{
		StringBuilder sb=new StringBuilder();
		if(instance.application.type.startsWith("jazmin")){
			sb.append(exec(instance,false,
					instance.machine.jazminHome
					+"/jazmin stop "+instance.id));
			return sb.toString();
		}
		//
		if(instance.application.type.equals(Application.TYPE_MEMCACHED)){
			String memcachedHome=instance.machine.memcachedHome;
			String instanceDir=memcachedHome+"/"+instance.id;
			String pidFile=instanceDir+"/memcached_pid";
			sb.append(exec(instance,false,
					"kill -9 `cat "+pidFile+"`"));
			return sb.toString();
		}
		//
		if(instance.application.type.equals(Application.TYPE_HAPROXY)){
			String haproxyHome=instance.machine.haproxyHome;
			String instanceDir=haproxyHome+"/"+instance.id;
			String pidFile=instanceDir+"/haproxy_pid";
			sb.append(exec(instance,true,
					"kill -9 `cat "+pidFile+"`"));
			return sb.toString();
		}
		
		sb.append("not support");
		return sb.toString();
	}
	/**
	 * 
	 */
	public static AppPackage getInstancePackage(String instanceId) {
		Instance ins=instanceMap.get(instanceId);
		if(ins==null){
			logger.warn("can not find instance {}",instanceId);
			return null;
		}
		String suffex="";
		suffex=".jaz";
		if(ins.application.type.equals(Application.TYPE_JAZMIN_WEB)){
			suffex=".war";
		}
		if(ins.application.type.equals(Application.TYPE_MYSQL)){
			suffex=".sql";
		}
		String packageName=ins.appId+"-"+ins.packageVersion+suffex;
		AppPackage p= packageMap.get(packageName);
		logger.info("return package {} - {}",p,packageName);
		return p;
	}
	//
	public static AppPackage getPackage(String name){
		return packageMap.get(name);
	}
	//
	public static String renderApplicationGraph(String system){
		return graphVizRenderer.renderInstanceGraph(system,"");
	}
	//
	public static String renderInstanceGraph(String system,String cluster){
		return graphVizRenderer.renderInstanceGraph(system, cluster);
	}
	//
	private static void createDirs(String path){
		File workspace=new File(workSpaceDir,path);
		if(!workspace.exists()){
			logger.info("create workspace dir {}",workspace);
			if(!workspace.mkdirs()){
				logger.warn("can not create workspace dir {}",workspace);
				return;
			}
		}
	}
	//
	public static void checkWorkspace(){
		createDirs("config");
		createDirs("template");
		createDirs("repo");
		createDirs("package");
		createDirs("script");
		//
		createFile("config/application.json");
		createFile("config/instance.json");
		createFile("config/machine.json");
		createFile("config/user.json");
		createFile("config/iptables.rule");
		createFile("template/Default.vm");

	}
	//
	private static void createFile(String path){
		try {
			logger.info("create new file {}",path);
			String s=IOUtil.getContent(DeployStartServlet.class.getResourceAsStream("workspace/"+path));
			File configFile=new File(workSpaceDir,path);
			if(!configFile.exists()){
				configFile.createNewFile();
				FileUtil.saveContent(s,configFile);
			}
		} catch (IOException e) {
			logger.catching(e);
		}
	}
	//
	
	public static int compileApp(Application app,OutputListener listener) {
		if(app.scmUser==null){
			return -1;
		}
		File localPath=new File(DeployManager.repoPath,app.id);
		if(!localPath.exists()){
			logger.info("create local path:{}",localPath.getAbsolutePath());
			localPath.mkdirs();
		}
		WorkingCopy wc=new WorkingCopy(
				app.scmUser, 
				app.scmPassword,
				app.scmPath, localPath.getAbsolutePath());
		wc.setOutputListener(listener);
		try {
			wc.cleanup();
			wc.checkout();
			wc.update();
		} catch (SVNException e) {
			logger.catching(e);
			listener.onOutput(e.getMessage());
			return -1;
		}
		//
		if(app.antTarget!=null){
			AntManager antManager=new AntManager(DeployManager.antPath);
			antManager.setOutputListener(listener);
			antManager.setCommonLib(DeployManager.antCommonLibPath);
			File buildFile=new File(localPath, "build.xml");
			try {
				return antManager.antCall(app.antTarget,buildFile.getAbsolutePath());
			} catch (Exception e) {
				logger.catching(e);
				listener.onOutput(e.getMessage());
				return -1;
			}
		}
		return -1;
	}
	
	
}
