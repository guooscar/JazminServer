/**
 * 
 */
package jazmin.server.sip.stack;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

import jazmin.log.LoggerFactory;
import jazmin.log.Logger;
import jazmin.server.sip.SipChannel;
import jazmin.server.sip.io.sip.SipMessage;

/**
 * @author jonas@jonasborjesson.com
 */
public class TcpConnection extends AbstractConnection {
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
