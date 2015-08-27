/**
 * 
 */
package jazmin.server.webssh;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.SshUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;


/**
 * @author yama
 *
 */
public class WebSshChannel {
	private static Logger logger=LoggerFactory.get(WebSshChannel.class);
	//
	public static final AttributeKey<WebSshChannel> SESSION_KEY=
			AttributeKey.valueOf("s");
	
	public String id;
	public int sshConnectTimeout;
	public String remoteAddress;
	public int remotePort;
	public Date createTime;
	public long messageReceivedCount=0;
	public long messageSentCount=0;
	public Channel channel;
	private ChannelShell shell;
	private OutputStream shellOutputStream;
	private InputStream shellInputStream;
	//
	public String sshHost;
	public String sshUser;
	public int sshPort;
	public String sshCmd;
	//
	public WebSshChannel() {
		createTime=new Date();
	}
	//
	private static final char RECEIVE_KEY='0';
	private static final char RECEIVE_WINDOWRESIZE='1';
	private static final char RECEIVE_LOGIN='2';
	//
	//
	private void startShell(String host,String user,int port,String pwd,String cmd){
		try {
			this.sshHost=host;
			this.sshUser=user;
			this.sshPort=port;
			this.sshCmd=cmd;
			logger.info("connection to {}@{}:{}",user,host,port);
			shell=SshUtil.shell(host,port,user,pwd,sshConnectTimeout);
			shellInputStream=shell.getInputStream();
			shellOutputStream=shell.getOutputStream();
			startInputReader();
			//
			if(cmd!=null){
				shellOutputStream.write((cmd+"\r\n").getBytes());
				shellOutputStream.flush();
			}
		} catch (Exception e) {
			logger.catching(e);
			channel.close();
		}
	}
	//
	private void startInputReader(){
		Thread inputReaderThread=new Thread(new Runnable() {
			@Override
			public void run() {
				while(!shell.isClosed()){
					try {
						int n = 0;
			            byte[] buffer = new byte[4096];
			            while (-1 != (n = shellInputStream.read(buffer))) {
			            	String s=new String(buffer,0,n);
			            	sendMessage(s);	
			            }
					} catch (Exception e) {
						logger.catching(e);
					}
				}
				logger.info("ssh connection :"+shell+" stopped");
				channel.close();
			}
		},"ProcesserInputReader-"+sshUser+"@"+sshHost+":"+sshPort+"/"+sshCmd);
		inputReaderThread.start();
	}
	//
	public void receiveMessage(String msg){
		char command=msg.charAt(0);
		if(command==RECEIVE_KEY&&sshCmd==null){
			for(int i=1;i<msg.length();i++){
				String s=msg.charAt(i)+"";
				try {
					shellOutputStream.write(s.getBytes());
					shellOutputStream.flush();
				} catch (IOException e) {
					logger.catching(e);
				}			
			}
			return;
		}
		//
		if(command==RECEIVE_WINDOWRESIZE){
			String t=msg.substring(1);
			String ss[]=t.split(",");
			if(shell instanceof com.jcraft.jsch.ChannelShell){
				shell.setPtySize(Integer.valueOf(ss[0]),Integer.valueOf(ss[1]),0,0);	
			}
			return;
		}
		//
		if(command==RECEIVE_LOGIN){
			String t=msg.substring(1);
			JSONObject loginData=JSON.parseObject(t);
			startShell(
					loginData.getString("host"), 
					loginData.getString("user"),
					loginData.getIntValue("port"),
					loginData.getString("password"),
					loginData.getString("cmd"));
			return;
		}	
	}
	//
	private void sendMessage(String msg){
		messageSentCount++;
		TextWebSocketFrame frame=new TextWebSocketFrame(msg);
		channel.writeAndFlush(frame);
	}
	//
	public void closeChannel(){
		if(shell!=null&&shell.isConnected()){
			shell.disconnect();
		}
		try {
			if(shell!=null&&shell.getSession().isConnected()){
				shell.getSession().disconnect();
			}
		} catch (JSchException e) {
			logger.catching(e);
		}
		shell=null;
	}
}
