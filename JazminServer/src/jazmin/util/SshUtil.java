/**
 * 
 */
package jazmin.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.function.BiConsumer;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * @author yama 7 Jan, 2015
 */
public class SshUtil {
	public static  class MyUserInfo implements UserInfo,
			UIKeyboardInteractive {
		public String getPassword() {
			return null;
		}

		public boolean promptYesNo(String str) {
			return true;
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
	public static ChannelShell  shell(
			String host, 
			int port, 
			String user, 
			String pwd) throws Exception {
		return shell(host, port, user, pwd,10000);
	}
	//
	public static ChannelShell  shell(
			String host, 
			int port, 
			String user, 
			String pwd,
			int connectTimeout) throws Exception {
		JSch jsch = new JSch();
		Session session=jsch.getSession(user, host, port);
		Properties config = new java.util.Properties(); 
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
	    session.setPassword(pwd);
	    UserInfo ui = new MyUserInfo();
	    session.setUserInfo(ui);
		session.connect(connectTimeout);
		ChannelShell channel = (ChannelShell) session.openChannel("shell");
		channel.connect(connectTimeout);
		return channel;
	}
	//
	public static ChannelExec execute(
			String host, 
			int port, 
			String user, 
			String pwd,
			String cmd,
			int timeout) throws Exception {
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, port);
		session.setPassword(pwd);
		Properties config = new java.util.Properties(); 
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		UserInfo ui = new MyUserInfo();
		session.setUserInfo(ui);
		session.connect(timeout);
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setInputStream(null);
		channel.setCommand(cmd);
		channel.connect(timeout);
		return channel;
	}
	//
	public static int execute(
			String host, 
			int port, 
			String user, 
			String pwd,
			String cmd,
			BiConsumer<String,String>callback) throws Exception {
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, port);
		session.setPassword(pwd);
		Properties config = new java.util.Properties(); 
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect();
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(cmd);
		channel.setInputStream(null);
		InputStream in = channel.getInputStream();
		InputStream err = channel.getErrStream();
		channel.connect();
		int exitCode=0;
		String outResult="";
		String errResult="";
		while (true) {
			outResult=getResult(in);
			errResult=getResult(err);
			if (channel.isClosed()) {
				if (in.available() > 0)
					continue;
				exitCode=channel.getExitStatus();
				break;
			}
			try {
				Thread.sleep(100);
			} catch (Exception ee) {}
		}
		channel.disconnect();
		session.disconnect();
		callback.accept(outResult,errResult);
		return exitCode;
	}
	//
	private static String getResult(InputStream in)throws Exception{
		byte[] tmp = new byte[1024];
		StringBuilder resultOut=new StringBuilder();
		while (in.available() > 0) {
			int i = in.read(tmp, 0, 1024);
			if (i < 0)
				break;
			resultOut.append(new String(tmp, 0, i));
		}
		return resultOut.toString();
	}
	//
	public static void main(String[] args) throws Exception {
		//shell("localhost",2222, "appadmin","");
		/*execute("localhost",22, "yama","77585211","top",new BiConsumer<String, String>() {
			@Override
			public void accept(String t, String u) {
				System.err.println(t);
			}
		});*/
		Channel c=execute("localhost",22, "yama","77585211","top",1000);
		InputStream in=c.getInputStream();
		byte[] tmp = new byte[1024];
		while (true) {
			int i = in.read(tmp, 0, 1024);
			if (i < 0)
				break;
			System.err.println(new String(tmp, 0, i));
		}
		
	}
}
