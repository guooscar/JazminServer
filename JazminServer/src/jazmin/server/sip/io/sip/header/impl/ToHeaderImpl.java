/**
 * 
 */
package jazmin.server.sip.io.sip.header.impl;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.address.Address;
import jazmin.server.sip.io.sip.header.ToHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public final class ToHeaderImpl extends AddressParametersHeaderImpl implements ToHeader {

    /**
     * 
     */
    public ToHeaderImpl(final Address address, final Buffer parametersBuffer) {
        super(ToHeader.NAME, address, parametersBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getTag() throws SipParseException {
        return getParameter(TAG);
    }


    @Override
    public ToHeader clone() {
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        try {
            return ToHeader.frame(buffer);
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the To-header", e);
        }
    }

    @Override
    public ToHeader ensure() {
        return this;
    }

}
