package jazmin.server.sip.io.pkts.packet.sip;

import java.util.List;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.impl.ApplicationPacket;
import jazmin.server.sip.io.pkts.packet.sip.header.CSeqHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.CallIdHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ContactHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ContentTypeHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.FromHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.MaxForwardsHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.RecordRouteHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.RouteHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.SipHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ToHeader;
import jazmin.server.sip.io.pkts.packet.sip.header.ViaHeader;

/**
 * Packet representing a SIP message.
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public interface SipPacket extends ApplicationPacket {

    /**
     * The first line of a sip message, which is either a request or a response
     * line
     * 
     * @return
     */
    Buffer getInitialLine();

    /**
     * Got tired of casting the {@link SipPacket} into a
     * {@link SipRequestPacket} so you can use this method instead. Just a short
     * cut for:
     * 
     * <code>
     *     (SipRequest)sipMessage;
     * </code>
     * 
     * @return this but casted into a {@link SipRequestPacket}
     * @throws ClassCastException
     *             in case this {@link SipPacket} is actually a
     *             {@link SipResponsePacket}.
     */
    SipRequestPacket toRequest() throws ClassCastException;

    /**
     * Got tired of casting the {@link SipPacket} into a
     * {@link SipResponsePacket} so you can use this method instead. Just a
     * short cut for:
     * 
     * <code>
     *     (SipResponse)sipMessage;
     * </code>
     * 
     * @return this but casted into a {@link SipResponsePacket}
     * @throws ClassCastException
     *             in case this {@link SipPacket} is actually a
     *             {@link SipResponsePacket}.
     */
    SipResponsePacket toResponse() throws ClassCastException;

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
     * Returns the content (payload) of the {@link SipPacket} as an
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

    /**
     * Checks whether this {@link SipPacket} is carrying anything in its message
     * body.
     * 
     * @return true if this {@link SipPacket} has a message body, false
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
     * However, you just created a {@link SipPacket} yourself then this method
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
     *         {@link SipPacket}.
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
     *         {@link RouteHeader} headers found in this {@link SipPacket}.
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
     * Convenience method for determining whether the method of this message is
     * a BYE or not. Hence, this is NOT to the method to determine whether this
     * is a BYE Request or not!
     * 
     * @return true if the method of this message is a BYE, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
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
     * {@inheritDoc}
     * 
     * <p>
     * In the case of a SIP message the following checks are conducted: The
     * following checks are available:
     * 
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
    @Override
    void verify();

    /**
     * Get the {@link Buffer} that is representing this {@link SipPacket}. Note,
     * the data behind the buffer is shared with the actual {@link SipPacket} so
     * any changes to the {@link Buffer} will affect this {@link SipPacket}.
     * Hence, by changing this buffer directly, you bypass all checks for valid
     * inputs and the end-result of doing so is undefined (most likely you will
     * either blow up at some point or you will end up sending garbage across
     * the network).
     * 
     * @return
     */
    Buffer toBuffer();

    @Override
    SipPacket clone();

}
