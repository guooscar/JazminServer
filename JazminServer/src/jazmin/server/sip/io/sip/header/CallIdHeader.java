/**
 * 
 */
package jazmin.server.sip.io.sip.header;

import static jazmin.server.sip.io.sip.impl.PreConditions.assertNotEmpty;
import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.header.impl.CallIdHeaderImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface CallIdHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("Call-ID");

    /**
     * The compact name of the Call-ID header is 'i'
     */
    Buffer COMPACT_NAME = Buffers.wrap("i");

    Buffer getCallId();

    @Override
    CallIdHeader clone();

    static CallIdHeader frame(final Buffer buffer) {
        assertNotEmpty(buffer, "The value of the Call-ID cannot be null or empty");
        return new CallIdHeaderImpl(buffer);
    }


    /**
     * Frame the {@link CallIdHeader} using its compact name.
     * 
     * @param compactForm
     * @param buffer
     * @return
     * @throws SipParseException
     */
    public static CallIdHeader frameCompact(final Buffer buffer) throws SipParseException {
        assertNotEmpty(buffer, "The value of the Call-ID cannot be null or empty");
        return new CallIdHeaderImpl(true, buffer);
    }

    static CallIdHeader create() {
        return new CallIdHeaderImpl();
    }

}
