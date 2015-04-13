/**
 * 
 */
package jazmin.server.ftp;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
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
	private static Logger logger=LoggerFactory.get(FTPServer.class);
	//
	private FtpServer server;
	private ListenerFactory factory;
	private SslConfigurationFactory ssl;
	private FTPUserManager userManager;
	private FtpServerFactory serverFactory ;
	private LinkedHashMap<String,Ftplet>ftplets;
	private CommandListener commandListener;
	private FTPStatistics statistics;
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
	 * set ftp server admin user name
	 * @param adminName admin user name
	 */
	public void setAdminUser(String adminName){
		userManager=new FTPUserManager(adminName);
	}
	/**
	 * return ftp server admin user name
	 * @return admin user name
	 */
	public String getAdminUser(){
		return userManager.getAdminName();
	}
	/**
	 * add new ftp user
	 * @param user the new user
	 * @throws FTPException
	 */
	public void addUser(FTPUserInfo user) throws FTPException{
		userManager.addUser(user);
	}
	/**
	 * return all ftp user names
	 * @return
	 */
	public String[] getAllUserNames(){
		try {
			return serverFactory.getUserManager().getAllUserNames();
		} catch (FtpException e) {
			logger.catching(e);
			return new String[]{};
		}
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
	//
	
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
				try {
					commandListener.afterCommand(session, request, reply);
				} catch (Exception e) {
					logger.catching(e);
				}
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
				try {
					commandListener.beforeCommand(session, request);
				} catch (Exception e) {
					logger.catching(e);
				}
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
				try {
					commandListener.onConnect(session);
				} catch (Exception e) {
					logger.catching(e);
				}
			}
			return FtpletResult.DEFAULT;
		}

		@Override
		public FtpletResult onDisconnect(FtpSession arg0) throws FtpException,
				IOException {
			if(commandListener!=null){
				FTPSession session=new FTPSession();
				session.session=arg0;
				try {
					commandListener.onDisconnect(session);
				} catch (Exception e) {
					logger.catching(e);
				}
			}
			return FtpletResult.DEFAULT;
		}
	}
	//--------------------------------------------------------------------------
	//
	public FTPServer() {
		serverFactory=new FtpServerFactory();
		factory = new ListenerFactory();
		ssl = new SslConfigurationFactory();
		userManager=new FTPUserManager("admin");
		ftplets=new LinkedHashMap<String, Ftplet>();
		ftplets.put("EX",new ServiceFtplet());
	}
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
		.print("adminUser",getAdminUser())
		.print("port",getPort())
		.print("commandListener",getCommandListener());
		return ib.toString();
	}
	//
	//
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		Jazmin.start();
		FTPServer server=new FTPServer();
		server.setPort(2221);
		//
		FTPUserInfo admin=new FTPUserInfo();
		admin.userName="admin";
		admin.homeDirectory="/Users/yama/Desktop/ftp";
		admin.userPassword="202CB962AC59075B964B07152D234B70";//123
		//admin.homedirectory="/";
		server.addUser(admin);
		//
		server.setCommandListener(new CommandAdapter() {
			@Override
			public void afterCommand(FTPSession session, FTPRequest req,
					FTPReply reply) throws Exception {
				System.out.println(session.getUser()+"/"
					+req.getRequestLine()
					+" /"+reply.getCode()
					+"/"+reply.getMessage());
			}
		});
		//
		Jazmin.addServer(server);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}

}
