/**
 * 
 */
package jazmin.server.sip.io.pkts.framer;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.io.pkts.packet.sip.SipPacket;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipParser;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipRequestPacketImpl;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipResponsePacketImpl;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class SIPFramer implements Framer<TransportPacket> {

    public SIPFramer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.SIP;
    }

    /**
     * {@inheritDoc}
     * 
     * Very basic way of framing a sip message. It makes a lot of assumption but
     * in the framing phase we are, well, just framing.
     */
    @Override
    public SipPacket frame(final TransportPacket parent, final Buffer buffer) throws IOException {

        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        final SipMessage sip = SipParser.frame(buffer);
        if (sip.isRequest()) {
            return new SipRequestPacketImpl(parent, sip.toRequest());
        }

        return new SipResponsePacketImpl(parent, sip.toResponse());
    }

    @Override
    public boolean accept(final Buffer data) throws IOException {
        return couldBeSipMessage(data);
    }

    /**
     * Helper function that checks whether or not the data could be a SIP message. It is a very
     * basic check but if it doesn't go through it definitely is not a SIP message.
     * 
     * @param data
     * @return
     */
    public static boolean couldBeSipMessage(final Buffer data) throws IOException {
        final byte a = data.getByte(0);
        final byte b = data.getByte(1);
        final byte c = data.getByte(2);
        return a == 'S' && b == 'I' && c == 'P' || // response
                a == 'I' && b == 'N' && c == 'V' || // INVITE
                a == 'A' && b == 'C' && c == 'K' || // ACK
                a == 'B' && b == 'Y' && c == 'E' || // BYE
                a == 'O' && b == 'P' && c == 'T' || // OPTIONS
                a == 'C' && b == 'A' && c == 'N' || // CANCEL
                a == 'M' && b == 'E' && c == 'S' || // MESSAGE
                a == 'R' && b == 'E' && c == 'G' || // REGISTER
                a == 'I' && b == 'N' && c == 'F' || // INFO
                a == 'P' && b == 'R' && c == 'A' || // PRACK
                a == 'S' && b == 'U' && c == 'B' || // SUBSCRIBE
                a == 'N' && b == 'O' && c == 'T' || // NOTIFY
                a == 'U' && b == 'P' && c == 'D' || // UPDATE
                a == 'R' && b == 'E' && c == 'F' || // REFER
                a == 'P' && b == 'U' && c == 'B'; // PUBLISH

    }

}
