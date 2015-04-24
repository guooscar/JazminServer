/**
 * 
 */
package jazmin.server.sip.io.pkts.filters;

import jazmin.server.sip.io.pkts.frame.Frame;
import jazmin.server.sip.io.pkts.packet.Packet;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface Filter {

    /**
     * Check whether this filter accepts the frame or not.
     * 
     * @param frame
     * @return
     * @throws FilterException
     *             in case something goes wrong when accessing data from the
     *             {@link Frame}. Whatever exception the {@link Frame} will
     *             throw will be wrapped in a {@link FilterException}.
     */
    boolean accept(Packet packet) throws FilterException;

}
