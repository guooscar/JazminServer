/**
 * 
 */
package jazmin.server.sip.io.sip.header;

import static jazmin.server.sip.io.sip.impl.PreConditions.assertNotEmpty;
import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.impl.SipParser;


/**
 * <p>
 * Represents any header in SIP.
 * </p>
 * 
 * <p>
 * All {@link SipHeader}s in this API are created through a set of factory methods as well as
 * through builders. Any header that is simple in nature, as in it only contains a single value,
 * such as the {@link MaxForwardsHeader}, can be created directly through a <code>create</code>
 * method on the the corresponding interface of that header. Headers that are constructed through
 * many arguments, or have ambiguous arguments, are created through builders. Finally, all headers
 * have a <code>frame</code> method that takes a {@link Buffer} and will attempt to frame the
 * content into a specific header. Note it <i>frames</i> the header and as such does not verify
 * every aspect of the header since speed is important.
 * </p>
 * 
 * <p>
 * Example: create a simple header directly, such as the {@link MaxForwardsHeader}.
 * 
 * <pre>
 * MaxForwardsHeader header = MaxForwardsHeader.create(20);
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * Example: create a header using the builder-pattern, such as a {@link ToHeader}.
 * 
 * <pre>
 * ToHeader header = ToHeader.with().user(&quot;alice&quot;).host(&quot;example.com&quot;).build();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * Note, by default most things are done lazily in order to speed things up. As such, you may
 * successfully construct a header but it may in fact miss important information. If you are
 * building an application where you want to be 100% sure that a header is correct according to the
 * BNF in rfc 3261 then call {@link #verify()}.
 * </p>
 * 
 * @author jonas@jonasborjesson.com
 */
public interface SipHeader extends Cloneable {

    /**
     * Get the name of the header
     * 
     * @return
     */
    Buffer getName();

    /**
     * Get the value of the buffer
     * 
     * @return
     */
    Buffer getValue();

    void verify() throws SipParseException;

    void getBytes(Buffer dst);

    SipHeader clone();

    /**
     * Create a new {@link SipHeader} based on the buffer. Each {@link SipHeader} will override this
     * factory method to parse the header into a more specialized header.
     * 
     * Note, the header returned really is a {@link SipHeader} and is NOT e.g. a {@link ToHeader}.
     * If you really need to parse it as a {@link ToHeader} you should use the
     * 
     * @param header the raw header
     * @return a new {@link SipHeader}.
     * @throws SipParseException in case the header is not a correct formatted header.
     */
    static SipHeader frame(final Buffer value) throws SipParseException {
        assertNotEmpty(value, "The value of the header cannot be null or the empty buffer");
        return SipParser.nextHeader(value);
    }

    /**
     * As most things in this library are done lazily, such as framing headers, you can make sure
     * that a particular header has indeed been parsed to the more specific header type by calling
     * this method. If the header has yet not been parsed fully, e.g., we may have extracted out a
     * {@link ContactHeader} but it is still in its "raw" form and therefore represented as a
     * {@link SipHeader} as opposed to an actual {@link ContactHeader} but by calling this method
     * you will force the library to actually fully frame it.
     * 
     * @return
     */
    SipHeader ensure();

}
