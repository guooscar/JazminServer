/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.header;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.address.Address;
import jazmin.server.sip.io.pkts.packet.sip.header.impl.RouteHeaderImpl;

/**
 * Source: RFC 3261 section 20.30
 * 
 * <p>
 * The Route header field is used to force routing for a request through the
 * listed set of proxies. Examples of the use of the Route header field are in
 * Section 16.12.1.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 *    Route: &lt;sip:bigbox3.site3.atlanta.com;lr&gt;,
 *           &lt;sip:server10.biloxi.com;lr&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author jonas@jonasborjesson.com
 */
public interface RouteHeader extends AddressParametersHeader {

    Buffer NAME = Buffers.wrap("Route");

    @Override
    RouteHeader clone();

    /**
     * Frame the value as a {@link RouteHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    public static RouteHeader frame(final Buffer buffer) throws SipParseException {
        final Object[] result = AddressParametersHeader.frame(buffer);
        return new RouteHeaderImpl((Address) result[0], (Buffer) result[1]);
    }

}
