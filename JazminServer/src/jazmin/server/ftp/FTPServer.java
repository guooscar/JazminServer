/**
 * 
 */
package jazmin.server.ftp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.core.thread.Dispatcher;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftpletcontainer.impl.DefaultFtpletContainer;
import org.apache.ftpserver.impl.DefaultFtpServer;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;

/**
 * @author yama
 * 26 Mar, 2015
 */
public class FTPServer extends Server{
	//
	private FtpServer server;
	private ListenerFactory factory;
	private SslConfigurationFactory ssl;
	private ConnectionConfigFactory connectionConfigFactory;
	private LinkedHashMap<String,Ftplet>ftplets;
	private CommandListener commandListener;
	private FtpStatistics statistics;
	private Map<String,FileTransferInfo>fileTransferInfos;
	private Method listenerBeforeMethod;
	private Method listenerAfterMethod;
	private Method listenerOnConnectMethod;
	private Method listenerOnDisConnectMethod;
	private FtpUserManager userManager;
	private Map<String, jazmin.server.ftp.FtpSession>sessionMap;
	private DataConnectionConfigurationFactory dataConnectionConfigurationFactory;
	//
	public FTPServer() {
		factory = new ListenerFactory();
		ssl = new SslConfigurationFactory();
		connectionConfigFactory=new ConnectionConfigFactory();
		ftplets=new LinkedHashMap<String, Ftplet>();
		ftplets.put("EX",new ServiceFtplet());
		fileTransferInfos=new ConcurrentHashMap<String, FileTransferInfo>();
		dataConnectionConfigurationFactory=new DataConnectionConfigurationFactory();
		sessionMap=new ConcurrentHashMap<String, jazmin.server.ftp.FtpSession>();
		listenerBeforeMethod=Dispatcher.getMethod(
				CommandListener.class,
				"beforeCommand",FtpSession.class, FtpRequest.class);
		listenerAfterMethod=Dispatcher.getMethod(
				CommandListener.class,
				"afterCommand",FtpSession.class, FtpRequest.class,FtpReply.class);
		listenerOnConnectMethod=Dispatcher.getMethod(
				CommandListener.class,
				"onConnect",FtpSession.class);
		listenerOnDisConnectMethod=Dispatcher.getMethod(
				CommandListener.class,
				"onDisconnect",FtpSession.class);
	}
	
