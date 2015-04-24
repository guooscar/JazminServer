/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.header;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipParser;

/**
 * Interface for those headers representing a media type, such as the
 * {@link ContentTypeHeader}
 * 
 * @author jonas@jonasborjesson.com
 */
public interface MediaTypeHeader extends SipHeader {

    /**
     * 
     * @return
     */
    Buffer getContentType();

    /**
     * 
     * @return
     */
    Buffer getContentSubType();

    /**
     * Convenience method for checking whether the media type is
     * "application/sdp"
     * 
     * @return
     */
    boolean isSDP();


    /**
     * Convenience method for parsing out a media type header.
     * 
     * @param buffer
     * @return
     * @throws SipParseException
     */
    public static Buffer[] frame(final Buffer buffer) throws SipParseException {
        if (buffer == null) {
            throw new SipParseException(0, "Cannot parse a null-buffer. Cmon!");
        }

        final Buffer mType = SipParser.consumeMType(buffer);
        if (mType == null) {
            throw new SipParseException(buffer.getReaderIndex(), "Expected m-type but got nothing");
        }
        SipParser.expectSLASH(buffer);
        final Buffer subType = SipParser.consumeMSubtype(buffer);
        if (subType == null) {
            throw new SipParseException(buffer.getReaderIndex(), "Expected m-subtype but got nothing");
        }
        return new Buffer[] {mType, subType};
    }

}
