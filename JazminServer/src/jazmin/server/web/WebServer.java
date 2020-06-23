package jazmin.server.web;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

import jazmin.core.Jazmin;
import jazmin.core.Registerable;
import jazmin.core.monitor.Monitor;
import jazmin.core.monitor.MonitorAgent;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;
import jazmin.server.web.mvc.ControllerStub;
import jazmin.server.web.mvc.DispatchServlet;
import jazmin.util.FileUtil;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
/**
 * WebServer is a wrapper of Jetty(http://www.eclipse.org/jetty/)
 * @author yama
 * 27 Dec, 2014
 */
public class WebServer extends jazmin.core.Server implements Registerable{
	//
	private static Logger logger=LoggerFactory.get(WebServer.class);
	//
	private Server server;
	private HandlerCollection handlers;
	private WebAppContext webAppContext;
	private ContextHandlerCollection contextHandler;
	private RequestLogHandler requestLogHandler;
	private int port=7001;
	private int httpsPort=-1;
	private String keyStoreFile;
	private String keyStoreType;
	private String keyStorePassword;
	private int idleTimeout=30;//30seconds
	private boolean dirAllowed=false;
	private String hostname;
	private AtomicLong totalSessionCounter;
	private Map<String, Object>serverAttributes;
	static{
		System.getProperties().put("org.eclipse.jetty.util.log.class",JettyLogger.class.getName());
	}
	public WebServer() {
		totalSessionCounter=new AtomicLong();
		serverAttributes=new HashMap<>();
	}
	//
	public void setServerAttribute(String key,Object value){
		serverAttributes.put(key, value);
	}
	//
	/**
	 * set jetty logger enable flag
	 * @param enabled jetty logger enable flag
	 */
	public void setEnableJettyLogger(boolean enabled){
		JettyLogger.enable=enabled;
	}
	/**
	 * return whether or not jetty server default servlet allowed directory list 
	 * @return the dirAllowed
	 */
	public boolean isDirAllowed() {
		return dirAllowed;
	}

	/**
	 * set whether or not jetty server default servlet allowed directory list 
	 * @param dirAllowed the dirAllowed to set
	 */
	public void setDirAllowed(boolean dirAllowed) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.dirAllowed = dirAllowed;
	}
	/**
	 * add war package to server
	 * @param contextPath the war context path
	 * @param war the war path
	 */
	public void addWar(String contextPath,String war){
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		webAppContext=createWebAppContext(contextPath);
		webAppContext.setWar(war);
	}
	/**
	 * add resource directory to server
	 * @param contextPath the resource context path
	 * @param resourceBase resource directory path
	 */
	public void addResource(String contextPath,String resourceBase){
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		webAppContext=createWebAppContext(contextPath);
		webAppContext.setResourceBase(resourceBase);
	}
	/**
	 * create context for jazmin web application
	 * @param contextPath the web applicaiton context path
	 * @param resourceBase the web applicaiton resource base
	 */
	public void addApplication(String contextPath,String resourceBase){
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		webAppContext=createWebAppContext(contextPath);
		webAppContext.setResourceBase(resourceBase);
	}
	//
	public void addServlet(Class<? extends Servlet> servlet,String pathSpec){
		webAppContext.addServlet(servlet, pathSpec);
	}
	//
	private WebAppContext createWebAppContext(String contextPath){
		WebAppContext webAppContext=new WebAppContext();
		webAppContext.setContextPath(contextPath);
		File tempDir=new File("webapp/"+Jazmin.getServerName());
		FileUtil.deleteDirectory(tempDir);
		if(!tempDir.mkdirs()){
			logger.warn("can not create temp dir:"+tempDir.getAbsolutePath());
		}else{
			logger.info("set temp dir to:"+tempDir.getAbsolutePath());
			webAppContext.setTempDirectory(tempDir);		
		}
		ServletHolder defaultServlet=new ServletHolder(DefaultServlet.class);
		webAppContext.addServlet(defaultServlet, "/");
		webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed",
				Boolean.toString(dirAllowed));
		List<String>configList=new ArrayList<String>();
		configList.add("org.eclipse.jetty.webapp.WebInfConfiguration");
		configList.add("org.eclipse.jetty.webapp.WebXmlConfiguration");
		configList.add("org.eclipse.jetty.webapp.MetaInfConfiguration");
		configList.add("org.eclipse.jetty.webapp.FragmentConfiguration");
		configList.add("org.eclipse.jetty.plus.webapp.EnvConfiguration");
		configList.add("org.eclipse.jetty.plus.webapp.PlusConfiguration");
		configList.add("org.eclipse.jetty.annotations.AnnotationConfiguration");
		webAppContext.setConfigurationClasses(configList);
		//
