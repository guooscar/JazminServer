/**
 * 
 */
package jazmin.server.webssh;

/**
 * @author yama
 *
 */
public class SendCommandChannelRobot extends ChannelRobot{
	String cmd;
	public SendCommandChannelRobot(String cmd) {
		this.cmd=cmd;
	}
	@Override
	public void onOpen(WebSshChannel channel) {
		channel.sendMessageToServer(cmd+"\r\n");
	}
}
