/**
 * 
 */
package jazmin.server.webssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;

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

	String id;
	Date createTime;
	long messageReceivedCount=0;
	long messageSentCount=0;
	public PeerEndpoint endpoint;
	private ChannelShell shell;
	private OutputStream shellOutputStream;
	private InputStream shellInputStream;
	ConnectionInfo connectionInfo;
	long ticket;
	WebSshServer webSshServer;
	//
	public WebSshChannel(WebSshServer webSshServer) {
		this.webSshServer=webSshServer;
		createTime=new Date();
		ticket=0;
	}
	//
	
	//
	private static final char RECEIVE_KEY='0';
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	private static final char RECEIVE_WINDOWRESIZE='1';
	//
	
	//
	public void startShell()throws Exception{

		logger.info("connection to {}@{}:{}", connectionInfo.user,
				connectionInfo.host, connectionInfo.port);
		shell = SshUtil.shell(connectionInfo.host, connectionInfo.port,
						connectionInfo.user, connectionInfo.password,
						webSshServer.getDefaultSshConnectTimeout());
		shellInputStream = shell.getInputStream();
		shellOutputStream = shell.getOutputStream();
		startInputReader();
		try{
			if (connectionInfo.channelListener != null) {
				connectionInfo.channelListener.onOpen(this);
			}
		}catch (Exception e) {
			logger.catching(e);
			sendError(e.getMessage());
		}
		
	}
	/**
	 * @return the connectionInfo
	 */
	public ConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}
	/**
	 * @param connectionInfo the connectionInfo to set
	 */
	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}
	//
	void updateTicket(){
		ticket++;
		try{
			if(connectionInfo!=null&&connectionInfo.channelListener!=null){
				connectionInfo.channelListener.onTicket(this, ticket);
			}
		}catch (Exception e) {
			sendError(e.getMessage());
			logger.catching(e);
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
						sendError(e.getMessage());
						logger.catching(e);
					}
				}
				logger.info("ssh connection :"+shell+" stopped");
				webSshServer.removeChannel(id);
				endpoint.close();
				
			}
		},"WebSSHInputReader-"+connectionInfo.user+"@"+connectionInfo.host
				+":"+connectionInfo.port);
		inputReaderThread.start();
	}
	//
	public void sendError(String error){
		//show red alert info to client
		String rsp=
				"\033[31m\n\r**************************************************\n\r"+
				"\n\rerror:"+error+"\n\r"+
				"\n\r**************************************************\n\r\033[0m";
		sendMessageToClient(rsp);
	}
	//
	private void receiveServerMessage(String s){
		if(connectionInfo.channelListener!=null){
    		connectionInfo.channelListener.onMessage(WebSshChannel.this,s);
    	}
    	sendMessageToClient(s);	
	}
	//
	StringBuilder inputBuffer=new StringBuilder();
	//
	public void receiveMessage(String msg){
		messageReceivedCount++;
		try{
			receiveMessage0(msg);
		}catch (Exception e) {
			logger.catching(e);
			sendError(e.getMessage());
		}
	}
	private void receiveMessage0(String msg){
		char command=msg.charAt(0);
		if(command==RECEIVE_KEY&&connectionInfo.enableInput){
			boolean sendToServer=true;
			if(connectionInfo.channelListener!=null){
				sendToServer=connectionInfo.channelListener.inputSendToServer();
			}
			if(sendToServer){
				for(int i=1;i<msg.length();i++){
					sendMessageToServer(msg.charAt(i)+"");
				}
			}else{
				//hook input mode
				//ECHO 
				for(int i=1;i<msg.length();i++){
					char c=msg.charAt(i);
					sendMessageToClient(c+"");
					switch (c) {
					case 127://del
						if(inputBuffer.length()>0){
							sendMessageToClient(((char)0x08)+"\033[J");//BS
							inputBuffer.deleteCharAt(inputBuffer.length()-1);		
						}
						break;
					case '\r':
						sendMessageToClient("\n");
						if(connectionInfo.channelListener!=null){
							connectionInfo.channelListener.onInput(this, inputBuffer.toString().trim());
						}
						inputBuffer.delete(0,inputBuffer.length());
					default:
						inputBuffer.append(c);
						break;
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
		endpoint.write(msg);
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
		webSshServer.removeChannel(id);
		shell=null;
	}
	
}
