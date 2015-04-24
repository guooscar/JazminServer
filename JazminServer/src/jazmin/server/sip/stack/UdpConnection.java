package jazmin.server.sip.stack;

import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;

/**
 * Encapsulates a
 * 
 * @author jonas@jonasborjesson.com
 */
public final class UdpConnection extends AbstractConnection {
	private static Logger logger=LoggerFactory.get(UdpConnection.class);
    // public UdpConnection(final ChannelHandlerContext ctx, final InetSocketAddress remoteAddress)
    // {
    // super(ctx, remoteAddress);
    // }

    public UdpConnection(final Channel channel, final InetSocketAddress remoteAddress) {
        super(channel, remoteAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUDP() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(final SipMessage msg) {
    	if(logger.isDebugEnabled()){
			logger.debug(">>>>>>>>>>\n{}",msg);
		}
        final DatagramPacket pkt = new DatagramPacket(toByteBuf(msg), getRemoteAddress());
        channel().writeAndFlush(pkt);
    }

    @Override
    public boolean connect() {
        return true;
    }

}
