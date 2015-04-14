/**
 * 
 */
package jazmin.test.server.ftp;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleServer;
import jazmin.server.ftp.CommandAdapter;
import jazmin.server.ftp.FTPReply;
import jazmin.server.ftp.FTPRequest;
import jazmin.server.ftp.FTPServer;
import jazmin.server.ftp.FTPSession;
import jazmin.server.ftp.FTPUserInfo;
import jazmin.server.ftp.FTPPropUserManager;

/**
 * @author g2131
 *
 */
public class TestFTPServer {

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
		admin.homeDirectory="d:/ftp";
		admin.userPassword="202CB962AC59075B964B07152D234B70";//123
		//admin.homedirectory="/";
		FTPPropUserManager userManager=new FTPPropUserManager("admin");
		userManager.addUser(admin);
		server.setUserManager(userManager);
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
