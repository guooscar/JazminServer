/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.impl;

import java.io.IOException;
import java.io.OutputStream;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.IPPacket;
import jazmin.server.sip.io.pkts.packet.Packet;
import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.packet.UDPPacket;
import jazmin.server.sip.io.pkts.packet.sip.SipPacket;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * ALL packets within pkts.io must implement this {@link AbstractPacket}.
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public abstract class AbstractPacket implements Packet {

    /**
     * The protocol of this frame
     */
    private final Protocol protocol;

    /**
     * A packet may contain additional packets, which are carried within the
     * payload. Note, not all packets have payloads.
     */
    private final Buffer payload;

    /**
     * The parent packet.
     */
    private final Packet parent;

    /**
     * The next packet.
     */
    private Packet nextPacket;

    /**
     * 
     * @param p
     *            the protocol that this {@link Packet} is representing.
     * @param parent
     *            all {@link Packet} have a parent, this is it.
     * @param payload
     *            the payload of the packet, which may be null. Note, this is
     *            the actual payload of this packet. For an {@link IPPacket}
     *            that would typically be {@link TransportPacket} (such as udp
     *            or tcp). For a {@link UDPPacket}, the payload could really be
     *            anything, such as a {@link SipPacket}.
     * 
     */
    public AbstractPacket(final Protocol p, final Packet parent, final Buffer payload) {
        assert p != null;
        this.protocol = p;
        this.payload = payload;
        this.parent = parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#verify()
     */
    @Override
    public void verify() {
        // TODO Auto-generated method stub
    }

    /**
     * The write strategy is fairly simple. If we have a "nextPacket" it means
     * that we have been asked to frame our payload which also means that that
     * payload may have changed and therefore ask the nextPacket to write itself
     * back out to the stream. If there is no nextPacket we can just take the
     * raw payload and write it out as is since it cannot have changed since we
     * framed this packet.
     */
    @Override
    public final void write(final OutputStream out) throws IOException {
        if (this.nextPacket != null) {
            this.nextPacket.write(out);
        } else {
            this.write(out, this.payload);
        }
    }

    @Override
    public abstract Packet clone();

    @Override
    public Packet getParentPacket() {
        return this.parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#hasProtocol(io.pkts.protocol.Protocol)
     */
    @Override
    public boolean hasProtocol(final Protocol p) throws Exception {
        if (p == null) {
            return false;
        }

        final Packet packet = getPacket(p);
        return packet != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#getProtocol()
     */
    @Override
    public Protocol getProtocol() {
        return this.protocol;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#getPacket(io.pkts.protocol.Protocol)
     */
    @Override
    public Packet getPacket(final Protocol p) throws Exception {
        if (this.protocol == p) {
            return this;
        }

        final Packet child = getNextPacket();
        if (child != null && child.getProtocol() == p) {
            return child;
        }

        if (child == null) {
            return checkParent(p);
        }

        return child.getPacket(p);
    }

    public Packet checkParent(final Protocol p) {
        Packet current = this.parent;
        while (current != null && current.getProtocol() != p) {
            current = current.getParentPacket();
        }

        return current;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#getName()
     */
    @Override
    public String getName() {
        return this.protocol.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#getPayload()
     */
    @Override
    public Buffer getPayload() {
        if (this.payload != null) {
            return this.payload.slice();
        }
        return null;
    }

}
