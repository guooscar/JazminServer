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

import com.jcraft.jsch.ChannelShell;


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
	public String localAddress;
	public int localPort;
	public String remoteAddress;
	public int remotePort;
	public Date createTime;
	public long messageReceivedCount=0;
	public long messageSentCount=0;
	public Channel channel;
	private ChannelShell shell;
	private OutputStream shellOutputStream;
	private InputStream shellInputStream;
	public WebSshChannel() {
		createTime=new Date();
	}
	//
	private static final char RECEIVE_KEY='0';
	private static final char RECEIVE_WINDOWRESIZE='1';
	//
	private static final char SEND_DATA='0';
	private static final char SEND_TITLE='1';
	//
	public void startProcess(){
		try {
			shell=SSHUtil.execute("localhost",22, "yama","77585211");
			shell.setPty(true);
			shell.connect(5000);
			shellInputStream=shell.getInputStream();
			shellOutputStream=shell.getOutputStream();
			startInputReader();
			sendMessage(SEND_TITLE+""+"localhost");
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
			            	sendMessage(SEND_DATA+""+s);	
			            }
					} catch (Exception e) {
						logger.catching(e);
					}
				}
				logger.info("process :"+shell+" stopped");
				channel.close();
			}
		},"ProcesserInputReader-"+id);
		inputReaderThread.start();
	}
	//
	public void receiveMessage(String msg){
		char command=msg.charAt(0);
		if(command==RECEIVE_KEY){
			for(int i=1;i<msg.length();i++){
				String s=msg.charAt(i)+"";
				try {
					shellOutputStream.write(s.getBytes());
					shellOutputStream.flush();
				} catch (IOException e) {
					logger.catching(e);
				}			
			}
		}
		//
		if(command==RECEIVE_WINDOWRESIZE){
			String t=msg.substring(1);
			String ss[]=t.split(",");
			shell.setPtySize(Integer.valueOf(ss[0]),Integer.valueOf(ss[1]),0,0);
		}
	}
	//
	private void sendMessage(String msg){
		logger.debug("send data {}",msg);
		TextWebSocketFrame frame=new TextWebSocketFrame(msg);
		channel.writeAndFlush(frame);
	}
}
