/**
 * 
 */
package jazmin.server.sip.io.pkts.filters;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.Packet;
import jazmin.server.sip.io.pkts.packet.PacketParseException;
import jazmin.server.sip.io.pkts.packet.sip.SipPacket;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SipCallIdFilter extends SipFilter {

    private final Buffer callId;

    public SipCallIdFilter(final String callId) {
        this.callId = Buffers.wrap(callId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Packet packet) throws FilterException {
        try {
            if (super.accept(packet)) {
                final SipPacket msg = (SipPacket) packet.getPacket(Protocol.SIP);
                return msg.getCallIDHeader().getValue().equals(this.callId);
            }
        } catch (final SipParseException e) {
            throw new FilterException("Unable to process the frame due to SipParseException", e);
        } catch (final IOException e) {
            throw new FilterException("Unable to process the frame due to IOException", e);
        }
        return false;
    }

    public String getCallId() {
        return this.callId.toString();
    }

}
