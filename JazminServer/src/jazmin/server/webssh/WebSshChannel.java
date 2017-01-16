/**
 * 
 */
package jazmin.server.webssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.webssh.ConnectionInfoProvider.ConnectionInfo;
import jazmin.util.SshUtil;


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
	ConnectionInfo connectionInfo;
	long ticket;
	//
	public WebSshChannel() {
		createTime=new Date();
		ticket=0;
	}
	//
	private static final char RECEIVE_KEY='0';
	private static final char RECEIVE_WINDOWRESIZE='1';
	//
	//
	public void startShell()throws Exception{

		logger.info("connection to {}@{}:{}", connectionInfo.user,
				connectionInfo.host, connectionInfo.port);
		shell = SshUtil.shell(connectionInfo.host, connectionInfo.port,
						connectionInfo.user, connectionInfo.password,
						sshConnectTimeout);
		shellInputStream = shell.getInputStream();
		shellOutputStream = shell.getOutputStream();
		startInputReader();
		if (connectionInfo.channelListener != null) {
			connectionInfo.channelListener.onOpen(this);
		}
		
	}
	//
	void updateTicket(){
		ticket++;
		if(connectionInfo!=null&&connectionInfo.channelListener!=null){
			connectionInfo.channelListener.onTicket(this, ticket);
		}
	}
	//
	private void startInputReader(){
		Thread inputReaderThread=new Thread(new Runnable() {
			@Override
			public void run() {
				while(shell!=null&&!shell.isClosed()){
					try {
						int n = 0;
			            byte[] buffer = new byte[4096];
			            while (-1 != (n = shellInputStream.read(buffer))) {
			            	String s=new String(buffer,0,n);
			            	receiveServerMessage(s);
			            }
					} catch (Exception e) {
						logger.catching(e);
					}
				}
				logger.info("ssh connection :"+shell+" stopped");
				channel.close();
			}
		},"WebSSHInputReader-"+connectionInfo.user+"@"+connectionInfo.host
				+":"+connectionInfo.port);
		inputReaderThread.start();
	}
	//
	private void receiveServerMessage(String s){
		if(connectionInfo.channelListener!=null){
    		connectionInfo.channelListener.onMessage(WebSshChannel.this,s);
    	}
    	sendMessageToClient(s);	
	}
	//
	public void receiveMessage(String msg){
		char command=msg.charAt(0);
		if(command==RECEIVE_KEY&&connectionInfo.enableInput){
			boolean sendToServer=true;
			if(connectionInfo.channelListener!=null){
				sendToServer=connectionInfo.channelListener.onInput(this, msg.substring(1));
			}
			if(sendToServer){
				for(int i=1;i<msg.length();i++){
					sendMessageToServer(msg.charAt(i)+"");
				}
			}else{
				//ECHO 
				for(int i=1;i<msg.length();i++){
					char c=msg.charAt(i);
					sendMessageToClient(c+"");
					if(c=='\r'){
						sendMessageToClient("\n");
					}
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
	}
	//
	public void sendMessageToServer(String cmd){
		try{
			shellOutputStream.write((cmd).getBytes());
			shellOutputStream.flush();
		}catch (Exception e) {
			logger.catching(e);
		}
	}
	//
	public void sendMessageToClient(String msg){
		messageSentCount++;
		TextWebSocketFrame frame=new TextWebSocketFrame(msg);
		channel.writeAndFlush(frame);
	}
	//
	public void closeChannel(){
		if(connectionInfo!=null&&connectionInfo.channelListener!=null){
			connectionInfo.channelListener.onClose(this);
		}
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
