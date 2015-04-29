/**
 * 
 */
package jazmin.server.sip.io.sip.header;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.header.impl.ContentTypeHeaderImpl;

/**
 * Represents the a content type header.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface ContentTypeHeader extends SipHeader, MediaTypeHeader, Parameters {

    Buffer NAME = Buffers.wrap("Content-Type");

    @Override
    ContentTypeHeader clone();

    /**
     * Frame the value as a {@link ContentTypeHeader}. This method assumes that you have already
     * parsed out the actual header name "Content-Type: ". Also, this method assumes that a message
     * framer (or similar) has framed the buffer that is being passed in to us to only contain this
     * header and nothing else.
     * 
     * Note, as with all the frame-methods on all headers/messages/whatever, they do not do any
     * validation that the information is actually correct. This method will simply only try and
     * validate just enough to get the framing done.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    public static ContentTypeHeader frame(final Buffer buffer) throws SipParseException {
        final Buffer[] mediaType = MediaTypeHeader.frame(buffer);
        return new ContentTypeHeaderImpl(mediaType[0], mediaType[1], buffer);
    }

}
