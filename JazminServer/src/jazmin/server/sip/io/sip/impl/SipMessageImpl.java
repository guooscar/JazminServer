/**
 * 
 */
package jazmin.server.sip.io.sip.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipMessage;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.SipRequest;
import jazmin.server.sip.io.sip.SipResponse;
import jazmin.server.sip.io.sip.header.CSeqHeader;
import jazmin.server.sip.io.sip.header.CallIdHeader;
import jazmin.server.sip.io.sip.header.ContactHeader;
import jazmin.server.sip.io.sip.header.ContentLengthHeader;
import jazmin.server.sip.io.sip.header.ContentTypeHeader;
import jazmin.server.sip.io.sip.header.ExpiresHeader;
import jazmin.server.sip.io.sip.header.FromHeader;
import jazmin.server.sip.io.sip.header.MaxForwardsHeader;
import jazmin.server.sip.io.sip.header.RecordRouteHeader;
import jazmin.server.sip.io.sip.header.RouteHeader;
import jazmin.server.sip.io.sip.header.SipHeader;
import jazmin.server.sip.io.sip.header.ToHeader;
import jazmin.server.sip.io.sip.header.ViaHeader;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public abstract class SipMessageImpl implements SipMessage {

    public static final Buffer FROM_HEADER = Buffers.wrap("From".getBytes());

    public static final Buffer TO_HEADER = Buffers.wrap("To".getBytes());

    public static final Buffer Call_ID_HEADER = Buffers.wrap("Call-ID".getBytes());

    public static final Buffer CSEQ_HEADER = Buffers.wrap("CSeq".getBytes());


    /**
     * The initial line of the sip message, which is either a request or a
     * response line
     */
    private SipInitialLine initialLine;

    /**
     * The unparsed initial line.
     */
    private final Buffer rawInitialLine;

    /**
     * All the headers of the sip message
     */
    private final Buffer headers;

    /**
     * The payload, which may be null
     */
    private  Buffer payload;

    /**
     * Map with parsed headers. Need to change since there are many headers that
     * can appear multiple times. We'll get to that...
     * 
     * We'll keep the default size of 16 and load factory of 0.75, which means
     * that we won't do a re-hash until we hit 12 headers. A basic request has
     * around 10ish headers but in real life there will be much more so get some
     * real world examples and set an appropriate size based on that.
     */
    private final Map<Buffer, List<SipHeader>> parsedHeaders = new LinkedHashMap<Buffer, List<SipHeader>>(16, 0.75f);

    /**
     * 
     * @param rawInitialBuffer
     *            the raw initial line, which is either a request or a response
     *            line (hopefully anyway, we won't know until we try!_
     * @param headers
     *            all the headers (un-parsed) of the SIP message
     * @param payload
     *            the payload or null if there is none
     */
    public SipMessageImpl(final Buffer rawInitialBuffer, final Buffer headers, final Buffer payload) {
        this.rawInitialLine = rawInitialBuffer;
        this.headers = headers;
        this.payload = payload;
    }

    /**
     * 
     * @param initialLine
     *            the initial line, which is either a request or a response line
     * @param headers
     *            all the headers (un-parsed) of the SIP message
     * @param payload
     *            the payload or null if there is none
     */
    public SipMessageImpl(final SipInitialLine initialLine, final Buffer headers, final Buffer payload) {
        this.initialLine = initialLine;
        this.rawInitialLine = null;
        this.headers = headers;
        this.payload = payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getInitialLine() {
        if (this.initialLine == null) {
            return this.rawInitialLine;
        }

        return this.initialLine.getBuffer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isResponse() {
        return getInitialLineInternal().isResponseLine();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isRequest() {
        return getInitialLineInternal().isRequestLine();
    }

    private SipInitialLine getInitialLineInternal() {
        if (this.initialLine == null) {
            this.initialLine = SipInitialLine.parse(this.rawInitialLine);
        }
        return this.initialLine;
    }

    /**
     * Since everything is done lazily, this method will parse and return the
     * initial line as a {@link SipRequestLine}. Only meant to be used by the
     * sub-classes.
     * 
     * Of course, this method could throw both {@link ClassCastException} in
     * case this is actually a response and the parsing of the request line
     * could also fail.
     * 
     * @return
     */
    protected SipRequestLine getRequestLine() throws SipParseException, ClassCastException {
        return (SipRequestLine) getInitialLineInternal();
    }

    /**
     * Same as {@link #getRequestLine()} but for {@link SipResponseLine}
     * instead.
     * 
     * @return
     * @throws SipParseException
     * @throws ClassCastException
     */
    protected SipResponseLine getResponseLine() throws SipParseException, ClassCastException {
        return (SipResponseLine) getInitialLineInternal();
    }

    public SipHeader getSipHeader(final Buffer name) {
        return getHeaderInternal(name, true);
    }

    /**
     * 
     * @param headerName
     * @param frame flag indicating whether or not we should make sure that the header has been
     *        framed to its "real" type.
     * @return
     * @throws SipParseException
     */
    private SipHeader getHeaderInternal(final Buffer headerName, final boolean frame) throws SipParseException {
        List<SipHeader> headers = this.parsedHeaders.get(headerName);
        final SipHeader h = headers == null || headers.isEmpty() ? null : headers.get(0);
        if (h != null) {
            if (frame) {
                final SipHeader framed = h.ensure();
                if (framed != h) {
                    // if the two references are different that means that we did
                    // indeed re-frame the header to a more specific header type.
                    headers.set(0, framed);
                    return framed;
                }
            }
            return h;
        }

        while (this.headers != null && this.headers.hasReadableBytes()) {
            final List<SipHeader> nextHeaders = SipParser.nextHeaders(this.headers);
            if (nextHeaders == null || nextHeaders.isEmpty()) {
                return null;
            }

            final Buffer currentHeaderName = nextHeaders.get(0).getName();
            headers = this.parsedHeaders.get(currentHeaderName);
            if (headers == null) {
                this.parsedHeaders.put(currentHeaderName, nextHeaders);
            } else {
                headers.addAll(nextHeaders);
            }

            if (currentHeaderName.equals(headerName)) {
                final SipHeader header = nextHeaders.get(0);
                if (frame) {
                    final SipHeader framed = header.ensure();
                    if (framed != header) {
                        nextHeaders.set(0, framed);
                    }
                    return framed;
                }
                return header;
            }
        }

        // didn't find the header that was requested
        return null;

    }
    /**
     * {@inheritDoc}
     */
    @Override
    public SipHeader getHeader(final Buffer headerName) throws SipParseException {
        return getHeaderInternal(headerName, false);
    }

    @Override
    public SipHeader popHeader(final Buffer headerName) throws SipParseException {
        final SipHeader header = getHeader(headerName);
        if (header == null) {
            return null;
        }

        final List<SipHeader> headers = this.parsedHeaders.get(headerName);
        headers.remove(0);
        if (headers.isEmpty()) {
            this.parsedHeaders.remove(headerName);
        }

        return header;
    }


    @Override
    public void addHeader(final SipHeader header) {
        internalAddHeader(header, false);
    }

    /**
     * There are situations, such as when user does a
     * {@link #setHeader(SipHeader)}, where we must frame all headers, this
     * method does that.
     * 
     * @throws SipParseException
     */
    private void frameAllHeaders() throws SipParseException {
        while (this.headers != null && this.headers.hasReadableBytes()) {
            final List<SipHeader> nextHeaders = SipParser.nextHeaders(this.headers);
            if (nextHeaders == null || nextHeaders.isEmpty()) {
                return;
            }

            final Buffer currentHeaderName = nextHeaders.get(0).getName();
            final List<SipHeader> headers = this.parsedHeaders.get(currentHeaderName);

            if (headers == null) {
                this.parsedHeaders.put(currentHeaderName, nextHeaders);
            } else {
                headers.addAll(nextHeaders);
            }
        }
    }

    private void internalAddHeader(final SipHeader header, final boolean addFirst) {
        List<SipHeader> headers = this.parsedHeaders.get(header.getName());
        if (headers == null) {
            headers = new ArrayList<SipHeader>();
            this.parsedHeaders.put(header.getName(), headers);
        }

        if (addFirst) {
            headers.add(0, header);
        } else {
            headers.add(header);
        }
    }

    @Override
    public void addHeaderFirst(final SipHeader header) throws SipParseException {
        internalAddHeader(header, true);
    }

    @Override
    public void setHeader(final SipHeader header) throws SipParseException {
    	if(header==null){
    		return;
    	}
        frameAllHeaders();
        final List<SipHeader> headers = new ArrayList<SipHeader>();
        headers.add(header);
        this.parsedHeaders.put(header.getName(), headers);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public SipHeader getHeader(final String headerName) throws SipParseException {
        return getHeader(Buffers.wrap(headerName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FromHeader getFromHeader() throws SipParseException {
        final SipHeader header = getHeaderInternal(FromHeader.NAME, true);
        return (FromHeader) header;
    }
   
    /**
     * {@inheritDoc}
     */
    @Override
    public ViaHeader getViaHeader() throws SipParseException {
        final SipHeader header = getHeaderInternal(ViaHeader.NAME, true);
        return (ViaHeader) header;
    }

    @Override
    public List<ViaHeader> getViaHeaders() throws SipParseException {
        frameAllHeaders();
        final List<SipHeader> headers = this.parsedHeaders.get(ViaHeader.NAME);
        if (headers == null || headers.isEmpty()) {
            return Collections.emptyList();
        }
        final List<ViaHeader> vias = new ArrayList<ViaHeader>(headers.size());
        for (int i = 0; i < headers.size(); ++i) {
            final SipHeader header = headers.get(i);
            if (header instanceof ViaHeader) {
                vias.add((ViaHeader) header);
            } else {
                final Buffer buffer = header.getValue();
                final ViaHeader via = ViaHeader.frame(buffer);
                headers.set(i, via);
                vias.add(via);
            }
        }
        return vias;
    }

    @Override
    public List<RouteHeader> getRouteHeaders() throws SipParseException {
        frameAllHeaders();
        final List<SipHeader> headers = this.parsedHeaders.get(RouteHeader.NAME);
        if (headers == null || headers.isEmpty()) {
            return Collections.emptyList();
        }
        final List<RouteHeader> routes = new ArrayList<RouteHeader>(headers.size());
        for (int i = 0; i < headers.size(); ++i) {
            final SipHeader header = headers.get(i);
            if (header instanceof RouteHeader) {
                routes.add((RouteHeader) header);
            } else {
                final Buffer buffer = header.getValue();
                final RouteHeader route = RouteHeader.frame(buffer);
                headers.set(i, route);
                routes.add(route);
            }
        }
        return routes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecordRouteHeader getRecordRouteHeader() throws SipParseException {
        final SipHeader header = getHeaderInternal(RecordRouteHeader.NAME, true);
        return (RecordRouteHeader) header;
    }

    @Override
    public List<RecordRouteHeader> getRecordRouteHeaders() throws SipParseException {
        frameAllHeaders();
        final List<SipHeader> headers = this.parsedHeaders.get(RecordRouteHeader.NAME);
        if (headers == null || headers.isEmpty()) {
            return Collections.emptyList();
        }
        final List<RecordRouteHeader> recordRoutes = new ArrayList<RecordRouteHeader>(headers.size());
        for (int i = 0; i < headers.size(); ++i) {
            final SipHeader header = headers.get(i);
            if (header instanceof RecordRouteHeader) {
                recordRoutes.add((RecordRouteHeader) header);
            } else {
                final Buffer buffer = header.getValue();
                final RecordRouteHeader rr = RecordRouteHeader.frame(buffer);
                headers.set(i, rr);
                recordRoutes.add(rr);
            }
        }
        return recordRoutes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RouteHeader getRouteHeader() throws SipParseException {
        final SipHeader header = getHeaderInternal(RouteHeader.NAME, true);
        return (RouteHeader) header;
    }

    @Override
    public MaxForwardsHeader getMaxForwards() throws SipParseException {
        final SipHeader header = getHeaderInternal(MaxForwardsHeader.NAME, true);
        return (MaxForwardsHeader) header;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpiresHeader getExpiresHeader() throws SipParseException {
        final SipHeader header = getHeaderInternal(ExpiresHeader.NAME, true);
        return (ExpiresHeader) header;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public ContentLengthHeader getContentLengthHeader() throws SipParseException {
        final SipHeader header = getHeaderInternal(ContentLengthHeader.NAME, true);
        return (ContentLengthHeader) header;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public ContactHeader getContactHeader() throws SipParseException {
        final SipHeader header = getHeaderInternal(ContactHeader.NAME, true);
        return (ContactHeader) header;
    }

    @Override
    public ContentTypeHeader getContentTypeHeader() throws SipParseException {
        final SipHeader header = getHeaderInternal(ContentTypeHeader.NAME, true);
        return (ContentTypeHeader) header;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ToHeader getToHeader() throws SipParseException {
        final SipHeader header = getHeaderInternal(ToHeader.NAME, true);
        return (ToHeader) header;
    }

    @Override
    public CSeqHeader getCSeqHeader() throws SipParseException {
        final SipHeader header = getHeaderInternal(CSeqHeader.NAME, true);
        return (CSeqHeader) header;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CallIdHeader getCallIDHeader() throws SipParseException {
        final SipHeader header = getHeaderInternal(CallIdHeader.NAME, true);
        if (header != null) {
            return (CallIdHeader) header;
        }

        return (CallIdHeader) getHeaderInternal(CallIdHeader.COMPACT_NAME, true);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Buffer getMethod() throws SipParseException;

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInitial() throws SipParseException {

        // over simplified check
        final ToHeader to = getToHeader();
        return to.getTag() == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInvite() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'I' && m.getByte(1) == 'N' && m.getByte(2) == 'V' && m.getByte(3) == 'I'
                    && m.getByte(4) == 'T' && m.getByte(5) == 'E';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRegister() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'R' && m.getByte(1) == 'E' && m.getByte(2) == 'G' && m.getByte(3) == 'I'
                    && m.getByte(4) == 'S' && m.getByte(5) == 'T' && m.getByte(6) == 'E' && m.getByte(7) == 'R';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBye() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'B' && m.getByte(1) == 'Y' && m.getByte(2) == 'E';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAck() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'A' && m.getByte(1) == 'C' && m.getByte(2) == 'K';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUpdate() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'U' && m.getByte(1) == 'P' && m.getByte(2) == 'D' && m.getByte(3) == 'A'
                    && m.getByte(4) == 'T' && m.getByte(5) == 'E';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancel() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'C' && m.getByte(1) == 'A' && m.getByte(2) == 'N' && m.getByte(3) == 'C'
                    && m.getByte(4) == 'E' && m.getByte(5) == 'L';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    @Override
    public boolean isOptions() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'O' && m.getByte(1) == 'P' && m.getByte(2) == 'T' && m.getByte(3) == 'I'
                    && m.getByte(4) == 'O' && m.getByte(5) == 'N' && m.getByte(6) == 'S';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    @Override
    public boolean isMessage() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'M' && m.getByte(1) == 'E' && m.getByte(2) == 'S' && m.getByte(3) == 'S'
                    && m.getByte(4) == 'A' && m.getByte(5) == 'G' && m.getByte(6) == 'E';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    @Override
    public boolean isInfo() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'I' && m.getByte(1) == 'N' && m.getByte(2) == 'F' && m.getByte(3) == 'O';
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse out the method due to underlying IOException", e);
        }
    }

    @Override
    public String toString() {
        return toBuffer().toString();
    }

    @Override
    public final Object getContent() throws SipParseException {
        if (!hasContent()) {
            return null;
        }
        return this.payload;
    }

    @Override
    public final Buffer getRawContent() {
        if (!hasContent()) {
            return null;
        }

        return this.payload;
    }
    //
    public void setRawContent(Buffer buffer){
    	this.payload=buffer;
    	ContentLengthHeader clh=getContentLengthHeader();
    	if(clh==null){
    		clh=ContentLengthHeader.create(payload.getReadableBytes());
    		addHeader(clh);
    	}
    	clh.setContentLength(payload.getReadableBytes());
    }
    //
    @Override
    public final boolean hasContent() {
        return this.payload != null && this.payload.hasReadableBytes();
    }

    @Override
    public SipRequest toRequest() throws ClassCastException {
        throw new ClassCastException("Unable to cast this SipMessage into a SipRequest");
    }

    @Override
    public SipResponse toResponse() throws ClassCastException {
        throw new ClassCastException("Unable to cast this SipMessage into a SipResponse");
    }

    @Override
    public SipResponse createResponse(final int responseCode) throws SipParseException, ClassCastException {
        throw new ClassCastException("Unable to cast this SipMessage into a SipRequest");
    }

    @Override
    public Buffer toBuffer() {
    	//NOTE here we assume max header size is 2048 if max header size > 2048 
    	//result will wrong
    	final Buffer buffer = Buffers.createBuffer(2048+(payload==null?0:payload.capacity()));
    	//final Buffer buffer = Buffers.createBuffer(2048);
    	getInitialLine().getBytes(buffer);
        buffer.write(SipParser.CR);
        buffer.write(SipParser.LF);
        transferHeaders(buffer);
        if(headers==null){
        	 buffer.write(SipParser.CR);
             buffer.write(SipParser.LF); 	
        }
        if (this.payload != null) {
        	if(buffer.getWritableBytes()<payload.capacity()){
        		throw new IllegalStateException("payload is too large,"
        					+payload.capacity()+"/"+buffer.capacity());
        	}
            this.payload.getBytes(0, buffer);
        }
        return buffer;
    }

    /**
     * Helper method to clone all the headers into one continuous buffer.
     * 
     * @return
     */
    protected Buffer cloneHeaders() {
        if (this.headers.getReaderIndex() == 0) {
            return this.headers.clone();
        }

        final Buffer headerClone = Buffers.createBuffer(this.headers.capacity() + 200);
        transferHeaders(headerClone);
        return headerClone;
    }

    /**
     * Helper method to clone the payload.
     * 
     * @return
     */
    protected Buffer clonePayload() {
        if (this.payload != null) {
            return this.payload.clone();
        }

        return null;
    }

    /**
     * Transfer the data of all headers into the supplied buffer.
     * 
     * @param dst
     */
    protected void transferHeaders(final Buffer dst) {
        for (final Entry<Buffer, List<SipHeader>> headers : this.parsedHeaders.entrySet()) {
            for (final SipHeader header : headers.getValue()) {
                header.getBytes(dst);
                dst.write(SipParser.CR);
                dst.write(SipParser.LF);
            }
        }

        if (this.headers != null) {
            this.headers.getBytes(dst);
        }
    }

    @Override
    public abstract SipMessage clone();

}
