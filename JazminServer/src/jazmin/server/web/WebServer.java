package jazmin.server.web;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

import jazmin.core.Jazmin;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;
import jazmin.server.web.mvc.ControllerStub;
import jazmin.server.web.mvc.DispatchServlet;
import jazmin.server.web.mvc.MethodStub;
import jazmin.util.FileUtil;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;
/**
 * 
 * @author yama
 * 27 Dec, 2014
 */
public class WebServer extends jazmin.core.Server{
	//
	private static Logger logger=LoggerFactory.get(WebServer.class);
	//
	private Server server;
	private HandlerCollection handlers;
	private WebAppContext webAppContext;
	private ContextHandlerCollection contextHandler;
	private RequestLogHandler requestLogHandler;
	private int port=7001;
	private int idleTimeout=30;//30seconds
	private boolean dirAllowed=false;
	static{
		System.getProperties().put("org.eclipse.jetty.util.log.class",
				JettyLogger.class.getName());
	}
	public WebServer() {
	}
	
	/**
	 * @return the dirAllowed
	 */
	public boolean dirAllowed() {
		return dirAllowed;
	}

	/**
	 * @param dirAllowed the dirAllowed to set
	 */
	public void dirAllowed(boolean dirAllowed) {
		this.dirAllowed = dirAllowed;
	}

	/**
	 * 
	 */
	public void addWar(String contextPath,String war) throws Exception{
		webAppContext=createWebAppContext(contextPath);
		webAppContext.setWar(war);
	}
	/**
	 * 
	 */
	public void addResource(String contextPath,String resourceBase){
		webAppContext=createWebAppContext(contextPath);
		webAppContext.setResourceBase(resourceBase);
	}
	//
	private WebAppContext createWebAppContext(String contextPath){
		WebAppContext webAppContext=new WebAppContext();
		webAppContext.setContextPath(contextPath);
		File tempDir=new File("webapp");
		FileUtil.deleteDirectory(tempDir);
		if(!tempDir.mkdir()){
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
		return webAppContext;
	}
	//
	//
	public int port() {
		return port;
	}
	//
	public void port(int port) {
		this.port = port;
	}
	//
	public int idleTimeout() {
		return idleTimeout;
	}
	//
	public void idleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}
	//
	public WebAppContext webAppContext(){
		return webAppContext;
	}
	//-------------------------------------------------------------------------
	//
	public void init()throws Exception{
		//set up jetty logger
		
		//
		server = new Server();
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
		//
		HttpConfiguration httpConfig = new HttpConfiguration();
        ServerConnector httpConnector = new ServerConnector(server,
        		new HttpConnectionFactory(httpConfig)); 
		httpConnector.setPort(port);
		httpConnector.setIdleTimeout(idleTimeout*1000);
		//
		server.setConnectors(new Connector[] {httpConnector});		
        server.setHandler(handlers);
        server.start();
		if(webAppContext!=null){
			Jazmin.appClassLoader(webAppContext.getClassLoader());
		}
		//
		ConsoleServer cs=Jazmin.server(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(new WebServerCommand());
		}
	}
	//
	//
	public void start() throws Exception{
		if(webAppContext!=null){
			JspFactory jspFactory=JspFactory.getDefaultFactory();
			ServletContext sc=webAppContext.getServletContext();
			if(jspFactory!=null){
				JspApplicationContext jac=jspFactory.getJspApplicationContext(sc);
				jac.addELResolver(new PublicFieldELResolver());					
			}
		}
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
		ib.print("port",port);
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
		List<MethodStub>msList=new ArrayList<MethodStub>();
		csList.forEach(cs->msList.addAll(cs.methodStubs()));
		Collections.sort(msList);
		ib.section("services");
		msList.forEach(ib::println);
		//
		return ib.toString();
	}
}
