/**
 * 
 */
package jazmin.server.webssh;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * @author yama 7 Jan, 2015
 */
public class SSHUtil {
	public static abstract class MyUserInfo implements UserInfo,
			UIKeyboardInteractive {
		public String getPassword() {
			return null;
		}

		public boolean promptYesNo(String str) {
			return false;
		}

		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return false;
		}

		public boolean promptPassword(String message) {
			return false;
		}

		public void showMessage(String message) {
		}

		public String[] promptKeyboardInteractive(String destination,
				String name, String instruction, String[] prompt, boolean[] echo) {
			return null;
		}
	}
	//--------------------------------------------------------------------------
	//
	public static ChannelShell  execute(
			String host, 
			int port, 
			String user, 
			String pwd) throws Exception {
		JSch jsch = new JSch();
		Session session=jsch.getSession(user, host, 22);
	    session.setPassword(pwd);
	    UserInfo ui = new MyUserInfo(){
	        public boolean promptYesNo(String message){
	        	return true;
	        }
	      };
	    session.setUserInfo(ui);
		session.connect();
		ChannelShell channel = (ChannelShell) session.openChannel("shell");
		return channel;
	}
}
