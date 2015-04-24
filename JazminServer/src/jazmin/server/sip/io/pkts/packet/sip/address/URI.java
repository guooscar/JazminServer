/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.address;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface URI {

    /**
     * Returns the scheme of this URI, which really can be anything (see RFC3261
     * section 25.1 and the definition of absoluteURI) but most commonly will be
     * "sip", "sips" or "tel".
     * 
     * @return
     */
    Buffer getScheme();

    /**
     * Check whether this {@link URI} is a "sip" or "sips" URI.
     * 
     * @return true if this {@link URI} is a SIP URI, false otherwise.
     */
    boolean isSipURI();

    /**
     * Write the bytes of this URI into the destination buffer
     * 
     * @param dst
     */
    void getBytes(Buffer dst);

    /**
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    static URI frame(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException, IOException {
        buffer.markReaderIndex();
        final Buffer b = buffer.readBytes(3);
        buffer.resetReaderIndex();
        // not fool proof but when we parse for real we will make sure
        // that it is correct. This is good enough for us.
        if (b.getByte(0) == 's' && b.getByte(1) == 'i' && b.getByte(2) == 'p') {
            return SipURI.frame(buffer);
        } else if (b.getByte(0) == 't' && b.getByte(1) == 'e' && b.getByte(2) == 'l') {
            throw new RuntimeException("Sorry, can't do Tel URIs right now. Haven't implemented it just yet...");
        }
        throw new RuntimeException("Have only implemented SIP uri parsing right now. Sorry");
    }

    URI clone();


}
