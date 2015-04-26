/**						JAZMIN SERVER SOURCE FILE
--------------------------------------------------------------------------------
	     	  ___  _______  _______  __   __  ___   __    _ 		
		     |   ||   _   ||       ||  |_|  ||   | |  |  | |		
		     |   ||  |_|  ||____   ||       ||   | |   |_| |		
		     |   ||       | ____|  ||       ||   | |       |		
		  ___|   ||       || ______||       ||   | |  _    |		
		 |       ||   _   || |_____ | ||_|| ||   | | | |   |		
		 |__yama_||__| |__||_______||_|   |_||___| |_|  |__|	 
		 
--------------------------------------------------------------------------------
********************************************************************************
 							Copyright (c) 2015 yama.
 This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 ANY use of this software MUST be subject to the consent of yama.

********************************************************************************
*/
package jazmin.core;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jazmin.core.aop.Dispatcher;
import jazmin.core.app.Application;
import jazmin.core.app.ApplicationLoader;
import jazmin.core.boot.BootScriptLoader;
import jazmin.core.job.JobStore;
import jazmin.core.task.TaskStore;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.util.IOUtil;

/**
 * JazminServer is a Java based application/message/rpc server.
 * @author yama
 * 2014-12-20
 */
public class Jazmin {
	private static Logger logger=LoggerFactory.get(Jazmin.class);
	//
	public static final String VERSION;
	public static final String LOGO=            
"                                                                           \n"+
"                                      .                c,                  \n"+
"                                    .xMXl           .oNMO                  \n"+
"                                   lNMMMMX'      ,o0MMMMO                  \n"+
"                                 lNMMMMMMMN. .d0WMMMMMMMd           ..     \n"+
"                               ,XMMMMMMMMMM. ,MMMWXOdc;,.    .;ldOXWN.     \n"+
"                     oOKNWWk  :WMMMMMMMMMMx  kkl'    .,cok0NMMMMMMMN,      \n"+
"                     dMMMMK  .WMMMMMMMMMWo     .;oOXWMMMMMMMMMMMMMO.       \n"+
"                     ;0KWMk  oMMMMMMMMM0'   .l0WMMMMMMMMMMMMMMMMK,         \n"+
"                  .','.  .l. ,WMMMMMMX:   'kWMMMMZJLMMMMMMMMMWx'           \n"+
"             ,lkKWMMMMMNO:    .0W0K0;   .OMMMMMMMMMMMMMMMMNk:.  ..         \n"+
"         .c0WMMMMMMMMMMMMMX,    .      lWMMMMMMMMMMMMN0d:.    ,0MMMWX0kdl; \n"+
"         'kMMMMMMMMMMMMMMMMk          ;0MMMMMWN0ko:'.     .,oXMMMMMMMMMXd' \n"+
"           ,XMMMMMMMMMMMXd;            .dl:,.       .';lkXMMMMMMMMMMXd'    \n"+
"             :kXWMMMN0o.                      .;dOKNMMMMMMMMMMMMW0l.       \n"+
"            .    ..      .        ,0KK00Oxdc,.   :KMMMMMMMMMNOo,.          \n"+
"         .oNMNko'   .ckXWW0d    lNMMMMMMMMMMMWKl   cddolc;'.               \n"+
"        oWMMMXc  .lXMMMMMMMd    NMMMMMMMMMMMMMMMX.                         \n"+
"      .KMMMX;  ,kWMMMMMMMMM;   .WMMMMMMMMMMMMMMMMX.                        \n"+
"      ,,,,,  '0MMMMMMMMMMMK  ;  XMMMMMMMMMMMMMMMMMO                        \n"+
"           .kMMMMMMMMMMMMN. .N: :MMMMMMMMMMMMMMMMMMl                       \n"+
"          'NMMMMMMMMMMMMO. 'NMN. xMMMMMMMMMMMMMMMMMW'                      \n"+
"         .XMMMMMMMMMMKd' .oWMMMK. oWMMMMMMMMMMMMMMMMK                      \n"+
"         OMMMMMMMNx;.   dWMMMMMMN: .oKMMMMMMMMMMMMMMMO                     \n"+
"        :MMMMMKo'       lMMMMMMMMMK,  .;oOXWMMMMMMMMMMk                    \n"+
"        OMW0l.           cWMMMMMMMK.        .,:ldkOKXNWx                   \n"+
"       .Xo.               .KMMMMMO                                         \n"+
"                            cXMMO                                          \n"+
"                              ,x.                                          \n"+
"        																	\n"+
"	      ___  _______  _______  __   __  ___   __    _ 					\n"+
"	     |   ||   _   ||       ||  |_|  ||   | |  |  | |					\n"+
"	     |   ||  |_|  ||____   ||       ||   | |   |_| |					\n"+
"	     |   ||       | ____|  ||       ||   | |       |					\n"+
"	  ___|   ||       || ______||       ||   | |  _    |					\n"+
"	 |       ||   _   || |_____ | ||_|| ||   | | | |   |					\n"+
"	 |__yama_||__| |__||_______||_|   |_||___| |_|  |__|					\n";   

