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
import jazmin.core.aop.Dispatcher;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;
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
	private FtpServerFactory serverFactory ;
	private LinkedHashMap<String,Ftplet>ftplets;
	private CommandListener commandListener;
	private FTPStatistics statistics;
	private Map<String,FileTransferInfo>fileTransferInfos;
	private Method listenerBeforeMethod;
	private Method listenerAfterMethod;
	private Method listenerOnConnectMethod;
	private Method listenerOnDisConnectMethod;
	private FTPUserManager userManager;
	//
	public FTPServer() {
		serverFactory=new FtpServerFactory();
		factory = new ListenerFactory();
		ssl = new SslConfigurationFactory();
		ftplets=new LinkedHashMap<String, Ftplet>();
		ftplets.put("EX",new ServiceFtplet());
		fileTransferInfos=new ConcurrentHashMap<String, FileTransferInfo>();
		listenerBeforeMethod=Dispatcher.getMethod(
				CommandListener.class,
				"beforeCommand",FTPSession.class, FTPRequest.class);
		listenerAfterMethod=Dispatcher.getMethod(
				CommandListener.class,
				"afterCommand",FTPSession.class, FTPRequest.class,FTPReply.class);
		listenerOnConnectMethod=Dispatcher.getMethod(
				CommandListener.class,
				"onConnect",FTPSession.class);
		listenerOnDisConnectMethod=Dispatcher.getMethod(
				CommandListener.class,
				"onDisconnect",FTPSession.class);
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
	public FTPStatistics getStatistics() {
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
	public FTPUserManager getUserManager() {
		return userManager;
	}
	/**
	 * set user manager
	 * @param userManager
	 */
	public void setUserManager(FTPUserManager userManager) {
		if(isInited()){
			throw new IllegalStateException("set before inited.");
		}
		this.userManager = userManager;
	}

	//--------------------------------------------------------------------------
	
	private class ServiceFtplet implements Ftplet{
		@Override
		public FtpletResult afterCommand(FtpSession arg0, FtpRequest arg1,
				FtpReply arg2) throws FtpException, IOException {
			if(commandListener!=null){
				FTPSession session=new FTPSession();
				FTPRequest request=new FTPRequest();
				FTPReply reply=new FTPReply();
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
						listenerAfterMethod,null,session,request,reply);
			}
			return FtpletResult.DEFAULT;
		}

		@Override
		public FtpletResult beforeCommand(FtpSession arg0, FtpRequest arg1)
				throws FtpException, IOException {
			if(commandListener!=null){
				FTPSession session=new FTPSession();
				FTPRequest request=new FTPRequest();
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
						listenerBeforeMethod,null,session,request);
			}
			return FtpletResult.DEFAULT;
		}

		@Override
		public void destroy() {
			
		}

		@Override
		public void init(FtpletContext ctx) throws FtpException {
			statistics=new FTPStatistics();
			statistics.statistics=ctx.getFtpStatistics();
		}

		@Override
		public FtpletResult onConnect(FtpSession arg0) throws FtpException,
				IOException {
			if(commandListener!=null){
				FTPSession session=new FTPSession();
				session.session=arg0;
				Jazmin.dispatcher.invokeInPool("", 
						commandListener, 
						listenerOnConnectMethod,null,session);
			}
			return FtpletResult.DEFAULT;
		}

		@Override
		public FtpletResult onDisconnect(FtpSession arg0) throws FtpException,
				IOException {
			if(commandListener!=null){
				FTPSession session=new FTPSession();
				session.session=arg0;
				Jazmin.dispatcher.invokeInPool("", 
						commandListener, 
						listenerOnDisConnectMethod,null,session);
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
			cs.registerCommand(new FTPServerCommand());
		}
	}
	//
	@Override
	public void start() throws Exception {
		//
		serverFactory.setFtplets(ftplets);
		serverFactory.addListener("default", factory.createListener());
		serverFactory.setUserManager(userManager);
		server = serverFactory.createServer(); 
		//
		server.start();
	}

	//
	@Override
	public void stop() throws Exception {
		server.stop();
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
		.print("commandListener",getCommandListener())
		.print("userManager",getUserManager());
		return ib.toString();
	}
}
