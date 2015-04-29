/**
 * 
 */
package jazmin.server.sip.io.sip;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.sip.header.ViaHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public interface SipResponse extends SipMessage {

    /**
     * Get the status code of this SIP response
     * 
     * @return
     */
    int getStatus();
    
    /**
     * Get the reason phrase of this {@link SipResponse}
     * 
     * @return
     */
    Buffer getReasonPhrase();

    /**
     * Convenience method for checking whether this is a 1xx response or not.
     * 
     * @return
     */
    boolean isProvisional();

    /**
     * Convenience method for checking whether this response is a final response, i.e. any response
     * >= 200.
     * 
     * @return
     */
    boolean isFinal();

    /**
     * Convenience method for checking whether this is a 2xx response or not.
     * 
     * @return
     */
    boolean isSuccess();

    /**
     * Convenience method for checking whether this is a 3xx response or not.
     * 
     * @return
     */
    boolean isRedirect();

    /**
     * Convenience method for checking whether this is a 4xx response or not.
     * 
     * @return
     */
    boolean isClientError();

    /**
     * Convenience method for checking whether this is a 5xx response or not.
     * 
     * @return
     */
    boolean isServerError();

    /**
     * Convenience method for checking whether this is a 6xx response or not.
     * 
     * @return
     */
    boolean isGlobalError();

    /**
     * Convenience method for checking whether this is a 100 Trying response or
     * not.
     * 
     * @return
     */
    boolean is100Trying();

    /**
     * Convenience method for checking whether this is a 180 Ringing response or
     * or a 183 Early Media response.
     * 
     * @return true if this response is a 180 or a 183 response, false otherwise
     */
    boolean isRinging();

    /**
     * Convenience method for checking whether this is a 480 Timeout response or
     * not.
     * 
     * @return
     */
    boolean isTimeout();

    /**
     * Pop the top-most {@link ViaHeader}.
     * 
     * This is a convenience method for calling {@link SipMessage#popHeader(Buffer)}.
     * 
     * @return the top-most {@link ViaHeader} or null if this {@link SipResponse} contained no
     *         {@link ViaHeader}s.
     */
    ViaHeader popViaHeader() throws SipParseException;

    @Override
    SipResponse clone();

}
