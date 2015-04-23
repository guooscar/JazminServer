/**
 * 
 */
package jazmin.server.sip.stack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class AbstractConnection implements Connection {

    // private final ChannelHandlerContext ctx;
    private final Channel channel;
    private final InetSocketAddress remote;

    /*
     * protected AbstractConnection(final ChannelHandlerContext ctx, final InetSocketAddress remote)
     * { this.ctx = ctx; this.channel = null; this.remote = remote; }
     */

    protected AbstractConnection(final Channel channel, final InetSocketAddress remote) {
        // this.ctx = null;
        this.channel = channel;
        this.remote = remote;
    }

    protected Channel channel() {
        return this.channel;
    }

    @Override
    public byte[] getRawRemoteIpAddress() {
        return this.remote.getAddress().getAddress();
    }

    @Override
    public byte[] getRawLocalIpAddress() {
        final SocketAddress local = this.channel.localAddress();
        final InetAddress address = ((InetSocketAddress) local).getAddress();
        return address.getAddress();
    }

    @Override
    public final String getLocalIpAddress() {
        final SocketAddress local = this.channel.localAddress();
        return ((InetSocketAddress) local).getAddress().getHostAddress();
    }

    @Override
    public final InetSocketAddress getRemoteAddress() {
        return this.remote;
    }

    @Override
    public final String getRemoteIpAddress() {
        return this.remote.getAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        final SocketAddress local = this.channel.localAddress();
        return ((InetSocketAddress) local).getPort();
    }

    @Override
    public int getRemotePort() {
        return this.remote.getPort();
    }

    @Override
    public boolean isUDP() {
        return false;
    }

    @Override
    public boolean isTCP() {
        return false;
    }

    @Override
    public boolean isTLS() {

        return false;
    }

    @Override
    public boolean isSCTP() {
        return false;
    }

    @Override
    public boolean isWS() {
        return false;
    }

    // protected ChannelHandlerContext getContext() {
    // return this.ctx;
    // }

    /**
     * All {@link Connection}s needs to convert the msg to a {@link ByteBuf}
     * before writing it to the {@link ChannelHandlerContext}.
     * 
     * @param msg
     *            the {@link SipMessage} to convert.
     * @return the resulting {@link ByteBuf}
     */
    protected ByteBuf toByteBuf(final SipMessage msg) {
        try {
            final Buffer b = msg.toBuffer();
            final int capacity = b.capacity() + 2;
            final ByteBuf buffer = this.channel.alloc().buffer(capacity, capacity);

            for (int i = 0; i < b.getReadableBytes(); ++i) {
                buffer.writeByte(b.getByte(i));
            }
            buffer.writeByte(SipParser.CR);
            buffer.writeByte(SipParser.LF);
            return buffer;
        } catch (final IOException e) {
            // shouldn't be possible since the underlying buffer
            // from the msg is backed by a byte-array.
            // TODO: do something appropriate other than this
            throw new RuntimeException("Unable to convert SipMessage to a ByteBuf due to IOException", e);
        }
    }

}
