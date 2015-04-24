/**
 * 
 */
package jazmin.server.sip.stack;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

import jazmin.server.sip.io.pkts.packet.sip.SipMessage;

/**
 * @author jonas@jonasborjesson.com
 */
public final class TcpConnection extends AbstractConnection {


    public TcpConnection(final Channel channel, final InetSocketAddress remote) {
        super(channel, remote);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(final SipMessage msg) {
        channel().writeAndFlush(toByteBuf(msg));
    }

    @Override
    public boolean connect() {
        return true;
    }

}
