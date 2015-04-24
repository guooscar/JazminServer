/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.header.impl;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.address.Address;
import jazmin.server.sip.io.pkts.packet.sip.header.RouteHeader;


/**
 * @author jonas@jonasborjesson.com
 */
public final class RouteHeaderImpl extends AddressParametersHeaderImpl implements RouteHeader {

    /**
     * @param name
     * @param address
     * @param params
     */
    public RouteHeaderImpl(final Address address, final Buffer params) {
        super(RouteHeader.NAME, address, params);
    }

    @Override
    public RouteHeader clone() {
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        try {
            return RouteHeader.frame(buffer);
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the Route-header", e);
        }
    }

    @Override
    public RouteHeader ensure() {
        return this;
    }

}
