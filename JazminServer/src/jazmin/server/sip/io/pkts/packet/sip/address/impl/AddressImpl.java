/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.address.impl;

import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.ifNull;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.buffer.ByteNotFoundException;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.address.Address;
import jazmin.server.sip.io.pkts.packet.sip.address.URI;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipParser;

/**
 * @author jonas@jonasborjesson.com
 */
public final class AddressImpl implements Address {

    /**
     * The display name or empty if it wasn't set.
     */
    private final Buffer displayName;

    /**
     * The URI portion of this address (the addr-spec in RFC3261 BNF talk)
     */
    private final Buffer uriBuffer;

    /**
     * The framed {@link URI}, which is done lazily
     */
    private URI uri;

    /**
     * We need to know whether the display name was a quoted string so we can
     * generate the same format when we output it through {@link #toBuffer()} or
     * {@link #toString()}.
     */
    private final boolean enclosedDisplayName;

    /**
     * We need to know whether the URI was enclosed within angle brackets or
     * not. We cannot just base this on the display name being there or not
     * since the display name may in fact be empty but we could still have
     * brackets around the URI whether they are needed or not. Just want to put
     * out the same format as what the user gave us.
     */
    private final boolean angleBrackets;

    /**
     * 
     */
    private AddressImpl(final Buffer displayName, final Buffer uri, final boolean enclosedDisplayName,
            final boolean angleBrackets) {
        this.displayName = ifNull(displayName, Buffers.EMPTY_BUFFER);
        this.uriBuffer = uri;
        this.enclosedDisplayName = enclosedDisplayName;
        this.angleBrackets = angleBrackets;
    }

    public AddressImpl(final Buffer displayName, final URI uri) {
        this.displayName = ifNull(displayName, Buffers.EMPTY_BUFFER);
        this.uri = uri;
        this.uriBuffer = null;

        // TODO: we may want to check whether or not we need
        // to enclose the display name or not.
        this.enclosedDisplayName = true;
        this.angleBrackets = true;
    }

    /**
     * @return
     */
    @Override
    public Buffer getDisplayName() {
        return this.displayName;
    }

    /**
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     * @throws SipParseException
     */
    @Override
    public URI getURI() throws SipParseException {
        if (this.uri == null) {
            try {
                this.uri = URI.frame(this.uriBuffer);
            } catch (final IndexOutOfBoundsException e) {
                throw new SipParseException(this.uriBuffer.getReaderIndex(),
                        "Unable to process the value due to a IndexOutOfBoundsException", e);
            } catch (final IOException e) {
                throw new SipParseException(this.uriBuffer.getReaderIndex(),
                        "Could not read from the underlying stream while parsing the value");
            }
        }
        return this.uri;
    }

    /**
     * 
     * Parses a SIP "name-addr" as defined by RFC3261 section 25.1:
     * 
     * <pre>
     * name-addr      =  [ display-name ] LAQUOT addr-spec RAQUOT
     * addr-spec      =  SIP-URI / SIPS-URI / absoluteURI
     * display-name   =  *(token LWS)/ quoted-string
     * </pre>
     * 
     * @param buffer
     * @return
     * @throws SipParseException
     * @throws IOException
     */
    public static final Address parse(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        SipParser.consumeWS(buffer);
        boolean doubleQuote = false;
        if (buffer.peekByte() == SipParser.DQUOT) {
            doubleQuote = true;
        }

        final Buffer displayName = SipParser.consumeDisplayName(buffer);
        boolean leftAngleBracket = true;

        // handle the case of an address that looks like:
        // "" <sip:alice@example.com>
        // where the two double quotes is the ones that
        // caused a problem. This checks for that case and
        // consumes any potential white space that is left
        // after the consumption of that weird display name
        if (doubleQuote && displayName.isEmpty()) {
            SipParser.consumeWS(buffer);
        }

        // if no display name, then there may be a '<' present
        // and if so, consume it.
        if (displayName.isEmpty() && buffer.peekByte() == SipParser.LAQUOT) {
            buffer.readByte();
        } else if (!displayName.isEmpty()) {
            // if display name, we DO expect a '<'. Note, there may or may
            // not be white space before the <
            SipParser.consumeWS(buffer);
            SipParser.expect(buffer, SipParser.LAQUOT);
        } else {
            leftAngleBracket = false;
        }

        // if there is no angle bracket then we are not protected
        // by the '<' '>' construct so then we must actually break
        // when we hit a ';' or a '?' since those would then be part
        // of the header and not the URI
        Buffer addrSpec = null;
        if (!leftAngleBracket) {
            try {
                final int index = buffer.indexOf(1024, SipParser.SEMI, SipParser.QUESTIONMARK, SipParser.CR,
                        SipParser.LF);

                if (index >= 0) {
                    final Buffer temp = buffer.readBytes(index - buffer.getReaderIndex());
                    addrSpec = SipParser.consumeAddressSpec(temp);
                } else {
                    // none of the bytes we were looking for was found
                    // so we will just consume the entire buffer
                    addrSpec = SipParser.consumeAddressSpec(buffer);
                }

            } catch (final ByteNotFoundException e) {
                throw new SipParseException(buffer.getReaderIndex(),
                        "Unable to parse the uri (addr-spec) portion of the address");
            }
        } else {
            addrSpec = SipParser.consumeAddressSpec(buffer);
        }

        if (addrSpec == null) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to find the name-addr portion");
        }

        if (displayName.isEmpty() && buffer.hasReadableBytes() && buffer.peekByte() == SipParser.RAQUOT) {
            buffer.readByte();
        } else if (!displayName.isEmpty()) {
            // if display name, we DO expect a '>'
            SipParser.expect(buffer, SipParser.RAQUOT);
        }

        return new AddressImpl(displayName, addrSpec, doubleQuote, leftAngleBracket);
    }

    /**
     * TODO: make much more efficient once we have a composite buffer
     * {@inheritDoc}
     */
    @Override
    public Buffer toBuffer() {
        final Buffer buffer = Buffers.createBuffer(1024);
        getBytes(buffer);
        return buffer;
    }

    @Override
    public String toString() {
        return toBuffer().toString();
    }

    @Override
    public void getBytes(final Buffer dst) {
        if (!this.displayName.isEmpty()) {
            if (this.enclosedDisplayName) {
                dst.write(SipParser.DQUOT);
                this.displayName.getBytes(0, dst);
                dst.write(SipParser.DQUOT);
            } else {
                this.displayName.getBytes(0, dst);
            }
            dst.write(SipParser.SP);
        }
        if (this.angleBrackets) {
            dst.write(SipParser.LAQUOT);
            if (this.uri == null) {
                this.uriBuffer.getBytes(0, dst);
            } else {
                this.uri.getBytes(dst);
            }
            dst.write(SipParser.RAQUOT);
        } else {
            if (this.uri == null) {
                this.uriBuffer.getBytes(0, dst);
            } else {
                this.uri.getBytes(dst);
            }
        }
    }
}
