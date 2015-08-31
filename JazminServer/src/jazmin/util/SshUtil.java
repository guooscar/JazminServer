/**
 * 
 */
package jazmin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.function.BiConsumer;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

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
	private static Logger logger=LoggerFactory.get(SshUtil.class);
	//
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
		if(logger.isDebugEnabled()){
			logger.debug("create shell {}@{}:{}",user,host,port);
		}
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
		if(logger.isDebugEnabled()){
			logger.debug("exec {}@{}:{} {}",user,host,port,cmd);
		}
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
			int connectTimeout,
			BiConsumer<String,String>callback) throws Exception {
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, port);
		if(logger.isDebugEnabled()){
			logger.debug("exec {}@{}:{} {}",user,host,port,cmd);
		}
		session.setPassword(pwd);
		Properties config = new java.util.Properties(); 
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect(connectTimeout);
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(cmd);
		channel.setInputStream(null);
		InputStream in = channel.getInputStream();
		InputStream err = channel.getErrStream();
		channel.connect(connectTimeout);
		int exitCode=0;
		String outResult="";
		String errResult="";
		long totalExecuteTime=0;
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
			totalExecuteTime+=100;
			//
			if(totalExecuteTime>=10000){
				break;
			}
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
	//
	public static void scp(
			String host, 
			int port, 
			String user, 
			String pwd,
			String localFile,
			String remoteFile,
			int connectTimeout,
			BiConsumer<Long,Long>callback) throws Exception {
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, port);
		if(logger.isDebugEnabled()){
			logger.debug("scp {}@{}:{} {} -> {}",user,host,port,localFile,remoteFile);
		}
		session.setPassword(pwd);
		Properties config = new java.util.Properties(); 
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect(connectTimeout);
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand("scp -t "+remoteFile);
		channel.setInputStream(null);
		InputStream in = channel.getInputStream();
		OutputStream out = channel.getOutputStream();
		channel.connect(connectTimeout);
		checkAck(in);
		File lFile = new File(localFile);
		long filesize=lFile.length();
		String   command="C0644 "+filesize+" ";
		if (localFile.lastIndexOf('/') > 0) {
			command += localFile.substring(localFile.lastIndexOf('/') + 1);
		} else {
			command += localFile;
		}
		command += "\n";
		out.write(command.getBytes());
		out.flush();
		checkAck(in);
		//
		FileInputStream fis = new FileInputStream(lFile);
		byte[] buf = new byte[1024*500];
		long sendLen=0;
		while (true) {
			int len = fis.read(buf, 0, buf.length);
			sendLen+=len;
			callback.accept(filesize,sendLen);
			if (len <= 0)
				break;
			out.write(buf, 0, len); // out.flush();
		}
		fis.close();
		fis = null;
		// send '\0'
		buf[0] = 0;
		out.write(buf, 0, 1);
		out.flush();
		checkAck(in);
		out.close();
		channel.disconnect();
		session.disconnect();
		//
	} 
	//
	static void checkAck(InputStream in) throws Exception {
		int b = in.read();
		if (b == 0) {
			return;
		}
		if (b == -1) {
			 throw new Exception("read -1");
		}
		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1||b==2) { // error
				throw new Exception(sb.toString());
			}
		}
	}
	//
	public static void main(String[] args) throws Exception {
		
	}
}
