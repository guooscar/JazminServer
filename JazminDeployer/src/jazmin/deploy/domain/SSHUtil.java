/**
 * 
 */
package jazmin.deploy.domain;

import java.io.InputStream;
import java.util.function.BiConsumer;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * @author yama 7 Jan, 2015
 */
public class SSHUtil {
	static class SimpleUserInfo implements UserInfo {
		String password;

		public SimpleUserInfo(String password) {
			this.password = password;
		}
		@Override
		public String getPassphrase() {
			return null;
		}
		@Override
		public String getPassword() {
			return password;
		}
		@Override
		public boolean promptPassphrase(String arg0) {
			return false;
		}
		@Override
		public boolean promptPassword(String arg0) {
			return true;
		}
		@Override
		public boolean promptYesNo(String arg0) {
			return true;
		}
		@Override
		public void showMessage(String arg0) {
		}
	}
	//--------------------------------------------------------------------------
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
		session.setUserInfo(new SimpleUserInfo(pwd));
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
		execute("192.168.1.61", 22, "appadmin", "appadmin", 
				"/home/appadmin/jazmin_server/jazmin startbg SanGuoWebSystem",(out,err)->{
			System.out.println("out:"+out+"/");
			System.err.println("err:"+err+"/");
		});
	}
}
