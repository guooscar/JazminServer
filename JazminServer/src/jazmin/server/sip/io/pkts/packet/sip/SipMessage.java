package jazmin.server.sip.io.pkts.packet.sip;

import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.assertNotNull;

import java.io.IOException;
import java.util.List;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.header.CSeqHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.CallIdHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ContactHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ContentLengthHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ContentTypeHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ExpiresHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.FromHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.MaxForwardsHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.RecordRouteHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.RouteHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.SipHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ToHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ViaHeader;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipParser;
import jazmin.util.DumpIgnore;

/**
 * Packet representing a SIP message.
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
@DumpIgnore
public interface SipMessage extends Cloneable {

    /**
     * The first line of a sip message, which is either a request or a response
     * line
     * 
     * @return
     */
    Buffer getInitialLine();

    /**
     * Got tired of casting the {@link SipMessage} into a {@link SipRequest} so
     * you can use this method instead. Just a short cut for:
     * 
     * <code>
     *     (SipRequest)sipMessage;
     * </code>
     * 
     * @return this but casted into a {@link SipRequest}
     * @throws ClassCastException
     *             in case this {@link SipMessage} is actually a
     *             {@link SipResponse}.
     */
    SipRequest toRequest() throws ClassCastException;

    /**
     * Got tired of casting the {@link SipMessage} into a {@link SipResponse} so
     * you can use this method instead. Just a short cut for:
     * 
     * <code>
     *     (SipResponse)sipMessage;
     * </code>
     * 
     * @return this but casted into a {@link SipResponse}
     * @throws ClassCastException
     *             in case this {@link SipMessage} is actually a
     *             {@link SipResponse}.
     */
    SipResponse toResponse() throws ClassCastException;

    /**
     * Create a new response based on this {@link SipRequest}. If this
     * {@link SipMessage} is not a {@link SipRequest} then a
     * {@link ClassCastException} will be thrown. Only the mandatory headers
     * from the {@link SipRequest} are copied. Those mandatory headers are:
     * <ul>
     * <li>{@link ToHeader}</li>
     * <li>{@link FromHeader}</li>
     * <li>{@link CallIdHeader}.</li>
     * <li>{@link CSeqHeader}</li>
     * <li>{@link MaxForwardsHeader}</li>
     * </ul>
     * 
     * @param statusCode
     * @param request
     * @return
     * @throws SipParseException
     *             in case anything goes wrong when parsing out headers from the
     *             {@link SipRequest}
     */
    SipResponse createResponse(int responseCode) throws SipParseException, ClassCastException;

    /**
     * Check whether this sip message is a response or not
     * 
     * @return
     */
    boolean isResponse();

    /**
     * Check whether this sip message is a request or not
     * 
     * @return
     */
    boolean isRequest();

    /**
     * Returns the content (payload) of the {@link SipMessage} as an
     * {@link Object}. If the {@link ContentTypeHeader} indicates a content type
     * that is known (such as an sdp) then an attempt to parse the content into
     * that type is made. If the payload is unknown then a {@link Buffer}
     * representing the payload will be returned.
     * 
     * @return
     * @throws SipParseException
     *             in case anything goes wrong when trying to frame the content
     *             in any way.
     */
    Object getContent() throws SipParseException;

    /**
     * Get the content as a {@link Buffer}.
     * 
     * @return
     */
    Buffer getRawContent();
    
    
    void setRawContent(Buffer buffer);

    /**
     * Checks whether this {@link SipMessage} is carrying anything in its
     * message body.
     * 
     * @return true if this {@link SipMessage} has a message body, false
     *         otherwise.
     */
    boolean hasContent();

    /**
     * Get the method of this sip message
     * 
     * @return
     */
    Buffer getMethod() throws SipParseException;

    /**
     * Get the header as a buffer
     * 
     * @param headerName
     *            the name of the header we wish to fetch
     * @return the header as a {@link SipHeader} or null if not found
     * @throws SipParseException
     */
    SipHeader getHeader(Buffer headerName) throws SipParseException;

    /**
     * Same as {@link #getHeader(Buffers.wrap(keyParameter)}.
     * 
     * @param headerName
     *            the name of the header we wish to fetch
     * @return the header as a {@link SipHeader} or null if not found
     * @throws SipParseException
     */
    SipHeader getHeader(String headerName) throws SipParseException;

    void addHeader(SipHeader header) throws SipParseException;

    void addHeaderFirst(SipHeader header) throws SipParseException;

    /**
     * Remove and return the top-most header.
     * 
     * @param headerName the name of the header to pop.
     * @return the removed header or null if there was no such header.
     * @throws SipParseException
     */
    SipHeader popHeader(Buffer headerNme) throws SipParseException;

    /**
     * Set the specified header, which will replace the existing header of the
     * same name. If there are multiple headers of this header, then all "old"
     * ones are removed.
     * 
     * @param header
     */
    void setHeader(SipHeader header) throws SipParseException;

    /**
     * Convenience method for fetching the from-header
     * 
     * @return the from header as a buffer
     * @throws SipParseException
     *             TODO
     */
    FromHeader getFromHeader() throws SipParseException;

    /**
     * Convenience method for fetching the to-header
     * 
     * @return the to header as a buffer
     */
    ToHeader getToHeader() throws SipParseException;

    /**
     * Get the top-most {@link ViaHeader} if present. If this is a request that
     * has been sent then there should always be a {@link ViaHeader} present.
     * However, you just created a {@link SipMessage} youself then this method
     * may return null so please check for it.
     * 
     * @return the top-most {@link ViaHeader} or null if there are no
     *         {@link ViaHeader}s on this message just yet.
     * @throws SipParseException
     */
    ViaHeader getViaHeader() throws SipParseException;

    /**
     * Get all the Via-headers in this {@link SipMessage}. If this is a request
     * that just was created then this may return an empty list.
     * 
     * @return
     * @throws SipParseException
     */
    List<ViaHeader> getViaHeaders() throws SipParseException;

    /**
     * 
     * @return
     * @throws SipParseException
     */
    MaxForwardsHeader getMaxForwards() throws SipParseException;

    /**
     * Get the top-most {@link RecordRouteHeader} header if present.
     * 
     * @return the top-most {@link RecordRouteHeader} header or null if there
     *         are no {@link RecordRouteHeader} headers found in this
     *         {@link SipMessage}.
     * @throws SipParseException
     */
    RecordRouteHeader getRecordRouteHeader() throws SipParseException;

    /**
     * Get all the RecordRoute-headers in this {@link SipMessage}. If there are
     * no {@link RecordRouteHeader}s in this {@link SipMessage} then an empty
     * list will be returned.
     * 
     * @return
     * @throws SipParseException
     */
    List<RecordRouteHeader> getRecordRouteHeaders() throws SipParseException;

    /**
     * Get the top-most {@link RouteHeader} header if present.
     * 
     * @return the top-most {@link RouteHeader} header or null if there are no
     *         {@link RouteHeader} headers found in this {@link SipMessage}.
     * @throws SipParseException
     */
    RouteHeader getRouteHeader() throws SipParseException;

    /**
     * Get all the Route-headers in this {@link SipMessage}. If there are no
     * {@link RouteHeader}s in this {@link SipMessage} then an empty list will
     * be returned.
     * 
     * @return
     * @throws SipParseException
     */
    List<RouteHeader> getRouteHeaders() throws SipParseException;

    /**
     * Get the {@link ExpiresHeader}
     * 
     * @return
     * @throws SipParseException
     */
    ExpiresHeader getExpiresHeader() throws SipParseException;

    /**
     * Get the {@link ContactHeader}
     * 
     * @return
     * @throws SipParseException
     */
    ContactHeader getContactHeader() throws SipParseException;

    /**
     * Get the {@link ContentTypeHeader} for this message. If there is no
     * Content-Type header in this SIP message then null will be returned.
     * 
     * @return the {@link ContentTypeHeader} or null if there is none.
     * @throws SipParseException
     */
    ContentTypeHeader getContentTypeHeader() throws SipParseException;

    /**
     * Convenience method for fetching the call-id-header
     * 
     * @return the call-id header as a buffer
     */
    CallIdHeader getCallIDHeader() throws SipParseException;

    /**
     * Convenience method for fetching the CSeq header
     * 
     * @return
     * @throws SipParseException
     */
    CSeqHeader getCSeqHeader() throws SipParseException;
	
	ContentLengthHeader getContentLengthHeader() throws SipParseException;
    /**
     * Convenience method for determining whether the method of this message is
     * an INVITE or not. Hence, this is NOT to the method to determine whether
     * this is a INVITE Request or not!
     * 
     * @return true if the method of this message is a INVITE, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    boolean isInvite() throws SipParseException;

    /**
     * Convenience method for determining whether the method of this message is an REGISTER or not.
     * 
     * @return true if the method of this message is a REGISTER, false otherwise.
     * @throws SipParseException in case the method could not be parsed out of the underlying
     *         buffer.
     */
    boolean isRegister() throws SipParseException;

    /**
     * Convenience method for determining whether the method of this message is a BYE or not. Hence,
     * this is NOT to the method to determine whether this is a BYE Request or not!
     * 
     * @return true if the method of this message is a BYE, false otherwise.
     * @throws SipParseException in case the method could not be parsed out of the underlying
     *         buffer.
     */
    boolean isBye() throws SipParseException;

    /**
     * Convenience method for determining whether the method of this message is
     * an ACK or not. Hence, this is NOT to the method to determine whether this
     * is an ACK Request or not!
     * 
     * @return true if the method of this message is a ACK, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    boolean isAck() throws SipParseException;

    /**
     * Convenience method for determining whether the method of this message is
     * a OPTIONS or not. Hence, this is NOT to the method to determine whether
     * this is an OPTIONS Request or not!
     * 
     * @return true if the method of this message is a OPTIONS, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    boolean isOptions() throws SipParseException;

    /**
     * Convenience method for determining whether the method of this message is
     * a MESSAGE or not. Hence, this is NOT to the method to determine whether
     * this is an MESSAGE Request or not!
     * 
     * @return true if the method of this message is a MESSAGE, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    boolean isMessage() throws SipParseException;

    /**
     * Convenience method for determining whether the method of this message is
     * a INFO or not. Hence, this is NOT to the method to determine whether this
     * is an INFO Request or not!
     * 
     * @return true if the method of this message is a INFO, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    boolean isInfo() throws SipParseException;

    /**
     * Convenience method for determining whether the method of this message is
     * a CANCEL or not
     * 
     * @return true if the method of this message is a CANCEL, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    boolean isCancel() throws SipParseException;

    /**
     * Checks whether or not this request is considered to be an "initial"
     * request, i.e., a request that does not go within a dialog.
     * 
     * @return
     * @throws SipParseException
     */
    boolean isInitial() throws SipParseException;

    /**
     * <p>
     * <ul>
     * <li>ruri sip version - checks if the SIP version in the request URI is
     * supported, currently only 2.0.</li>
     * <li>ruri scheme - checks if the URI scheme of the request URI is
     * supported (sip[s]|tel[s]) by SIP-router.</li>
     * <li>required headers - checks if the minimum set of required headers to,
     * from, cseq, callid and via is present in the request.</li>
     * <li>via sip version - not working because parser fails already when
     * another version then 2.0 is present.</li>
     * <li>via protocol - not working because parser fails already if an
     * unsupported transport is present.</li>
     * <li>cseq method - checks if the method from the cseq header is equal to
     * the request method.</li>
     * <li>cseq value - checks if the number in the cseq header is a valid
     * unsigned integer.</li>
     * <li>content length - checks if the size of the body matches with the
     * value from the content length header.</li>
     * <li>expires value - checks if the value of the expires header is a valid
     * unsigned integer.</li>
     * <li>proxy require - checks if all items of the proxy require header are
     * present in the list of the extensions from the module parameter
     * proxy_require.</li>
     * 
     * <li>parse uri's - checks if the specified URIs are present and parseable
     * by the SIP-router parsers</li>
     * <li>digest credentials - Check all instances of digest credentials in a
     * message. The test checks whether there are all required digest parameters
     * and have meaningful values.</li>
     * </ul>
     * </p>
     * 
     * <p>
     * This list is taken from <a href=
     * "http://kamailio.org/docs/modules/stable/modules/sanity.html#sanity_check"
     * >Kamailio.org</a>
     * </p>
     * 
     */
    void verify();

    /**
     * Get the {@link Buffer} that is representing this {@link SipMessage}.
     * Note, the data behind the buffer is shared with the actual
     * {@link SipMessage} so any changes to the {@link Buffer} will affect this
     * {@link SipMessage}. Hence, by changing this buffer directly, you bypass
     * all checks for valid inputs and the end-result of doing so is undefined
     * (most likely you will either blow up at some point or you will end up
     * sending garbage across the network).
     * 
     * @return
     */
    Buffer toBuffer();

    /**
     * Perform a deep clone of this SipMessage.
     * 
     * @return
     */
    SipMessage clone();

    /**
     * Frame the supplied buffer into a {@link SipMessage}. No deep analysis of the message will be
     * performed so there is no guarantee that this {@link SipMessage} is actually a well formed
     * message.
     * 
     * @param buffer
     * @return the framed {@link SipMessage}
     */
    static SipMessage frame(final Buffer buffer) throws SipParseException, IOException {
        assertNotNull(buffer);
        return SipParser.frame(buffer);
    }

    /**
     * 
     * @param buffer
     * @return
     * @throws IOException
     */
    static SipMessage frame(final String buffer) throws SipParseException, IOException {
        assertNotEmpty(buffer, "Buffer cannot be null or the empty string");
        return SipParser.frame(Buffers.wrap(buffer));
    }



}
