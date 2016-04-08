/**
 * 
 */
package jazmin.driver.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.CachePolicy;
import jazmin.misc.InfoBuilder;
import jazmin.server.file.FileClient;
import jazmin.server.file.FileDownloadHandler;
import jazmin.util.RandomUtil;

/**
 * @author yama
 *
 */
public class FileServerDriver extends Driver{
	private static Logger logger=LoggerFactory.get(FileServerDriver.class);
	private FileClient client;
	private CachePolicy cachePolicy;
	private File homeDir;
	private List<ServerInfo>serverList;
	private Map<String,ServerInfo>serverMap;
	private int weightArray[];
	//
	static class ServerInfo{
		String id;
		String host;
		int port;
		int weight;
	}
	//
	public FileServerDriver() {
		cachePolicy=new CachePolicy();
		client=new FileClient();
		serverList=new ArrayList<FileServerDriver.ServerInfo>();
		serverMap=new HashMap<String, FileServerDriver.ServerInfo>();
	}
	//
	public void addServer(String id,String host,int port,int weight){
		ServerInfo si=new ServerInfo();
		si.id=id;
		si.host=host;
		si.port=port;
		si.weight=weight;
		for(ServerInfo s:serverList){
			if(s.id.equalsIgnoreCase(id)){
				throw new IllegalArgumentException("server "+id+" already exists");
			}
		}
		serverList.add(si);
		serverMap.put(si.id, si);
		weightArray=new int[serverList.size()];
		for(int i=0;i<serverList.size();i++){
			weightArray[i]=serverList.get(i).weight;
		}
	}
	
	//
	public void setHomeDir(String path){
		homeDir=new File(path);
		if(!homeDir.exists()){
			homeDir.mkdirs();
		}else{
			if(!homeDir.isDirectory()){
				throw new IllegalArgumentException("home dir:"+path+" should be directory");
			}
		}
	}
	//
	public String getHomeDir(){
		if(homeDir==null){
			return null;
		}
		return homeDir.getAbsolutePath();
	}
	/**
	 * @param type
	 * @param ttlInSeconds
	 * @see jazmin.server.cdn.CachePolicy#addPolicy(java.lang.String, int)
	 */
	public void addCachePolicy(String type, int ttlInSeconds) {
		cachePolicy.addPolicy(type, ttlInSeconds);
	}
	/**
	 * @return
	 * @see jazmin.server.cdn.CachePolicy#getPolicyMap()
	 */
	public Map<String, Long> getCachePolicyMap() {
		return cachePolicy.getPolicyMap();
	}
	//
	public void setDefaultCacheTTL(long ttl){
		cachePolicy.setDefaultTTL(ttl);;
	}
	//
	public long getDefaultCacheTTL(){
		return cachePolicy.getDefaultTTL();
	}
	//
	public void startCheckCache(){
		Jazmin.scheduleAtFixedRate(this::checkCachePolicy,
				10,
				30, 
				TimeUnit.MINUTES);
	}
	//
	private void checkCachePolicy(){
		logger.info("clean expires file in {}",homeDir);
		cachePolicy.cleanFile(homeDir);
	}
	//--------------------------------------------------------------------------
	/**
	 * upload file to server
	 */
	public String upload(File file) throws FileUploadException{
		int choice=RandomUtil.randomIntArray(weightArray);
		ServerInfo si=serverList.get(choice);
		try {
			String realFileId=client.upload("http://"+si.host+":"+si.port+"/upload/", file);
			String fileId= si.id+"_"+realFileId;
			FileServerPair fsp=getFileServerPair(fileId);
			cachePolicy.moveTo(file,fsp.file);
			return fileId;
		} catch (Exception e) {
			throw new FileUploadException(e);
		}
	}
	/**
	 * get local file
	 */
	public File getFile(String fileId){
		FileServerPair fsp=getFileServerPair(fileId);
		if(fsp.file.exists()){
			return fsp.file;
		}
		return null;	
	}
	/**
	 * download file from server
	 * @param fileId
	 * @param handler
	 */
	public void downloadFile(String fileId,FileDownloadHandler handler){
		FileServerPair fsp=getFileServerPair(fileId);
		client.download("http://"+fsp.serverInfo.host+":"+
				fsp.serverInfo.port+"/download/"+fsp.readId, fsp.file, handler);
	}
	//
	static class FileServerPair{
		ServerInfo serverInfo;
		File file;
		String readId;
	}
	//
	private FileServerPair getFileServerPair(String fileId){
		int idxOf_=fileId.indexOf('_');
		if(idxOf_==-1){
			throw new IllegalArgumentException("bad fileId :"+fileId+",can not find serverId");
		}
		String serverId=fileId.substring(0,idxOf_);
		String realId=fileId.substring(idxOf_+1);
		ServerInfo si=serverMap.get(serverId);
		if(si==null){
			throw new IllegalArgumentException("can not find server:"+serverId);
		}
		File serverDir=new File(homeDir,serverId);
		File secondDir=new File(serverDir,realId.charAt(0)+"");
		File file=new File(secondDir,realId);
		FileServerPair fsp=new FileServerPair();
		fsp.file=file;
		fsp.serverInfo=si;
		fsp.readId=realId;
		return fsp;
	}
	//
	//--------------------------------------------------------------------------
	@Override
	public void start() throws Exception {
		if(homeDir==null){
			throw new IllegalArgumentException("home dir can not be null");
		}
		if(serverList.isEmpty()){
			throw new IllegalArgumentException("server list is empty");
		}
		startCheckCache();
	}
	//
	@Override
	public void stop() throws Exception {
		client.stop();
	}
	//
	public String info() {
		InfoBuilder ib=InfoBuilder.create();
		ib.section("info")
		.format("%-30s:%-30s\n")
		.print("homeDir",getHomeDir())
		.print("defaultCacheTTL",getDefaultCacheTTL());
		for(ServerInfo si:serverList){
			ib.print("server-"+si.id,si.host+":"+si.port+"#"+si.weight);
		}
		
		for(Entry<String,Long> e:getCachePolicyMap().entrySet()){
			ib.print("cachePolicy-"+e.getKey(),e.getValue());
		}
		//
		
		return ib.toString();
	}
}
