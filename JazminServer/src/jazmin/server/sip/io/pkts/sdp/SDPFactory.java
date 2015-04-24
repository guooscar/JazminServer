/**
 * 
 */
package jazmin.server.sip.io.pkts.sdp;

import java.util.Vector;

import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.sdp.impl.SDPWrapper;


/**
 * @author jonas@jonasborjesson.com
 */
public final class SDPFactory {

    /**
     * Until we write our own parser we will use the javax.sip one.
     * 
     * The main reason I want to write a new one is so that I can make use of
     * {@link Buffer}s and to avoid some of the faults of the javax.sdp
     * interface such as the usage of {@link Vector}s (which we all know are
     * synchronized, which we really don't want to have).
     */
    private static final SdpFactory sdpFactory = SdpFactory.getInstance();

    /**
     * My singleton
     */
    private static final SDPFactory instance = new SDPFactory();

    private SDPFactory() {
        // left empty intentionally
    }

    public static SDPFactory getInstance() {
        return instance;
    }

    public SDP parse(final Buffer data) throws SdpException {
        try {
            final SessionDescription sdp = sdpFactory.createSessionDescription(data.toString());
            return new SDPWrapper(sdp);
        } catch (final javax.sdp.SdpParseException e) {
            throw new SdpParseException(e.getLineNumber(), e.getCharOffset(), e.getMessage(), e.getCause());
        }
    }

}
