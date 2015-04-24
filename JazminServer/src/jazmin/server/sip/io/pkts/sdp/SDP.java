/**
 * 
 */
package jazmin.server.sip.io.pkts.sdp;

import java.util.Collection;

import jazmin.server.sip.io.pkts.buffer.Buffer;

/**
 * Represents a Session Description Protocol as defined by RFC 2327.
 * 
 * Note, currently this SDP is very specific for RTP based traffic. The
 * javax.sdp implementation is very complete but its interface is a little
 * cumbersome to work with. This version primary goal is to make life easier for
 * those that deal with a lot of voip applications where RTP is the dominant
 * protocol.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface SDP {

    /**
     * For many VoIP applications, RTP is the most common protocol and this
     * convenience method extracts out all the RTP related information in the
     * SDP and presents it in one easy accessible "packet".
     * 
     * @return
     */
    Collection<RTPInfo> getRTPInfo();

    /**
     * Get the raw buffer representing this SDP.
     * 
     * @return
     */
    Buffer toBuffer();

    /**
     * Same as {@link #toBuffer()}.{@link #toString()}
     * 
     * @return
     */
    @Override
    String toString();

}