	/**
	 * @return
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#getActiveLocalAddress()
	 */
	public String getActiveLocalAddress() {
		return dataConnectionConfigurationFactory.getActiveLocalAddress();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#getActiveLocalPort()
	 */
	public int getActiveLocalPort() {
		return dataConnectionConfigurationFactory.getActiveLocalPort();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#getIdleTime()
	 */
	public int getIdleTime() {
		return dataConnectionConfigurationFactory.getIdleTime();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#getPassiveAddress()
	 */
	public String getPassiveAddress() {
		return dataConnectionConfigurationFactory.getPassiveAddress();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#getPassiveExternalAddress()
	 */
	public String getPassiveExternalAddress() {
		return dataConnectionConfigurationFactory.getPassiveExternalAddress();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#getPassivePorts()
	 */
	public String getPassivePorts() {
		return dataConnectionConfigurationFactory.getPassivePorts();
	}
	/**
	 * @return
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#isActiveEnabled()
	 */
	public boolean isActiveEnabled() {
		return dataConnectionConfigurationFactory.isActiveEnabled();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#isActiveIpCheck()
	 */
	public boolean isActiveIpCheck() {
		return dataConnectionConfigurationFactory.isActiveIpCheck();
	}

	/**
	 * @param activeEnabled
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#setActiveEnabled(boolean)
	 */
	public void setActiveEnabled(boolean activeEnabled) {
		dataConnectionConfigurationFactory.setActiveEnabled(activeEnabled);
	}

	/**
	 * @param activeIpCheck
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#setActiveIpCheck(boolean)
	 */
	public void setActiveIpCheck(boolean activeIpCheck) {
		dataConnectionConfigurationFactory.setActiveIpCheck(activeIpCheck);
	}

	/**
	 * @param activeLocalAddress
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#setActiveLocalAddress(java.lang.String)
	 */
	public void setActiveLocalAddress(String activeLocalAddress) {
		dataConnectionConfigurationFactory
				.setActiveLocalAddress(activeLocalAddress);
	}

	/**
	 * @param activeLocalPort
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#setActiveLocalPort(int)
	 */
	public void setActiveLocalPort(int activeLocalPort) {
		dataConnectionConfigurationFactory.setActiveLocalPort(activeLocalPort);
	}

	/**
	 * @param idleTime
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#setIdleTime(int)
	 */
	public void setIdleTime(int idleTime) {
		dataConnectionConfigurationFactory.setIdleTime(idleTime);
	}

	/**
	 * @param passiveAddress
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#setPassiveAddress(java.lang.String)
	 */
	public void setPassiveAddress(String passiveAddress) {
		dataConnectionConfigurationFactory.setPassiveAddress(passiveAddress);
	}

	/**
	 * @param passiveExternalAddress
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#setPassiveExternalAddress(java.lang.String)
	 */
	public void setPassiveExternalAddress(String passiveExternalAddress) {
		dataConnectionConfigurationFactory
				.setPassiveExternalAddress(passiveExternalAddress);
	}

	/**
	 * @param passivePorts
	 * @see org.apache.ftpserver.DataConnectionConfigurationFactory#setPassivePorts(java.lang.String)
	 */
	public void setPassivePorts(String passivePorts) {
		dataConnectionConfigurationFactory.setPassivePorts(passivePorts);
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ConnectionConfigFactory#getLoginFailureDelay()
	 */
	public int getLoginFailureDelay() {
		return connectionConfigFactory.getLoginFailureDelay();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ConnectionConfigFactory#getMaxAnonymousLogins()
	 */
	public int getMaxAnonymousLogins() {
		return connectionConfigFactory.getMaxAnonymousLogins();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ConnectionConfigFactory#getMaxLoginFailures()
	 */
	public int getMaxLoginFailures() {
		return connectionConfigFactory.getMaxLoginFailures();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ConnectionConfigFactory#getMaxLogins()
	 */
	public int getMaxLogins() {
		return connectionConfigFactory.getMaxLogins();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ConnectionConfigFactory#getMaxThreads()
	 */
	public int getMaxThreads() {
		return connectionConfigFactory.getMaxThreads();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ConnectionConfigFactory#isAnonymousLoginEnabled()
	 */
	public boolean isAnonymousLoginEnabled() {
		return connectionConfigFactory.isAnonymousLoginEnabled();
	}

	/**
	 * @param anonymousLoginEnabled
	 * @see org.apache.ftpserver.ConnectionConfigFactory#setAnonymousLoginEnabled(boolean)
	 */
	public void setAnonymousLoginEnabled(boolean anonymousLoginEnabled) {
		connectionConfigFactory.setAnonymousLoginEnabled(anonymousLoginEnabled);
	}

	/**
	 * @param loginFailureDelay
	 * @see org.apache.ftpserver.ConnectionConfigFactory#setLoginFailureDelay(int)
	 */
	public void setLoginFailureDelay(int loginFailureDelay) {
		connectionConfigFactory.setLoginFailureDelay(loginFailureDelay);
	}

	/**
	 * @param maxAnonymousLogins
	 * @see org.apache.ftpserver.ConnectionConfigFactory#setMaxAnonymousLogins(int)
	 */
	public void setMaxAnonymousLogins(int maxAnonymousLogins) {
		connectionConfigFactory.setMaxAnonymousLogins(maxAnonymousLogins);
	}

	/**
	 * @param maxLoginFailures
	 * @see org.apache.ftpserver.ConnectionConfigFactory#setMaxLoginFailures(int)
	 */
	public void setMaxLoginFailures(int maxLoginFailures) {
		connectionConfigFactory.setMaxLoginFailures(maxLoginFailures);
	}

	/**
	 * @param maxLogins
	 * @see org.apache.ftpserver.ConnectionConfigFactory#setMaxLogins(int)
	 */
	public void setMaxLogins(int maxLogins) {
		connectionConfigFactory.setMaxLogins(maxLogins);
	}

	/**
	 * @param maxThreads
	 * @see org.apache.ftpserver.ConnectionConfigFactory#setMaxThreads(int)
	 */
	public void setMaxThreads(int maxThreads) {
		connectionConfigFactory.setMaxThreads(maxThreads);
	}

	//
	/**
	 * @return
	 * @see org.apache.ftpserver.listener.ListenerFactory#getIdleTimeout()
	 */
	public int getIdleTimeout() {
		return factory.getIdleTimeout();
	}

	/**
	 * @return the statistics
	 */
	public FtpStatistics getStatistics() {
		return statistics;
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.listener.ListenerFactory#getPort()
	 */
	public int getPort() {
		return factory.getPort();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.listener.ListenerFactory#getServerAddress()
	 */
	public String getServerAddress() {
		return factory.getServerAddress();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.listener.ListenerFactory#isImplicitSsl()
	 */
	public boolean isImplicitSsl() {
		return factory.isImplicitSsl();
	}


	/**
	 * @param idleTimeout
	 * @see org.apache.ftpserver.listener.ListenerFactory#setIdleTimeout(int)
	 */
	public void setIdleTimeout(int idleTimeout) {
		factory.setIdleTimeout(idleTimeout);
	}

	/**
	 * @param implicitSsl
	 * @see org.apache.ftpserver.listener.ListenerFactory#setImplicitSsl(boolean)
	 */
	public void setImplicitSsl(boolean implicitSsl) {
		factory.setImplicitSsl(implicitSsl);
		if(implicitSsl){
			// set the SSL configuration for the listener
			factory.setSslConfiguration(ssl.createSslConfiguration());	
		}
	}

	/**
	 * @param port
	 * @see org.apache.ftpserver.listener.ListenerFactory#setPort(int)
	 */
	public void setPort(int port) {
		factory.setPort(port);
	}

	/**
	 * @param serverAddress
	 * @see org.apache.ftpserver.listener.ListenerFactory#setServerAddress(java.lang.String)
	 */
	public void setServerAddress(String serverAddress) {
		factory.setServerAddress(serverAddress);
	}

	/**
	 * @param keyStoreFile
	 * @see org.apache.ftpserver.ssl.SslConfigurationFactory#setKeystoreFile(java.io.File)
	 */
	public void setKeystoreFile(File keyStoreFile) {
		ssl.setKeystoreFile(keyStoreFile);
	}
	/**
	 * @param keystorePass
	 * @see org.apache.ftpserver.ssl.SslConfigurationFactory#setKeystorePassword(java.lang.String)
	 */
	public void setKeystorePassword(String keystorePass) {
		ssl.setKeystorePassword(keystorePass);
	}
	/**
	 * @return the commandListener
	 */
	public CommandListener getCommandListener() {
		return commandListener;
	}

	/**
	 * @param commandListener the commandListener to set
	 */
	public void setCommandListener(CommandListener commandListener) {
		this.commandListener = commandListener;
	}
	/**
	 * return file transfer information 
	 * @return file transfer information 
	 */
	public List<FileTransferInfo>getFileTransferInfos(){
		return new ArrayList<FileTransferInfo>(fileTransferInfos.values());
	}
	/**
	 * return user manager
	 * @return
	 */
	public FtpUserManager getUserManager() {
		return userManager;
	}
	/**
	 * set user manager
	 * @param userManager
	 */
	public void setUserManager(FtpUserManager userManager) {
		if(isStarted()){
			throw new IllegalStateException("set before inited.");
		}
		this.userManager = userManager;
	}
	/**
	 * @return all sessions
	 */
	public List<jazmin.server.ftp.FtpSession>getSessions(){
		return new ArrayList<jazmin.server.ftp.FtpSession>(sessionMap.values());
	}
	//--------------------------------------------------------------------------
	
	private class ServiceFtplet implements Ftplet{
		@Override
		public FtpletResult afterCommand(FtpSession arg0, FtpRequest arg1,
				FtpReply arg2) throws FtpException, IOException {
			if(commandListener!=null){
				jazmin.server.ftp.FtpSession session=new jazmin.server.ftp.FtpSession();
				jazmin.server.ftp.FtpRequest request=new jazmin.server.ftp.FtpRequest();
				jazmin.server.ftp.FtpReply reply=new jazmin.server.ftp.FtpReply();
				//
				session.session=arg0;
				request.request=arg1;
				reply.reply=arg2;
				//
				String cmd=request.request.getCommand();
				if(cmd.equals("STOR")||cmd.equals("RETR")){
					fileTransferInfos.remove(arg1.hashCode()+"");
				}
				//
				Jazmin.dispatcher.invokeInPool("", 
						commandListener, 
						listenerAfterMethod,Dispatcher.EMPTY_CALLBACK,session,request,reply);
			}
			return FtpletResult.DEFAULT;
		}

		@Override
		public FtpletResult beforeCommand(FtpSession arg0, FtpRequest arg1)
				throws FtpException, IOException {
			if(commandListener!=null){
				jazmin.server.ftp.FtpSession session=new jazmin.server.ftp.FtpSession();
				jazmin.server.ftp.FtpRequest request=new jazmin.server.ftp.FtpRequest();
				//
				session.session=arg0;
				request.request=arg1;
				//
				String cmd=request.request.getCommand();
				if(cmd.equals("STOR")||cmd.equals("RETR")){
					String argument=request.request.getArgument();
					FileTransferInfo ft=new FileTransferInfo();
					ft.file=argument;
					ft.session=session;
					ft.startTime=new Date();
					ft.type=cmd;
					fileTransferInfos.put(arg1.hashCode()+"",ft);
				}
				Jazmin.dispatcher.invokeInPool("", 
						commandListener, 
						listenerBeforeMethod,Dispatcher.EMPTY_CALLBACK,session,request);
			}
			return FtpletResult.DEFAULT;
		}

		@Override
		public void destroy() {
			
		}

		@Override
		public void init(FtpletContext ctx) throws FtpException {
			statistics=new FtpStatistics();
			statistics.statistics=ctx.getFtpStatistics();
		}

		@Override
		public FtpletResult onConnect(FtpSession arg0) throws FtpException,
				IOException {
			if(commandListener!=null){
				jazmin.server.ftp.FtpSession session=new jazmin.server.ftp.FtpSession();
				session.session=arg0;
				sessionMap.put(session.getSessionId().toString(),session);
				Jazmin.dispatcher.invokeInPool("", 
						commandListener, 
						listenerOnConnectMethod,Dispatcher.EMPTY_CALLBACK,session);
			}
			return FtpletResult.DEFAULT;
		}

		@Override
		public FtpletResult onDisconnect(FtpSession arg0) throws FtpException,
				IOException {
			if(commandListener!=null){
				jazmin.server.ftp.FtpSession session=new jazmin.server.ftp.FtpSession();
				session.session=arg0;
				sessionMap.remove(session.getSessionId().toString());
				Jazmin.dispatcher.invokeInPool("", 
						commandListener, 
						listenerOnDisConnectMethod,Dispatcher.EMPTY_CALLBACK,session);
			}
			return FtpletResult.DEFAULT;
		}
	}
	//--------------------------------------------------------------------------
	//
	@Override
	public void init() throws Exception {
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(FtpServerCommand.class);
		}
	}
	//
	@Override
	public void start() throws Exception {
		//
		if(userManager==null){
			throw new IllegalStateException("userManager can not be null.");
		}
		ExDefaultFtpServerContext context=new ExDefaultFtpServerContext();
		context.setFtpletContainer(new DefaultFtpletContainer(ftplets));
		factory.setDataConnectionConfiguration(
				dataConnectionConfigurationFactory.createDataConnectionConfiguration());
		context.addListener("default", factory.createListener());
		context.setUserManager(userManager);
		context.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
		server = new DefaultFtpServer(context);
		//
		server.start();
	}

	//
	@Override
	public void stop() throws Exception {
		if(server!=null){
			server.stop();
		}
	}
	//
	@Override
	public String info() {
		InfoBuilder ib=InfoBuilder.create();
		ib.section("info")
		.format("%-30s:%-30s\n")
		.print("idleTimeout",getIdleTimeout())
		.print("implicitSsl",isImplicitSsl())
		.print("serverAddress",getServerAddress())
		.print("port",getPort())
		.print("loginFailureDelay",getLoginFailureDelay())
		.print("maxAnonymousLogins",getMaxAnonymousLogins())
		.print("maxLoginFailures",getMaxLoginFailures())
		.print("maxLogins",getMaxLogins())
		.print("activeLocalAddress",getActiveLocalAddress())
		.print("activeLocalPort",getActiveLocalPort())
		.print("idleTime",getIdleTime())
		.print("passiveAddress",getPassiveAddress())
		.print("passiveExternalAddress",getPassiveExternalAddress())
		.print("passivePorts",getPassivePorts())
		.print("activeEnabled",isActiveEnabled())
		.print("activeIpCheck",isActiveIpCheck())
		.print("anonymousLoginEnabled",isAnonymousLoginEnabled())
		.print("commandListener",getCommandListener())
		.print("userManager",getUserManager());
		return ib.toString();
	}
}
