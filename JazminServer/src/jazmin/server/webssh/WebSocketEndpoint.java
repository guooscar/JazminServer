/**
 * 
 */
package jazmin.server.webssh;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class WebSocketEndpoint implements PeerEndpoint{
	private static Logger logger=LoggerFactory.get(WebSocketEndpoint.class);
	//
	Channel channel;
	public WebSocketEndpoint(Channel channel) {
		this.channel=channel;
	}
	//
	@Override
	public void close() {
		try {
			channel.close().sync();
		} catch (InterruptedException e) {
			logger.catching(e);
		}
	}

	@Override
	public void write(String msg) {
		TextWebSocketFrame frame=new TextWebSocketFrame(msg);
		channel.writeAndFlush(frame);
	}
	//
	@Override
	public String toString() {
		return "WS:"+channel;
	}
}
