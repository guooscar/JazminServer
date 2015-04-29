/**
 * 
 */
package jazmin.server.sip.io.sip.header.impl;

import java.util.UUID;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.header.CallIdHeader;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class CallIdHeaderImpl extends SipHeaderImpl implements CallIdHeader {

    public CallIdHeaderImpl() {
        super(CallIdHeader.NAME, generateCallId());
    }

    public CallIdHeaderImpl(final Buffer value) {
        super(CallIdHeader.NAME, value);
    }

    public CallIdHeaderImpl(final boolean compactForm, final Buffer value) {
        super(compactForm ? CallIdHeader.COMPACT_NAME : CallIdHeader.NAME, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getCallId() {
        return getValue();
    }

    @Override
    public CallIdHeader clone() {
        try {
            return CallIdHeader.frame(getValue().clone());
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the CallId-header", e);
        }
    }

    private static final Buffer generateCallId() {
        // TODO: implement something else...
        return Buffers.wrap(UUID.randomUUID().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final Buffer value = getValue();
        final int prime = 31;
        int result = 1;
        result = prime * result + (value == null ? 0 : value.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CallIdHeaderImpl other = (CallIdHeaderImpl) obj;
        final Buffer value = getValue();
        final Buffer otherValue = other.getValue();
        if (value == null) {
            if (otherValue != null) {
                return false;
            }
        } else if (!value.equals(otherValue)) {
            return false;
        }
        return true;
    }

    @Override
    public CallIdHeader ensure() {
        return this;
    }

}