//		if(webAppContext.getSessionHandler()!=null){
//			if(webAppContext.getSessionHandler().getSessionIdManager()!=null){
//				webAppContext.getSessionHandler().getSessionManager().addEventListener(new HttpSessionListener() {
//					@Override
//					public void sessionDestroyed(HttpSessionEvent e) {
//						totalSessionCounter.decrementAndGet();
//					}
//					//
//					@Override
//					public void sessionCreated(HttpSessionEvent e) {
//						totalSessionCounter.incrementAndGet();
//					}
//				});
//			}
//		}
		//
		return webAppContext;
	}
	//
	/**
	 * return port of this server
	 * @return port of this server
	 */
	public int getPort() {
		return port;
	}
	/**
	 * set port of this server
	 * @param port the port of server
	 */
	public void setPort(int port) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.port = port;
	}
	//
	
	//
	/**
	 * return idle timeout time of server connection
	 * @return idle timeout time of server connection
	 */
	public int getIdleTimeout() {
		return idleTimeout;
	}

	/**
	 * @return the httpsPort
	 */
	public int getHttpsPort() {
		return httpsPort;
	}
	/**
	 * @param httpsPort the httpsPort to set
	 */
	public void setHttpsPort(int httpsPort) {
		this.httpsPort = httpsPort;
	}
	/**
	 * @return the keyStoreFile
	 */
	public String getKeyStoreFile() {
		return keyStoreFile;
	}
	
	/**
	 * @return the keyStorePassword
	 */
	public String getKeyStorePassword() {
		return keyStorePassword;
	}
	/**
	 * @param keyStorePassword the keyStorePassword to set
	 */
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}
	/**
	 * @param keyStoreFile the keyStoreFile to set
	 */
	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}
	/**
	 * set server idle timeout time of server connection
	 * @param idleTimeout server idle timeout time of server connection 
	 */ 
	public void setIdleTimeout(int idleTimeout) {
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.idleTimeout = idleTimeout;
	}
	
	/**
	 * @return the keyStoreType
	 */
	public String getKeyStoreType() {
		return keyStoreType;
	}
	/**
	 * @param keyStoreType the keyStoreType to set
	 */
	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}
	/**
	 * return WebAppContext of this server
	 * @return  WebAppContext of this server
	 */
	public WebAppContext getWebAppContext(){
		return webAppContext;
	}
	//
	public void setHostname(String hostname){
		this.hostname=hostname;
	}
	//
	public String getHostname(){
		return hostname;
	}
	//--------------------------------------------------------------------------
	//
	static class JettyThreadPool implements ThreadPool{
		@Override
		public void execute(Runnable command) {
			Jazmin.dispatcher.execute(command);
		}
		//
		@Override
		public int getIdleThreads() {
			return Jazmin.dispatcher.getMaximumPoolSize()-Jazmin.dispatcher.getActiveCount();
		}
		//
		@Override
		public int getThreads() {
			return Jazmin.dispatcher.getMaximumPoolSize();
		}
		//
		@Override
		public boolean isLowOnThreads() {
			return false;
		}
		//
		@Override
		public void join() throws InterruptedException {
			logger.info("call JettyThreadPool join");
		}
	}
	//
	public void init()throws Exception{
		//set up jetty logger
		server = new Server(new JettyThreadPool());
		// Setup JMX
		MBeanContainer mbContainer=new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		server.addEventListener(mbContainer);
		server.addBean(mbContainer);
		// Add loggers MBean to server (will be picked up by MBeanContainer above)
		server.addBean(Log.getLog());
		handlers = new HandlerCollection();
	    contextHandler=new ContextHandlerCollection();
		requestLogHandler=new RequestLogHandler();
		requestLogHandler.setRequestLog(new WebRequestLog());
	    handlers.setHandlers(new Handler[] {
	    		new DefaultHandler(),
	    		contextHandler,
	    		requestLogHandler});
	    //  
	    if(webAppContext!=null){
			contextHandler.addHandler(webAppContext);
		}
	    List<Connector>connectors=new ArrayList<Connector>();
		//
		HttpConfiguration httpConfig = new HttpConfiguration();
        ServerConnector httpConnector = new ServerConnector(server,
        		new HttpConnectionFactory(httpConfig)); 
		httpConnector.setPort(port);
		httpConnector.setIdleTimeout(idleTimeout*1000);
		connectors.add(httpConnector);
		//
		if(httpsPort!=-1){
			 HttpConfiguration sslHttpConfig = new HttpConfiguration();
			 sslHttpConfig.setSecureScheme("https");
			 sslHttpConfig.setSecurePort(httpsPort);
			 sslHttpConfig.setOutputBufferSize(32768);
			 SslContextFactory sslContextFactory = new SslContextFactory();
			 if(keyStoreType!=null){
				 sslContextFactory.setKeyStoreType(keyStoreType);	 
			 }
			 sslContextFactory.setKeyStorePath(keyStoreFile);
			 if(keyStorePassword!=null){
				 sslContextFactory.setKeyStorePassword(keyStorePassword);
			 }
			 //sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
			 HttpConfiguration https_config = new HttpConfiguration(sslHttpConfig);
			 sslHttpConfig.addCustomizer(new SecureRequestCustomizer());
			 ServerConnector https = new ServerConnector(server,
			              new SslConnectionFactory(sslContextFactory, "http/1.1"),
			                new HttpConnectionFactory(https_config));
			 https.setPort(httpsPort);
			 https.setIdleTimeout(idleTimeout*1000);
			 connectors.add(https);
		}
		//
		server.setConnectors(connectors.toArray(new Connector[connectors.size()]));		
        server.setHandler(handlers);
        for(Entry<String, Object> e :serverAttributes.entrySet()){
			server.setAttribute(e.getKey(), e.getValue());
		}
        server.start();
        if(webAppContext!=null){
			Jazmin.setAppClassLoader(webAppContext.getClassLoader());
		}
		//
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(WebServerCommand.class);
		}
	}
	//
	//
	public void start() throws Exception{
		if(webAppContext!=null){
			JspFactory jspFactory=JspFactory.getDefaultFactory();
			ServletContext sc=webAppContext.getServletContext();
			if(jspFactory!=null){
//				JspApplicationContext jac=jspFactory.getJspApplicationContext(sc);
//				jac.addELResolver(new PublicFieldELResolver());					
			}
		}
		//
		Jazmin.mointor.registerAgent(new WebServerMonitorAgent());
	}
	//
	public void stop() throws Exception{
		server.stop();
	}
	//
	@Override
	public String info() {
		InfoBuilder ib=InfoBuilder.create();
		ib.section("info");
		ib.format("%-30s:%-30s\n");
		ib.print("jettyVersion",Jetty.VERSION);
		ib.print("port",port);
		ib.print("httpsPort",httpsPort);
		ib.print("keyStoreFile",keyStoreFile);
		ib.print("keyStoreType",keyStoreType);
		ib.print("idleTimeout",idleTimeout);
		ib.print("dirAllowed",dirAllowed);
		ib.print("webAppContext",webAppContext);
		ib.section("servlets:");
		if(webAppContext!=null){
			webAppContext.getServletContext().getServletRegistrations().forEach((k,v)->{
				ib.println(k);
			});	
			ib.section("welcome files");
			int index=0;
			if(webAppContext.getWelcomeFiles()!=null){
				for(String s:webAppContext.getWelcomeFiles()){
					ib.print("welcome file-"+(index++),s);						
				}	
			}
		}
		//
		List<ControllerStub>csList=DispatchServlet.dispatcher.controllerStubs();
		ib.section("controllers");
		for(ControllerStub cs:csList){
			ib.println(cs);
			cs.methodStubs().forEach(ib::println);
		}
		//
		return ib.toString();
	}
	//
	//
	private  class WebServerMonitorAgent implements MonitorAgent{
		@Override
		public void sample(int idx,Monitor monitor) {
			Map<String,String>info=new HashMap<String, String>();
			info.put("SessionCount", totalSessionCounter.longValue()+"");
			monitor.sample("WebServer.SessionCount",Monitor.CATEGORY_TYPE_VALUE,info);
			//
			Map<String,String>info2=new HashMap<String, String>();
			info2.put("InvokeCount", DispatchServlet.dispatcher.getInvokeCount()+"");
			monitor.sample("WebServer.InvokeCount",Monitor.CATEGORY_TYPE_COUNT,info2);
		}
		//
		@Override
		public void start(Monitor monitor) {
			Map<String,String>info=new HashMap<String, String>();
			info.put("jettyVersion",Jetty.VERSION);
			info.put("port",port+"");
			info.put("httpsPort",httpsPort+"");
			info.put("keyStoreFile",keyStoreFile+"");
			info.put("keyStoreType",keyStoreType+"");
			info.put("idleTimeout",idleTimeout+"");
			info.put("dirAllowed",dirAllowed+"");
			info.put("webAppContext",webAppContext+"");
			for(Entry<String, Object> e :serverAttributes.entrySet()){
				info.put("serverAttribute-"+e.getKey(),e.getValue()+"");
			}
			monitor.sample("WebServer.Info",Monitor.CATEGORY_TYPE_KV,info);
		}
	}
	@Override
	public void register(Object object) {
		DispatchServlet.dispatcher.registerController(object);
	}
	
}
