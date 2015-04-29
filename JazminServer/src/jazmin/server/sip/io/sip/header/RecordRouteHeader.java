/**
 * 
 */
package jazmin.server.sip.io.sip.header;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.address.Address;
import jazmin.server.sip.io.sip.header.impl.RecordRouteHeaderImpl;

/**
 * 
 * Source: RFC 3261 section 20.30
 * 
 * <p>
 * The Record-Route header field is inserted by proxies in a request to force
 * future requests in the dialog to be routed through the proxy.
 * </p>
 * 
 * <p>
 * Examples of its use with the Route header field are described in Sections
 * 16.12.1.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 *    Record-Route: &lt;sip:server10.biloxi.com;lr&gt;,
 *                  &lt;sip:bigbox3.site3.atlanta.com;lr&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author jonas@jonasborjesson.com
 */
public interface RecordRouteHeader extends AddressParametersHeader {

    Buffer NAME = Buffers.wrap("Record-Route");

    @Override
    RecordRouteHeader clone();


    /**
     * Frame the value as a {@link RecordRouteHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    public static RecordRouteHeader frame(final Buffer buffer) throws SipParseException {
        final Object[] result = AddressParametersHeader.frame(buffer);
        return new RecordRouteHeaderImpl((Address) result[0], (Buffer) result[1]);
    }

}