	//--------------------------------------------------------------------------
   static{
		VERSION=Jazmin.class.getPackage().getImplementationVersion();
	}
	//
	private static ScheduledExecutorService scheduledExecutorService=
			new ScheduledThreadPoolExecutor(
					5,
					new JazminThreadFactory("ScheduledExecutor"),
					new ThreadPoolExecutor.AbortPolicy());
	//--------------------------------------------------------------------------
	public static final Environment environment=new Environment();
	public static final Dispatcher dispatcher=new Dispatcher();
	public static final TaskStore taskStore=new TaskStore();
	public static final JobStore jobStore=new JobStore();
	//
	private static List<Lifecycle>lifecycles;
	private static Map<String,Driver>drivers;
	private static Map<String,Server>servers;
	private static String serverName;
	private static Date startTime;
	private static String bootFile;
	private static String applicationPackage;
	private static Application application;
	private static ClassLoader appClassloader;
	//
	static{
		lifecycles=new ArrayList<>();
		drivers=new ConcurrentHashMap<String, Driver>();
		servers=new ConcurrentHashMap<String, Server>();
		serverName="default";
		startTime=new Date();
		appClassloader=Thread.currentThread().getContextClassLoader();
	}
	/**
	 * get server start time
	 */
	public static Date getStartTime(){
		return startTime;
	}
	/**
	 * return server name
	 */
	public static String getServerName(){
		return serverName;
	}
	/**
	 * return server name
	 */
	public static void setServerName(String name){
		serverName=name;
	}
	/**
	 * return boot file path
	 */
	public static String getBootFile(){
		return bootFile;
	}
	//--------------------------------------------------------------------------
	/**
	 * boot jazmin server from config file specified by bootFileURI
	 * @param bootFileURI the file will be boot 
	 */
	public static void bootFromURI(String bootFileURI)throws Exception{
		logger.info("boot from:"+bootFileURI);
		BootScriptLoader bsl=new BootScriptLoader(new URL(bootFileURI).openStream());
		bsl.load();
	}
	//
	/**
	 * boot jazmin server from local file
	 * @param bootFile the boot file
	 */
	public static void boot(File bootFile)throws Exception{
		logger.info("boot from:"+bootFile.getAbsolutePath());
		BootScriptLoader bsl=new BootScriptLoader(new FileInputStream(bootFile));
		bsl.load();
	}
	//--------------------------------------------------------------------------
	/**
	 * return server path
	 */
	public static String getServerPath(){
		return new File(".").getAbsolutePath();
	}
	/**
	 * load application package
	 * @param appPackage the application package
	 */
	public static void loadApplication(String appPackage){
		logger.info("load application from:"+appPackage);
		ApplicationLoader applicationLoader=new ApplicationLoader(
				new File("work"),new File(appPackage));
		applicationPackage=appPackage;
		loadApplication(applicationLoader.load());
	}
	/**
	 * load application 
	 * @param app the application 
	 * @see Application
	 */
	public static void loadApplication(Application app){
		application=app;
		if(app!=null){
			appClassloader=application.getClass().getClassLoader();
		}
	}
	/**
	 * return application package name
	 * @return application package name
	 */
	public static String getApplicationPackage(){
		return applicationPackage;
	}
	/**
	 * return application instance 
	 * @return application instance
	 */
	public static Application getApplication(){
		return application;
	}
	/**
	 * return application class loader
	 * @return the application class loader
	 */
	public static ClassLoader getAppClassLoader(){
		return appClassloader;
	}
	/**
	 * set application class loader
	 * @param classLoader the application class loader will be used
	 */
	public static void setAppClassLoader(ClassLoader classLoader){
		appClassloader=classLoader;
	}
	//
	//--------------------------------------------------------------------------
	//drivers
	/**
	 * add driver to jazmin server with specified name.
	 * @param driver the driver will be added
	 */
	public static void addDriver(Driver driver){
		String name=driver.getClass().getSimpleName();
		if(drivers.containsKey(name)){
			throw new IllegalArgumentException("driver:"+name+" already exists.");
		}
		drivers.put(name, driver);
	}
	/**
	 * return driver by class
	 * @param the driverClass 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T  getDriver(Class<? extends Driver> driverClass){
		for(Driver d:drivers.values()){
			if(driverClass.equals(d.getClass())){
				return (T) d;
			}
			if(driverClass.isAssignableFrom(d.getClass())){
				return (T) d;
			}
		}
		return null;
	}
	/**
	 *get all drivers 
	 *@return all drivers
	 */
	public static List<Driver>getDrivers(){
		return new ArrayList<Driver>(drivers.values());
	}
	//--------------------------------------------------------------------------
	//servers
	/**
	 * add server to jazmin server.
	 * @param the server will be added
	 */
	public static void addServer(Server server) {
		String name=server.getClass().getSimpleName();
		if (servers.containsKey(name)) {
			throw new IllegalArgumentException("server:" + name
					+ " already exists.");
		}
		servers.put(name, server);
	}

