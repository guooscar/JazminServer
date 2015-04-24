/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.impl;

import java.io.IOException;
import java.io.OutputStream;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.IPPacket;
import jazmin.server.sip.io.pkts.packet.TCPPacket;
import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class TcpPacketImpl extends TransportPacketImpl implements TCPPacket {

    private final Buffer headers;

    private final Buffer options;

    private final IPPacket parent;

    /**
     * @param parent
     * @param headers
     */
    public TcpPacketImpl(final IPPacket parent, final Buffer headers, final Buffer options, final Buffer payload) {
        super(parent, Protocol.TCP, headers, payload);
        this.parent = parent;
        this.headers = headers;
        this.options = options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTCP() {
        return true;
    }

    /**
     * Get the header length in bytes
     * 
     * @return
     */
    @Override
    public int getHeaderLength() {
        // 20 because the minimum TCP header length is 20 - ALWAYS
        return 20 + (this.options != null ? this.options.capacity() : 0);
    }

    @Override
    public boolean isFIN() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x01) == 0x01;
        } catch (final Exception e) {
            // ignore, Shouldn't happen since we have already
            // framed all the bytes
            return false;
        }
    }

    @Override
    public boolean isSYN() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x02) == 0x02;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean isRST() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x04) == 0x04;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Check whether the psh (push) flag is turned on
     * 
     * @return
     */
    @Override
    public boolean isPSH() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x08) == 0x08;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean isACK() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x10) == 0x10;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean isURG() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x20) == 0x20;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean isECE() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x40) == 0x40;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean isCWR() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x80) == 0x80;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public TransportPacket clone() {
        final IPPacket parent = getParent().clone();
        final Buffer options = this.options != null ? this.options.clone() : null;
        return new TcpPacketImpl(parent, this.headers.clone(), options, getPayload().clone());
    }

    @Override
    public final void write(final OutputStream out, final Buffer payload) throws IOException {
        // TODO: options must be written out as well
        this.parent.write(out, Buffers.wrap(this.headers, payload));
    }

}
