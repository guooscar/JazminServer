/**
 * 
 */
package jazmin.server.sip.io.sip.header;

import java.util.Random;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.address.Address;
import jazmin.server.sip.io.sip.header.impl.ToHeaderImpl;


/**
 * @author jonas@jonasborjesson.com
 */
public interface ToHeader extends AddressParametersHeader {

    Buffer NAME = Buffers.wrap("To");

    /**
     * Get the tag parameter.
     * 
     * @return the tag or null if it hasn't been set.
     * @throws SipParseException
     *             in case anything goes wrong while extracting tag.
     */
    Buffer getTag() throws SipParseException;

    @Override
    ToHeader clone();

    /**
     * Frame the value as a {@link ToHeader}. This method assumes that you have already parsed out
     * the actual header name "To: ". Also, this method assumes that a message framer (or similar)
     * has framed the buffer that is being passed in to us to only contain this header and nothing
     * else.
     * 
     * Note, as with all the frame-methods on all headers/messages/whatever, they do not do any
     * validation that the information is actually correct. This method will simply only try and
     * validate just enough to get the framing done.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    public static ToHeader frame(final Buffer buffer) throws SipParseException {
        final Object[] result = AddressParametersHeader.frame(buffer);
        return new ToHeaderImpl((Address) result[0], (Buffer) result[1]);
    }

    /**
     * Generate a new tag that can be used as a tag parameter for the {@link ToHeader}. A
     * tag-parameter only has to be unique within the same Call-ID space so therefore it doesn't
     * have to be cryptographically strong etc.
     * 
     * @return
     */
    static Buffer generateTag() {
        // TODO: fix this and move it to a better place.
        return Buffers.wrap(Integer.toHexString(new Random().nextInt()));
    }

    static Builder with() {
        return new Builder();
    }

    static Builder with(final Address address) throws SipParseException {
        final Builder builder = new Builder();
        builder.address(address);
        return builder;
    }

    static class Builder extends AddressParametersHeader.Builder<ToHeader> {

        private Builder() {
            super(NAME);
        }

        @Override
        public ToHeader internalBuild(final Address address, final Buffer params) throws SipParseException {
            return new ToHeaderImpl(address, params);
        }
    }

}