	/**
	 * get server by type.
	 * @return the server 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T  getServer(Class<? extends Server> serverClass){
		for(Server d:servers.values()){
			if(serverClass.equals(d.getClass())){
				return (T) d;
			}
			if(serverClass.isAssignableFrom(d.getClass())){
				return (T) d;
			}
		}
		return null;
	}
	/**
	 *get all servers 
	 *@return all servers
	 */
	public static List<Server>getServers(){
		return new ArrayList<Server>(servers.values());
	}
	//
	// --------------------------------------------------------------------------
	//task
	/**
	 * schedule task with specified delay time and repeat it every period time
	 * @param command the command class will be scheduled
	 * @param initialDelay the initialDelay time
	 * @param period repeat period
	 * @param unit time unit of repeat time
	 */
	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {
		return scheduledExecutorService.scheduleAtFixedRate(command,
				initialDelay, period, unit);
	}
	
	/**
	 * @param command
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	public static void execute(Runnable command) {
		dispatcher.invokeInPool(
				command.getClass().getSimpleName(),
				command, Runnable.class.getMethods()[0]);
	}
	/**
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
	 */
	public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit) {
		return scheduledExecutorService.scheduleWithFixedDelay(command,
				initialDelay, delay, unit);
	}
	
	/**
	 * @param command
	 * @param delay
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ScheduledExecutorService#schedule(
	 * java.lang.Runnable, long, java.util.concurrent.TimeUnit)
	 */
	public static ScheduledFuture<?> schedule(Runnable command, long delay,
			TimeUnit unit) {
		return scheduledExecutorService.schedule(command, delay, unit);
	}
	//
	// --------------------------------------------------------------------------
	static void dumpLogo(){
		logger.info("\n"+LOGO);	
	}
	/**
	 * start jazmin server
	 */
	public static void start(){
		//
		String propServerName=System.getProperty("jazmin.server.name");
		if(propServerName!=null){
			serverName=propServerName;
		}
		if(serverName==null){
			serverName="default";
		}
		bootFile=System.getProperty("jazmin.boot.file");
		if(bootFile!=null){
			try {
				File bf=new File(bootFile);
				boot(bf);
			} catch (Exception e) {
				logger.fatal(e.getMessage(),e);
				System.exit(1);
			}
		}
		//start up sequence is very important,not change it
		lifecycles.add(environment);
		lifecycles.add(dispatcher);
		lifecycles.addAll(servers.values());
		lifecycles.addAll(drivers.values());
		lifecycles.add(taskStore);
		lifecycles.add(jobStore);
		if(application!=null){
			lifecycles.add(application);
		}
		try {
			initLifecycle();
			startLifecycle();
			dumpLifecycle();
			dumpJazmin();
		} catch (Throwable e) {
			logger.fatal(e.getMessage(),e);
			System.exit(1);
		}
	}
	//
	private static void dumpJazmin(){
		InfoBuilder ib=InfoBuilder.create();
		ib.section("Jazmin dump info");
		ib.format("%-30s:%-30s\n");
		ib.print("serverName",getServerName());
		ib.print("serverPath",getServerPath());
		ib.print("appClassloader",appClassloader);
		ib.print("applicationPackage",applicationPackage);
		logger.info("\n"+ib.toString());
		Date endTime=new Date();
		Duration d=Duration.between(startTime.toInstant(),endTime.toInstant());
		logger.info("jazmin start using {}//",d);
	}
	/**
	 * 
	 */
	private static void dumpLifecycle(){
		lifecycles.forEach(lc->{
			logger.info(lc.getClass().getSimpleName()+" dump information//");
			String lcInfo=lc.info();
			if(lcInfo!=null&&lc.info().trim().length()>0){
				logger.info("\n"+lc.info());		
			}
		});
	}
	//
	/**
	 * 
	 */
	private static void startLifecycle() throws Exception{
		for(Lifecycle lc:lifecycles){
			if(lc==null){
				continue;
			}
			logger.info("start lifecycle:{}-{}//",lc.getClass().getName(),
					lc.lifecycleListener);
			if(lc.lifecycleListener!=null){
				lc.lifecycleListener.beforeStart(lc);
			}
			lc.start();
			if(lc.lifecycleListener!=null){
				lc.lifecycleListener.afterStart(lc);
			}
			lc.started=true;
		}
	}
	/**
	 * 
	 */
	private static void initLifecycle() throws Exception{
		for(Lifecycle lc:lifecycles){
			if(lc==null){
				continue;
			}
			logger.info("init lifecycle:{}-{}//",lc.getClass().getName(),
					lc.lifecycleListener);
			if(lc.lifecycleListener!=null){
				lc.lifecycleListener.beforeInit(lc);
			}
			lc.init();
			if(lc.lifecycleListener!=null){
				lc.lifecycleListener.afterInit(lc);
			}
			lc.inited=true;
		}
	}
	/**
	 * 
	 */
	private static void stopLifecycle() throws Exception{
		Collections.reverse(lifecycles);
		for(Lifecycle lc:lifecycles){
			if(lc==null){
				continue;
			}
			logger.info("stop lifecycle:{}-{}//",lc.getClass().getName(),
					lc.lifecycleListener);
			if(lc.lifecycleListener!=null){
				lc.lifecycleListener.beforeStop(lc);
			}
			lc.stop();
			if(lc.lifecycleListener!=null){
				lc.lifecycleListener.afterStop(lc);
			}
		}
	}
	/**
	 * 
	 */
	private static void stop(){
		try {
			stopLifecycle();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		File workDir=new File("work");
		IOUtil.deleteDirectory(workDir);
		//
		Date stopTime=new Date();
		Duration d=Duration.between(stopTime.toInstant(), startTime.toInstant());
		logger.info("jazmin {} running {}//",VERSION,d);  
		LoggerFactory.stop();
	}
	//--------------------------------------------------------------------------
	/**
	 * main entry of jazmin server
	 */
	public static void main(String[] args) {
		Thread shutdownThread=new Thread(Jazmin::stop);
		shutdownThread.setName("ShutdownHook");
		Runtime.getRuntime().addShutdownHook(shutdownThread);
		Jazmin.dumpLogo();
		Jazmin.start();
	}
}
