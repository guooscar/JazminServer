package jazmin.server.sip.io.sip.header;

import static jazmin.server.sip.io.sip.impl.PreConditions.assertArgument;

import java.io.IOException;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.header.impl.ContentLengthHeaderImpl;

public interface ContentLengthHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("Content-Length");

    int getContentLength();

    void setContentLength(int ct);

    @Override
    ContentLengthHeader clone();

    static ContentLengthHeader create(final int contentLength) {
        assertArgument(contentLength >= 0, "The value must be greater or equal to zero");
        return new ContentLengthHeaderImpl(contentLength);
    }

    public static ContentLengthHeader frame(final Buffer buffer) throws SipParseException {
        try {
            final int value = buffer.parseToInt();
            return new ContentLengthHeaderImpl(value);
        } catch (final NumberFormatException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Expires header. Value is not an integer");
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Expires header. Got an IOException", e);
        }
    }

}
