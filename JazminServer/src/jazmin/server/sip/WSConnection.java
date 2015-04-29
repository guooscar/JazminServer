/**
 * 
 */
package jazmin.server.sip;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.net.InetSocketAddress;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.stack.AbstractConnection;

/**
 * @author jonas@jonasborjesson.com
 */
public final class WSConnection extends AbstractConnection {
	private static Logger logger=LoggerFactory.get(WSConnection.class);

    public WSConnection(final Channel channel, final InetSocketAddress remote) {
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
