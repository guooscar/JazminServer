/**
 * 
 */
package jazmin.test.server.ftp;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleServer;
import jazmin.server.ftp.FTPServer;
import jazmin.server.ftp.FTPUserInfo;
import jazmin.server.ftp.SimpleAuthCallback;
import jazmin.server.ftp.SimpleUserManager;

/**
 * @author g2131
 *
 */
public class TestSimpleAuthFTPServer {

	//
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		Jazmin.start();
		FTPServer server=new FTPServer();
		server.setPort(2221);
		//
		server.setUserManager(new SimpleUserManager(new SimpleCallback()));
		//
		Jazmin.addServer(server);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
	//
	static class SimpleCallback implements SimpleAuthCallback{

		@Override
		public FTPUserInfo authenticate(String user, String password)
				throws Exception {
			System.err.println("authenticate");
			if(user.equals("admin")&&password.equals("123")){
				FTPUserInfo uu=new FTPUserInfo();
				uu.userName="admin";
				uu.homeDirectory=".";
				return uu;
			}else{
				return null;
			}
		}
		//
		@Override
		public FTPUserInfo getUserByName(String user) throws Exception {
			System.err.println("getUserByName");
			FTPUserInfo uu=new FTPUserInfo();
			uu.userName="admin";
			uu.homeDirectory=".";
			return uu;
		}
		
	}
	//
}
