/**
 * 
 */
package jazmin.server.sip.stack;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.net.InetSocketAddress;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.sip.SipChannel;
import jazmin.server.sip.io.sip.SipMessage;

/**
 * @author jonas@jonasborjesson.com
 */
public final class WsConnection extends AbstractConnection {
	private static Logger logger=LoggerFactory.get(WsConnection.class);

    public WsConnection(final Channel channel, final InetSocketAddress remote) {
        super(channel, remote);
    }
    @Override
    public boolean isWS() {
    	return true;
    }
    /**
     * {@inheritDoc}
     * @throws Exception 
     */
    @Override
    public void send(final SipMessage msg) throws Exception {
    	if(logger.isDebugEnabled()){
			logger.debug(">>>>>>>>>>send to {}\n{}",getRemoteAddress(),msg);
		}
    	channel().attr(SipChannel.SESSION_KEY).get().messageSentCount++;
    	TextWebSocketFrame frame=new TextWebSocketFrame(toByteBuf(msg));
        channel().writeAndFlush(frame);
    }

    @Override
    public boolean connect() {
        return true;
    }
    
}
