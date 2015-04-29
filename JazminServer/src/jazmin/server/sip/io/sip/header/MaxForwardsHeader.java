/**
 * 
 */
package jazmin.server.sip.io.sip.header;

import static jazmin.server.sip.io.sip.impl.PreConditions.assertArgument;

import java.io.IOException;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.header.impl.MaxForwardsHeaderImpl;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface MaxForwardsHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("Max-Forwards");

    int getMaxForwards();

    void setMaxForwards(int value);

    /**
     * Decrement the value by one. Note, there is no check whether or not the
     * value goes to zero or even below it.
     */
    void decrement();

    public static MaxForwardsHeader frame(final Buffer buffer) throws SipParseException {
        try {
            final int value = buffer.parseToInt();
            return new MaxForwardsHeaderImpl(value);
        } catch (final NumberFormatException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Max-Forwards header. Value is not an integer");
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Max-Forwards header. Got an IOException", e);
        }
    }

    @Override
    MaxForwardsHeader clone();

    static MaxForwardsHeader create(final int max) {
        assertArgument(max >= 0, "The value must be greater or equal to zero");
        return new MaxForwardsHeaderImpl(max);
    }

    /**
     * Create a new {@link MaxForwardsHeader} with a value of 70.
     * 
     * @return
     */
    static MaxForwardsHeader create() {
        return new MaxForwardsHeaderImpl(70);
    }

}
