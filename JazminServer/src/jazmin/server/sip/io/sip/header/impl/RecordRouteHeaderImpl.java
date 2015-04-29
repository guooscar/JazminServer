/**
 * 
 */
package jazmin.server.sip.io.sip.header.impl;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.address.Address;
import jazmin.server.sip.io.sip.header.RecordRouteHeader;


/**
 * @author jonas@jonasborjesson.com
 */
public class RecordRouteHeaderImpl extends AddressParametersHeaderImpl implements RecordRouteHeader {

    /**
     * @param name
     * @param address
     * @param params
     */
    public RecordRouteHeaderImpl(final Address address, final Buffer params) {
        super(RecordRouteHeader.NAME, address, params);
    }

    @Override
    public RecordRouteHeader clone() {
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        try {
            return RecordRouteHeader.frame(buffer);
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the RecordRoute-header", e);
        }
    }

    @Override
    public RecordRouteHeader ensure() {
        return this;
    }

}
