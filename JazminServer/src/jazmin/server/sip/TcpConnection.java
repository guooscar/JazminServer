/**
 * 
 */
package jazmin.server.sip;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

import jazmin.log.LoggerFactory;
import jazmin.log.Logger;
import jazmin.server.sip.io.sip.SipMessage;
import jazmin.server.sip.stack.AbstractConnection;

/**
 * @author jonas@jonasborjesson.com
 */
public final class TcpConnection extends AbstractConnection {
	private static Logger logger=LoggerFactory.get(TcpConnection.class);

    public TcpConnection(final Channel channel, final InetSocketAddress remote) {
        super(channel, remote);
    }
    @Override
    public boolean isTCP() {
    	return true;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void send(final SipMessage msg)throws Exception{
    	if(logger.isDebugEnabled()){
			logger.debug(">>>>>>>>>>send to {}\n{}",getRemoteAddress(),msg);
		}
    	channel().attr(SipChannel.SESSION_KEY).get().messageSentCount++;
        channel().writeAndFlush(toByteBuf(msg));
    }

    @Override
    public boolean connect() {
        return true;
    }
    
}
