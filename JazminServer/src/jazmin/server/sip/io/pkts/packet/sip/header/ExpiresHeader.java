package jazmin.server.sip.io.pkts.packet.sip.header;

import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.assertArgument;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.header.impl.ExpiresHeaderImpl;

public interface ExpiresHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("Expires");

    int getExpires();

    void setExpires(int expires);

    @Override
    ExpiresHeader clone();

    static ExpiresHeader create(final int expires) {
        assertArgument(expires >= 0, "The value must be greater or equal to zero");
        return new ExpiresHeaderImpl(expires);
    }

    public static ExpiresHeader frame(final Buffer buffer) throws SipParseException {
        try {
            final int value = buffer.parseToInt();
            return new ExpiresHeaderImpl(value);
        } catch (final NumberFormatException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Expires header. Value is not an integer");
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Expires header. Got an IOException", e);
        }
    }

}
