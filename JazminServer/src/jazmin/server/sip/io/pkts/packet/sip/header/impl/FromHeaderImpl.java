/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.header.impl;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.address.Address;
import jazmin.server.sip.io.pkts.packet.sip.header.FromHeader;


/**
 * @author jonas@jonasborjesson.com
 */
public class FromHeaderImpl extends AddressParametersHeaderImpl implements FromHeader {

    /**
     * @param name
     * @param address
     * @param params
     */
    public FromHeaderImpl(final Address address, final Buffer params) {
        super(FromHeader.NAME, address, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getTag() throws SipParseException {
        return getParameter(TAG);
    }

    @Override
    public FromHeader clone() {
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        try {
            return FromHeader.frame(buffer);
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the From-header", e);
        }
    }

    @Override
    public FromHeader ensure() {
        return this;
    }

}
