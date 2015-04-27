/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
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
import jazmin.server.sip.io.pkts.packet.sip.header.impl.SipHeaderImpl;

/**
 * Basic sip parser that contains most (all?) of the different grammar rules for SIP as defined by
 * RFC 3261. View these various methods as building blocks for building up a complete SIP parser
 * (perhaps I should rename this class?).
 * 
 * All of these functions work in the following way:
 * <ul>
 * <li><b>consumeXXXX</b> - will (in general) simply try and consume whatever it is supposed to
 * consume and the function will return true of false depending on whether is was able to consume
 * anything from the {@link Buffer}. The consume-functions will (if successful) move the reader
 * index of the {@link Buffer}. If unsuccessful, the reader index will be left untouched.</li>
 * <li><b>expectXXX</b> - will (in general) expect that the next thing is whatever it is supposed to
 * expect. These functions are really the same as the consumeXXXX ones but instead of returning true
 * or false the expect-functions will throw a {@link SipParseException} to indicate that things
 * didn't turn out as we were hoping for. Also, remember that the {@link SipParseException} contains
 * the error offset into the {@link Buffer} where things broke. As with the consume-functions, the
 * expect-functions will (if successful) move the reader index of the {@link Buffer}</li>
 * <li></li>
 * </ul>
 * 
 * @author jonas@jonasborjesson.com
 */
public class SipParser {

    /**
     * There are many situations where you are looking to frame something but
     * you cannot find the terminating condition. Since an attacker could easily
     * just have you read a never ending stream we need at some point give up
     * and abort.
     */
    public static final int MAX_LOOK_AHEAD = 1024;


    public static final Buffer USER = Buffers.wrap("user");

    public static final Buffer TTL = Buffers.wrap("ttl");

    public static final Buffer MADDR = Buffers.wrap("maddr");

    public static final Buffer METHOD = Buffers.wrap("method");

    public static final Buffer TRANSPORT = Buffers.wrap("transport");

    public static final Buffer TRANSPORT_EQ = Buffers.wrap("transport=");

    public static final Buffer SIP2_0 = Buffers.wrap("SIP/2.0");

    public static final Buffer SIP2_0_SLASH = Buffers.wrap("SIP/2.0/");

    public static final Buffer SCHEME_SIP = Buffers.wrap("sip");

    public static final Buffer SCHEME_SIP_COLON = Buffers.wrap("sip:");

    public static final Buffer SCHEME_SIPS = Buffers.wrap("sips");

    public static final Buffer SCHEME_SIPS_COLON = Buffers.wrap("sips:");

    public static final byte AT = '@';

    public static final byte COLON = ':';

    public static final byte SEMI = ';';

    public static final byte DOUBLE_QOUTE = '"';

    public static final byte CR = '\r';

    public static final byte LF = '\n';

    public static final byte SP = ' ';

    public static final byte HTAB = '\t';

    public static final byte DASH = '-';

    public static final byte PERIOD = '.';

    public static final byte COMMA = ',';

    public static final byte EXCLAMATIONPOINT = '!';

    public static final byte PERCENT = '%';

    public static final byte STAR = '*';

    public static final byte UNDERSCORE = '_';

    public static final byte QUESTIONMARK = '?';

    public static final byte PLUS = '+';

    public static final byte BACKTICK = '`';

    public static final byte TICK = '\'';

    public static final byte TILDE = '~';

    public static final byte EQ = '=';

    public static final byte SLASH = '/';

    public static final byte BACK_SLASH = '\\';

    /**
     * Left parenthesis
     */
    public static final byte LPAREN = '(';

    /**
     * Right parenthesis
     */
    public static final byte RPAREN = ')';

    /**
     * Right angle quote
     */
    public static final byte RAQUOT = '>';

    /**
     * Left angle quote
     */
    public static final byte LAQUOT = '<';

    /**
     * Double quotation mark
     */
    public static final byte DQUOT = '"';

    public static final Buffer UDP = Buffers.wrap("udp");

    public static final Buffer TCP = Buffers.wrap("tcp");

    public static final Buffer TLS = Buffers.wrap("tls");

    public static final Buffer SCTP = Buffers.wrap("sctp");

    public static final Buffer WS = Buffers.wrap("ws");

    public static final Map<Buffer, Function<SipHeader, ? extends SipHeader>> framers = new HashMap<>();

    static {
        framers.put(CallIdHeader.NAME, header -> CallIdHeader.frame(header.getValue()));
        framers.put(CallIdHeader.COMPACT_NAME, header -> CallIdHeader.frameCompact(header.getValue()));
        framers.put(ContactHeader.NAME, header -> ContactHeader.frame(header.getValue()));
        framers.put(ContentTypeHeader.NAME, header -> ContentTypeHeader.frame(header.getValue()));
        framers.put(CSeqHeader.NAME, header -> CSeqHeader.frame(header.getValue()));
        framers.put(ExpiresHeader.NAME, header -> ExpiresHeader.frame(header.getValue()));
        framers.put(ContentLengthHeader.NAME, header -> ContentLengthHeader.frame(header.getValue()));
        framers.put(FromHeader.NAME, header -> FromHeader.frame(header.getValue()));
        framers.put(MaxForwardsHeader.NAME, header -> MaxForwardsHeader.frame(header.getValue()));
        framers.put(RecordRouteHeader.NAME, header -> RecordRouteHeader.frame(header.getValue()));
        framers.put(RouteHeader.NAME, header -> RouteHeader.frame(header.getValue()));
        framers.put(ToHeader.NAME, header -> ToHeader.frame(header.getValue()));
        framers.put(ViaHeader.NAME, header -> ViaHeader.frame(header.getValue()));
    }


    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // -------- Expect methods expects something to be true and if not ------
    // -------- they will throw an exception of some sort -------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /**
     * Expect that the next set of bytes is "SIP/2.0" and if not then we will
     * throw a {@link SipParseException}
     * 
     * @param buffer
     */
    public static void expectSIP2_0(final Buffer buffer) throws SipParseException {
        expect(buffer, 'S');
        expect(buffer, 'I');
        expect(buffer, 'P');
        expect(buffer, '/');
        expect(buffer, '2');
        expect(buffer, '.');
        expect(buffer, '0');
    }

    /**
     * 
     * @return
     */
    public static Buffer expectMethod(final Buffer buffer) {
        return null;
    }


    /**
     * Check whether the buffer is exactly three bytes long and has the bytes "UDP" in it.
     * 
     * @param t
     * @return
     */
    public static boolean isUDP(final Buffer t) {
        try {
            return t.capacity() == 3 && t.getByte(0) == 'U' && t.getByte(1) == 'D' && t.getByte(2) == 'P';
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isTCP(final Buffer t) {
        try {
            return t.capacity() == 3 && t.getByte(0) == 'T' && t.getByte(1) == 'C' && t.getByte(2) == 'P';
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isTLS(final Buffer t) {
        try {
            return t.capacity() == 3 && t.getByte(0) == 'T' && t.getByte(1) == 'L' && t.getByte(2) == 'S';
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isWS(final Buffer t) {
        try {
            return t.capacity() == 2 && t.getByte(0) == 'W' && t.getByte(1) == 'S';
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isSCTP(final Buffer t) {
        try {
            return t.capacity() == 4 && t.getByte(0) == 'S' && t.getByte(1) == 'C' && t.getByte(2) == 'T'
                    && t.getByte(3) == 'P';
        } catch (final IOException e) {
            return false;
        }
    }

    /**
     * Check whether the buffer is exactly three bytes long and has the
     * bytes "udp" in it. Note, in SIP there is a different between transport
     * specified in a Via-header and a transport-param specified in a SIP URI.
     * One is upper case, one is lower case. Another really annoying thing
     * with SIP.
     * 
     * @param t
     * @return
     */
    public static boolean isUDPLower(final Buffer t) {
        try {
            return t.capacity() == 3 && t.getByte(0) == 'u' && t.getByte(1) == 'd' && t.getByte(2) == 'p';
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isTCPLower(final Buffer t) {
        try {
            return t.capacity() == 3 && t.getByte(0) == 't' && t.getByte(1) == 'c' && t.getByte(2) == 'p';
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isTLSLower(final Buffer t) {
        try {
            return t.capacity() == 3 && t.getByte(0) == 't' && t.getByte(1) == 'l' && t.getByte(2) == 's';
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isWSLower(final Buffer t) {
        try {
            return t.capacity() == 2 && t.getByte(0) == 'w' && t.getByte(1) == 's';
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isSCTPLower(final Buffer t) {
        try {
            return t.capacity() == 4 && t.getByte(0) == 's' && t.getByte(1) == 'c' && t.getByte(2) == 't'
                    && t.getByte(3) == 'p';
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isSips(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException, IOException {
        SipParser.expect(buffer, 's');
        SipParser.expect(buffer, 'i');
        SipParser.expect(buffer, 'p');
        final byte b = buffer.readByte();
        if (b == SipParser.COLON) {
            return false;
        }

        if (b != 's') {
            throw new SipParseException(buffer.getReaderIndex() - 1,
                    "Expected 's' since the only schemes accepted are \"sip\" and \"sips\"");
        }

        SipParser.expect(buffer, SipParser.COLON);
        return true;
    }


    /**
     * Consumes all generic-params it can find. If there are no generic params
     * to be consumed it will return an empty list. The list of generic-params
     * are found on many SIP headers such as the To, From etc. In general, the
     * BNF looks like so:
     * 
     * 
     * <pre>
     *  *( SEMI generic-param )
     * </pre>
     * 
     * E.g. in To:
     * 
     * <pre>
     * To        =  ( "To" / "t" ) HCOLON ( name-addr / addr-spec ) *( SEMI to-param )
     * to-param  =  tag-param / generic-param
     * </pre>
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static List<Buffer[]> consumeGenericParams(final Buffer buffer) throws IndexOutOfBoundsException,
    IOException {
        final List<Buffer[]> params = new ArrayList<Buffer[]>();
        while (buffer.hasReadableBytes() && buffer.peekByte() == SEMI) {
            buffer.readByte(); // consume the SEMI
            params.add(consumeGenericParam(buffer));
        }
        return params;
    }

    /**
     * Consumes a generic param, which according to RFC 3261 section 25.1 is:
     * 
     * <pre>
     * generic-param  =  token [ EQUAL gen-value ]
     * gen-value      =  token / host / quoted-string
     * </pre>
     * 
     * The return value is two buffers returned as an array and if the second
     * element is null (so make sure you check it!) then there was no value for
     * this parameter.
     * 
     * Also note that due to poor implementations out there, the following is
     * accepted as valid input:
     * 
     * foo=
     * 
     * I.e., foo is a flag parameter and as such there should not be an equal
     * sign following it but there are many servers out there that does this
     * anyway.
     * 
     * @param buffer
     *            the buffer from which we will consume a generic-param
     * @return a buffer array of size two. The first element is the name of the
     *         parameter and the second element is the value of that parameter
     *         or null if the parameter was a flag parameter (didn't have a
     *         value)
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static Buffer[] consumeGenericParam(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        final Buffer key = consumeToken(buffer);
        Buffer value = null;
        if (key == null) {
            return new Buffer[2];
        }
        if (consumeEQUAL(buffer) > 0) {
            // TODO: consume host and quoted string
            if (isNext(buffer, DOUBLE_QOUTE)) {
                value = consumeQuotedString(buffer);
            } else {
                value = consumeToken(buffer);
            }
        }
        return new Buffer[] {
                key, value };
    }

    /**
     * Will check whether the next readable byte in the buffer is a certain byte
     * 
     * @param buffer
     *            the buffer to peek into
     * @param b
     *            the byte we are checking is the next byte in the buffer to be
     *            read
     * @return true if the next byte to read indeed is what we hope it is
     */
    public static boolean isNext(final Buffer buffer, final byte b) throws IOException {
        if (buffer.hasReadableBytes()) {
            final Byte actual = buffer.peekByte();
            return actual == b;
        }

        return false;
    }

    /**
     * Check whether the next byte is a digit or not
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static boolean isNextDigit(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        if (buffer.hasReadableBytes()) {
            final char next = (char) buffer.peekByte();
            return next >= 48 && next <= 57;
        }

        return false;
    }

    /**
     * Will expect at least 1 digit and will continue consuming bytes until a
     * non-digit is encountered
     * 
     * @param buffer
     *            the buffer to consume the digits from
     * @return the buffer containing digits only
     * @throws SipParseException
     *             in case there is not one digit at the first position in the
     *             buffer (where the current reader index is pointing to)
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static Buffer expectDigit(final Buffer buffer) throws SipParseException {
        final int start = buffer.getReaderIndex();
        try {

            while (buffer.hasReadableBytes() && isNextDigit(buffer)) {
                // consume it
                buffer.readByte();
            }

            if (start == buffer.getReaderIndex()) {
                throw new SipParseException(start, "Expected digit");
            }

            return buffer.slice(start, buffer.getReaderIndex());
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(start, "Expected digit but no more bytes to read");
        } catch (final IOException e) {
            throw new SipParseException(start, "Expected digit unable to read from underlying stream");
        }
    }

    /**
     * Convenience method for expecting (and consuming) a HCOLON, which is
     * defined as:
     * 
     * HCOLON = *( SP / HTAB ) ":" SWS
     * 
     * See RFC3261 section 25.1 Basic Rules
     */
    public static int expectHCOLON(final Buffer buffer) throws SipParseException {
        try {
            int consumed = consumeWS(buffer);
            expect(buffer, COLON);
            ++consumed;
            // Part of the fix for empty header values. Which based on the BNF I don't
            // think is legal but it is certainly happening in the wild
            // so need to accept it.
            consumed += consumeWS(buffer);
            if (!isNext(buffer, CR)) {
                consumed += consumeSWS(buffer);
            }
            return consumed;
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * Convenience method for expecting (and consuming) a SLASH, which is
     * defined as:
     * 
     * 
     * See RFC3261 section 25.1 Basic Rules
     * 
     * @param buffer
     * @throws SipParseException
     */
    public static void expectSLASH(final Buffer buffer) throws SipParseException {
        try {
            final int count = consumeSLASH(buffer);
            if (count == 0) {
                throw new SipParseException(buffer.getReaderIndex(), "Expected SLASH");
            }
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Expected SLASH but nothing more to read", e);

        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Expected SLASH but problem reading from stream", e);
        }
    }

    /**
     * Consume an slash (SLASH), which according to RFC3261 section 25.1 Basic
     * Rules is:
     * 
     * SLASH = SWS "/" SWS ; slash
     * 
     * @param buffer
     * @return true if we indeed did consume a SLASH
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeSLASH(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, SLASH);
    }

    /**
     * Expect the next byte to be a white space
     * 
     * @param buffer
     */
    public static int expectWS(final Buffer buffer) throws SipParseException {
        int consumed = 0;
        try {
            if (buffer.hasReadableBytes()) {
                final byte b = buffer.getByte(buffer.getReaderIndex());
                if (b == SP || b == HTAB) {
                    // ok, it was a WS so consume the byte
                    buffer.readByte();
                    ++consumed;
                } else {
                    throw new SipParseException(buffer.getReaderIndex(), "Expected WS");
                }
            } else {
                throw new SipParseException(buffer.getReaderIndex(),
                        "Expected WS but nothing more to read in the buffer");
            }
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
        return consumed;
    }

    /**
     * Consume sep (separating?) whitespace (LWS), which according to RFC3261
     * section 25.1 Basic Rules is:
     * 
     * SWS = [LWS] ; sep whitespace
     * 
     * "The SWS construct is used when linear white space is optional, generally
     * between tokens and separators." (RFC3261)
     * 
     * @param buffer
     * @return
     */
    public static int consumeSWS(final Buffer buffer) {
        try {
            return consumeLWS(buffer);
        } catch (final SipParseException e) {
            // ignore since currently the consumeLWS will ONLY
            // throw this exception when it expected a WS at a
            // particular place and since SWS is all optional
            // we will silently just consume it, but return
            // false however
            return 0;
        }
    }

    /**
     * Consume an asterisk/star (STAR), which according to RFC3261 section 25.1
     * Basic Rules is:
     * 
     * STAR = SWS "*" SWS ; asterisk
     * 
     * @param buffer
     * @return true if we indeed did consume a STAR
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeSTAR(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, STAR);
    }

    /**
     * Consume equal sign (EQUAL), which according to RFC3261 section 25.1 Basic
     * Rules is:
     * 
     * EQUAL = SWS "=" SWS ; equal
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeEQUAL(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        return consumeSeparator(buffer, EQ);
    }

    /**
     * Consume left parenthesis (LPAREN), which according to RFC3261 section
     * 25.1 Basic Rules is:
     * 
     * LPAREN = SWS "(" SWS ; left parenthesis
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeLPAREN(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        return consumeSeparator(buffer, LPAREN);
    }

    /**
     * Consume right parenthesis (RPAREN), which according to RFC3261 section
     * 25.1 Basic Rules is:
     * 
     * RPAREN = SWS ")" SWS ; right parenthesis
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeRPAREN(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        return consumeSeparator(buffer, RPAREN);
    }

    /**
     * Consume right angle quote (RAQUOT), which according to RFC3261 section
     * 25.1 Basic Rules is:
     * 
     * RAQUOT = SWS ">"; left angle quote
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeRAQUOT(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        return consumeSeparator(buffer, RAQUOT);
    }

    /**
     * Consume left angle quote (LAQUOT), which according to RFC3261 section
     * 25.1 Basic Rules is:
     * 
     * LAQUOT  =  SWS "<"; left angle quote
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeLAQUOT(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        return consumeSeparator(buffer, LAQUOT);
    }

    /**
     * Consume comma (COMMA), which according to RFC3261 section 25.1 Basic
     * Rules is:
     * 
     * COMMA = SWS "," SWS ; comma
     * 
     * @param buffer
     * @return true if we indeed did consume a COMMA
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeCOMMA(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, COMMA);
    }

    /**
     * Consume semicolon (SEMI), which according to RFC3261 section 25.1 Basic
     * Rules is:
     * 
     * SEMI = SWS ";" SWS ; semicolon
     * 
     * @param buffer
     * @return true if we indeed did consume a SEMI
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeSEMI(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, SEMI);
    }

    /**
     * Consume colon (COLON), which according to RFC3261 section 25.1 Basic
     * Rules is:
     * 
     * COLON = SWS ":" SWS ; colon
     * 
     * @param buffer
     * @return true if we indeed did consume a COLON
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeCOLON(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        return consumeSeparator(buffer, COLON);
    }

    /**
     * Consume open double quotation mark (LDQUT), which according to RFC3261
     * section 25.1 Basic Rules is:
     * 
     * LDQUOT = SWS DQUOTE; open double quotation mark
     * 
     * @param buffer
     * @return true if we indeed did consume a LDQUOT
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeLDQUOT(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        buffer.markReaderIndex();
        int consumed = consumeSWS(buffer);
        if (isNext(buffer, DQUOT)) {
            buffer.readByte();
            ++consumed;
        } else {
            buffer.resetReaderIndex();
            return 0;
        }
        return consumed;
    }

    /**
     * Consume close double quotation mark (RDQUT), which according to RFC3261
     * section 25.1 Basic Rules is:
     * 
     * RDQUOT = DQUOTE SWS ; close double quotation mark
     * 
     * @param buffer
     * @return true if we indeed did consume a LDQUOT
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int consumeRDQUOT(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
    IOException {
        buffer.markReaderIndex();
        int consumed = 0;
        if (isNext(buffer, DQUOT)) {
            buffer.readByte();
            ++consumed;
        } else {
            buffer.resetReaderIndex();
            return 0;
        }
        consumed += consumeSWS(buffer);
        return consumed;
    }

    /**
     * Helper function for checking stuff as described below. It is all the same pattern so...
     * (from rfc 3261 section 25.1)
     * 
     * When tokens are used or separators are used between elements,
     * whitespace is often allowed before or after these characters:
     * 
     * STAR    =  SWS "*" SWS ; asterisk
     * SLASH   =  SWS "/" SWS ; slash
     * EQUAL   =  SWS "=" SWS ; equal
     * LPAREN  =  SWS "(" SWS ; left parenthesis
     * RPAREN  =  SWS ")" SWS ; right parenthesis
     * RAQUOT  =  ">" SWS ; right angle quote
     * LAQUOT  =  SWS "<"; left angle quote
     * COMMA   =  SWS "," SWS ; comma
     * SEMI    =  SWS ";" SWS ; semicolon
     * COLON   =  SWS ":" SWS ; colon
     * LDQUOT  =  SWS DQUOTE; open double quotation mark
     * RDQUOT  =  DQUOTE SWS ; close double quotation mark

     * @param buffer
     * @param b
     * @return the number of bytes that was consumed.
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    private static int consumeSeparator(final Buffer buffer, final byte b) throws IndexOutOfBoundsException,
    IOException {
        buffer.markReaderIndex();
        int consumed = consumeSWS(buffer);
        if (isNext(buffer, b)) {
            buffer.readByte();
            ++consumed;
        } else {
            buffer.resetReaderIndex();
            return 0;
        }
        consumed += consumeSWS(buffer);
        return consumed;
    }

    /**
     * Expects a token, which according to RFC3261 section 25.1 Basic Rules is:
     * 
     * token = 1*(alphanum / "-" / "." / "!" / "%" / "*" / "_" / "+" / "`" / "'"
     * / "~" )
     * 
     * @param buffer
     * @return the buffer containing the expected token
     * @throws IOException
     * @throws IndexOutOfBoundsException
     * @throws SipParseException
     *             in case there is no token
     */
    public static Buffer expectToken(final Buffer buffer) throws IndexOutOfBoundsException, IOException,
    SipParseException {
        final Buffer token = consumeToken(buffer);
        if (token == null) {
            throw new SipParseException(buffer.getReaderIndex(), "Expected TOKEN");
        }
        return token;
    }

    /**
     * Consumes a quoted-string, which is defined as:
     * 
     * <pre>
     * quoted-string  =  SWS DQUOTE *(qdtext / quoted-pair ) DQUOTE
     * qdtext         =  LWS / %x21 / %x23-5B / %x5D-7E / UTF8-NONASCII
     * quoted-pair    =  "\" (%x00-09 / %x0B-0C / %x0E-7F)
     * </pre>
     * 
     * Note, this is a somewhat simplified version and we'll
     * 
     * @param buffer
     * @return
     * @throws IOException
     * @throws SipParseException
     */
    public static Buffer consumeQuotedString(final Buffer buffer) throws SipParseException, IOException {
        final int start = buffer.getReaderIndex();
        expect(buffer, DQUOT);
        while (buffer.hasReadableBytes()) {
            final byte b = buffer.readByte();
            if (b == DQUOT) {
                break;
            } else if (b == BACK_SLASH) {
                buffer.readByte();
            }
        }
        final int stop = buffer.getReaderIndex();
        buffer.setReaderIndex(start + 1);
        final Buffer result = buffer.readBytes(stop - start - 2);
        buffer.setReaderIndex(stop);
        return result;
    }

    /**
     * The display name in SIP is a little tricky since it may or may not be
     * there and the stuff following it (whether or not it was there to begin
     * with) can easily be confused with being a display name.
     * 
     * Note, a display name in SIP is only part of of the "name-addr" construct
     * and this function assumes that (even though it actually would work like a
     * regular {@link #consumeToken(Buffer)} in some cases)
     * 
     * <pre>
     * name-addr      =  [ display-name ] LAQUOT addr-spec RAQUOT
     * display-name   =  *(token LWS)/ quoted-string
     * </pre>
     * 
     * @param buffer
     * @return the display name or null if there was none
     * @throws IOException
     * @throws SipParseException
     */
    public static Buffer consumeDisplayName(final Buffer buffer) throws IOException, SipParseException {
        if (isNext(buffer, DQUOT)) {
            return consumeQuotedString(buffer);
        }

        final int count = getTokenCount(buffer);
        if (count == 0) {
            return Buffers.EMPTY_BUFFER;
        }

        // now, if the next thing after the count is a ':' then
        // we mistook the scheme for a token so bail out.
        buffer.markReaderIndex();
        final Buffer potentialDisplayName = buffer.readBytes(count);
        if (isNext(buffer, COLON)) {
            buffer.resetReaderIndex();
            return Buffers.EMPTY_BUFFER;
        }

        // all good...
        return potentialDisplayName;
    }

    /**
     * Consumes addr-spec, which according to RFC3261 section 25.1 is:
     * 
     * <pre>
     * addr-spec      =  SIP-URI / SIPS-URI / absoluteURI
     * SIP-URI          =  "sip:" [ userinfo ] hostport
     *                      uri-parameters [ headers ]
     * SIPS-URI         =  "sips:" [ userinfo ] hostport
     *                      uri-parameters [ headers ]
     * 
     * absoluteURI    =  scheme ":" ( hier-part / opaque-part )
     * </pre>
     * 
     * And as you can see, it gets complicated. Also, these consume-functions
     * are not to validate the exact grammar but rather to find the boundaries
     * so the strategy for consuming the addr-spec is:
     * <ul>
     * <li>If '>' is encountered, then we assume that this addr-spec is within a
     * name-addr so we stop here</li>
     * <li>If a white space or end-of-line is encountered, we also assume we are
     * done.</li>
     * </ul>
     * 
     * Note, I think the above is safe since I do believe you cannot have a
     * white space or a quoted string (which could have contained white space)
     * within any of the elements that are part of the addr-spec... Anyone?
     * 
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     * @throws SipParseException
     *             in case we cannot successfully frame the addr-spec.
     */
    public static Buffer consumeAddressSpec(final Buffer buffer) throws IndexOutOfBoundsException, IOException,
    SipParseException {
        buffer.markReaderIndex();
        int count = 0;
        int state = 0; // zero is to look for colon, everything else is to find
        // the end
        boolean done = false;

        while (buffer.hasReadableBytes() && !done) {
            ++count;
            final byte b = buffer.readByte();

            // emergency breaks...
            if (state == 0 && count > 99) {
                throw new SipParseException(buffer.getReaderIndex(), "No scheme found after 100 bytes, giving up");
            } else if (count > MAX_LOOK_AHEAD) {
                throw new SipParseException(buffer.getReaderIndex(),
                        "Have not been able to find the entire addr-spec after " + count + " bytes, giving up");
            } else if (state == 0 && b == COLON) {
                state = 1;
            } else if (state == 1 && (b == RAQUOT || b == SP || b == HTAB || b == CR || b == LF)) {
                done = true;
                --count;
            }
        }
        buffer.resetReaderIndex();

        // didn't find the scheme portion
        if (state == 0) {
            throw new SipParseException(buffer.getReaderIndex(), "No scheme found");
        }

        if (count > 0) {
            return buffer.readBytes(count);
        }

        return null;
    }

    /**
     * Consume the userinfo and hostport.
     * 
     * The reason why this method does both is because the userinfo portion is
     * optional and as such you actually don't know whether the stuff you are
     * currently going over is the hostport or the userinfo. Also, unfortunately
     * the userinfo is allowed to have characters that normally are reserved
     * (such as ';'), which complicates things as well.
     * 
     * 
     * <pre>
     * SIP-URI  =  "sip:" [ userinfo ] hostport
     *             uri-parameters [ headers ]
     * SIPS-URI =  "sips:" [ userinfo ] hostport
     *             uri-parameters [ headers ]
     * 
     * userinfo         =  ( user / telephone-subscriber ) [ ":" password ] "@"
     * user             =  1*( unreserved / escaped / user-unreserved )
     * user-unreserved  =  "&" / "=" / "+" / "$" / "," / ";" / "?" / "/"
     * password         =  *( unreserved / escaped / "&" / "=" / "+" / "$" / "," )
     * hostport         =  host [ ":" port ]
     * </pre>
     * 
     * As you can see, the user-unreserved is what mess things up. it would have
     * been way easier and more efficient if the user-unreserved wasn't there...
     * 
     * @param buffer
     * @return
     */
    public static Buffer[] consumeUserInfoHostPort(final Buffer buffer) throws SipParseException, IOException {
        int count = 0;
        Buffer user = null;
        Buffer host = null;
        int indexSemi = 0;
        int indexQuestion = 0;

        // first, try and determine if there is a user portion
        // present. If we can't find one within MAX_BYTES then assume
        // there isn't one there. Note, the way this method is typically used
        // is that you have already framed a buffer so it is very unlikely that
        // a header will be this long so we will most often quite much
        // earlier due to buffer.hasReadableBytes() return false
        int index = buffer.getReaderIndex();
        while (user == null && buffer.hasReadableBytes() && ++count < MAX_LOOK_AHEAD) {
            final byte b = buffer.readByte();
            if (b == SipParser.AT) {
                buffer.setReaderIndex(index);
                if (count - 1 == 0) {
                    throw new SipParseException(count,
                            "No user portion in URI despite the presence of a '@'. This is not legal");
                }
                user = buffer.readBytes(count - 1);
                buffer.readByte(); // consume the '@' sign
                // buffer.markReaderIndex();

                // reset these since they were apparently part
                // of the userinfo and they are legal there so...
                indexSemi = 0;
                indexQuestion = 0;
            } else if (indexSemi == 0 && b == SipParser.SEMI) {
                indexSemi = count;
            } else if (indexQuestion == 0 && b == SipParser.QUESTIONMARK) {
                indexQuestion = count;
            }
        }

        if (user == null) {
            buffer.setReaderIndex(index);
        }

        // no user portion so see if we have an index for
        // a semi colon or a question mark since they signify
        // the split between the hostport part and uri-parameters
        // or headers.
        if (user == null && (indexSemi != 0 || indexQuestion != 0)) {
            if (indexQuestion != 0 && indexQuestion < indexSemi) {
                throw new SipParseException(indexQuestion, "Headers specified before uri-parameters. Not allowed");
            }
            if (indexSemi > 0) {
                host = buffer.readBytes(indexSemi - 1);
            } else if (indexQuestion > 0) {
                host = buffer.readBytes(indexQuestion - 1);
            }
        }

        index = buffer.getReaderIndex();
        count = 0;
        while (host == null && buffer.hasReadableBytes() && ++count < MAX_LOOK_AHEAD) {
            final byte b = buffer.readByte();
            if (b == SipParser.SEMI || b == SipParser.QUESTIONMARK) {
                buffer.setReaderIndex(index);
                host = buffer.readBytes(count - 1);
            }
        }

        if (!buffer.hasReadableBytes()) {
            buffer.setReaderIndex(index);
            host = buffer.readBytes(count);
        } else if (count == MAX_LOOK_AHEAD) {
            throw new SipParseException(count, "Was never able to find the end of the SIP URI. Gave up after " + count
                    + " bytes");
        }
        return new Buffer[] {
                user, host };
    }

    /**
     * Consume the "sent-protocol", which according to RFC 3261 is:
     * 
     * <pre>
     * sent-protocol = protocol-name SLASH protocol-version SLASH transport
     * transport     =  "UDP" / "TCP" / "TLS" / "SCTP" / other-transport
     * other-transport   =  token
     * </pre>
     * 
     * The "sent-protocol" is only present in a Via header and typically looks
     * like this: SIP/2.0/UDP
     * 
     * The consume method will make sure that "SIP/2.0/" is present and if not,
     * complain. The transport can really be anything and as such that is what
     * you will get back as a return value. Hence, in the above example you
     * would get back a buffer consisting of "UDP".
     * 
     * Also note that the transport can be "other-transport" which translates to
     * a "token" so we allow really anything and as such we will just consume
     * and return the token after verifying that it start with the SIP/2.0
     * stuff.
     * 
     * @param buffer
     * @return the transport part of the "sent-protocol". Typically this will be
     *         one of UDP, TCP or TLS.
     * @throws IOException
     * @throws SipParseException
     *             in case anything goes wrong while parsing including if the
     *             protocol-name isn't SIP and the version isn't 2.0
     */
    public static Buffer consumeSentProtocol(final Buffer buffer) throws IOException, SipParseException {
        expectSIP2_0(buffer);
        expect(buffer, SipParser.SLASH);

        final Buffer protocol = consumeToken(buffer);
        if (protocol == null || protocol.isEmpty()) {
            throw new SipParseException(buffer.getReaderIndex(), "Expected transport");
        }
        return protocol;
    }

    /**
     * Consume a token, which according to RFC3261 section 25.1 Basic Rules is:
     * 
     * token = 1*(alphanum / "-" / "." / "!" / "%" / "*" / "_" / "+" / "`" / "'"
     * / "~" )
     * 
     * @param buffer
     * @return the buffer containing the token we consumed or null if nothing
     *         was consumed.
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static Buffer consumeToken(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        final int count = getTokenCount(buffer);
        if (count == 0) {
            return null;
        }
        return buffer.readBytes(count);
    }

    /**
     * Consumes a alphanum.
     * 
     * @param buffer
     * @return
     * @throws IOException
     */
    public static Buffer consumeAlphaNum(final Buffer buffer) throws IOException {
        final int count = getAlphaNumCount(buffer);
        if (count == 0) {
            return null;
        }
        return buffer.readBytes(count);
    }

    /**
     * Consume a Via-header, which according to RFC3261 is:
     * 
     * <pre>
     * Via               =  ( "Via" / "v" ) HCOLON via-parm *(COMMA via-parm)
     * via-parm          =  sent-protocol LWS sent-by *( SEMI via-params )
     * via-params        =  via-ttl / via-maddr / via-received / via-branch / via-extension
     * via-ttl           =  "ttl" EQUAL ttl
     * via-maddr         =  "maddr" EQUAL host
     * via-received      =  "received" EQUAL (IPv4address / IPv6address)
     * via-branch        =  "branch" EQUAL token
     * via-extension     =  generic-param
     * sent-protocol     =  protocol-name SLASH protocol-version SLASH transport
     * </pre>
     * 
     * Note, this method assumes that you have already stripped off the "Via" or
     * "v". I.e., the header name.
     * 
     * The return value is ALWAYS an array of objects of size 4. However, the
     * port can be null (index 2 = the third value) so make sure to check it.
     * Also, the 4th value (index 3) contains a List<Buffer[]> containing all
     * the via-parameters. Each entry in the list is a two dimensional array
     * where the first element is the key and the second is the value. For flag
     * parameters, the value will be null so make sure to check. For more info
     * regarding how parameters are parsed, see
     * {@link #consumeGenericParams(Buffer)}.
     * 
     * <ul>
     * <li>result[0] - the protocol. Will never ever be null</li>
     * <li>result[1] - the sent-by host. Will never ever be null</li>
     * <li>result[2] - the sent-by port. May be null if port wasn't specified</li>
     * <li>result[3] - the via-parameters as a List<Buffer[]>. Will always have
     * elements in it since a via without a branch parameter is illegal.</li>
     * </ul>
     * 
     * @param buffer
     * @return returns an array of 4 elements. See above for details
     * @throws SipParseException
     * @throws IOException
     */
    public static Object[] consumeVia(final Buffer buffer) throws SipParseException, IOException {
        final Object[] result = new Object[4];
        // start off by just finding the ';'. A Via-header MUST have a branch
        // parameter and as such
        // there must be a ';' present. If there isn't one, then bail out and
        // complain.
        int count = 0;
        int indexOfSemi = 0;
        int countOfColons = 0;
        int indexOfLastColon = 0;
        int readerIndexOfLastColon = 0; // for reporting

        result[0] = consumeSentProtocol(buffer);
        consumeLWS(buffer);

        final int index = buffer.getReaderIndex();
        while (indexOfSemi == 0 && buffer.hasReadableBytes() && ++count < MAX_LOOK_AHEAD) {
            final byte b = buffer.readByte();
            if (b == SipParser.SEMI) {
                indexOfSemi = count;
            } else if (b == SipParser.COLON) {
                ++countOfColons;
                indexOfLastColon = count;
                readerIndexOfLastColon = buffer.getReaderIndex();
            }
        }

        if (count == 0) {
            return null;
        }

        if (count == MAX_LOOK_AHEAD) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to find the parameters part of the Via-header "
                            + "even after searching for " + MAX_LOOK_AHEAD + " bytes.");
        }

        if (indexOfSemi == 0) {
            // well, we don't check if the branch parameter is there but
            // if there are no parameters present at all then there
            // is no chance it is present.
            throw new SipParseException(buffer.getReaderIndex(),
                    "No via-parameters found. The Via-header MUST contain at least the branch parameter.");
        }

        buffer.setReaderIndex(index);

        if (countOfColons == 0 || countOfColons == 7) {
            // no port, just host
            result[1] = buffer.readBytes(indexOfSemi - 1);
        } else if (indexOfLastColon != 0 && (countOfColons == 1 || countOfColons == 8)) {
            // found either a single colon or 8 colons where 8 colons indicates
            // that we have an ipv6 address in front of us.
            result[1] = buffer.readBytes(indexOfLastColon - 1);
            buffer.readByte(); // consume ':'
            result[2] = buffer.readBytes(indexOfSemi - indexOfLastColon - 1); // consume
            // port
            if (result[2] == null || ((Buffer) result[2]).isEmpty()) {
                throw new SipParseException(readerIndexOfLastColon + 1, "Expected port after colon");
            }
        } else {
            // indication an strange number of colons. May be the strange
            // ipv4 address after ipv6 thing which we currently dont handle
            throw new SipParseException(indexOfLastColon, "Found " + countOfColons + " which seems odd."
                    + " Expecting 0, 1, 7 or 8 colons in the Via-host:port portion. Please check your traffic");
        }

        final List<Buffer[]> params = consumeGenericParams(buffer);
        result[3] = params;

        return result;
    }

    /**
     * Consume a sent-by which according to 3261 is:
     * 
     * <pre>
     * sent-by           =  host [ COLON port ]
     * host             =  hostname / IPv4address / IPv6reference
     * hostname         =  *( domainlabel "." ) toplabel [ "." ]
     * domainlabel      =  alphanum
     * toplabel         =  ALPHA / ALPHA *( alphanum / "-" ) alphanum
     * IPv4address    =  1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT
     * IPv6reference  =  "[" IPv6address "]"
     * IPv6address    =  hexpart [ ":" IPv4address ]
     * hexpart        =  hexseq / hexseq "::" [ hexseq ] / "::" [ hexseq ]
     * hexseq         =  hex4 *( ":" hex4)
     * hex4           =  1*4HEXDIG
     * port           =  1*DIGIT
     * </pre>
     * 
     * Now, since we want to do things as fast as possible and all the
     * consumeXXX methods only do framing we will implement something a little
     * simpler, faster but not 100% according to the BNF.
     * 
     * So, consume everything until we either hit a ';' signifying parameters or
     * a ':' which then forces us to start checking the port. Also, white space
     * will stop parsing.
     * 
     * However, a colon could also signify an IPv6 address, which is not handled
     * right now.
     * 
     * @param buffer
     * @return
     * @throws IOException
     */
    public static Buffer[] consumeSentBye(final Buffer buffer) throws SipParseException, IOException {
        final int index = buffer.getReaderIndex();
        int count = 0;
        boolean done = false;
        boolean firstColon = false;
        boolean consumePort = false;
        while (!done && buffer.hasReadableBytes()) {
            final byte b = buffer.readByte();
            ++count;
            if (firstColon && isDigit(b)) {
                // get port
                consumePort = true;
                done = true;
                count -= 2;
                continue;
            } else if (firstColon) {
                // consume
                firstColon = false;
                continue;
            }

            if (b == SipParser.COLON) {
                firstColon = true;
            } else if (b == SipParser.SEMI) {
                --count;
                done = true;
            }
        }

        if (count == 0) {
            return null;
        }

        buffer.setReaderIndex(index);
        final Buffer host = buffer.readBytes(count);
        Buffer port = null;
        if (consumePort) {
            buffer.readByte(); // consume ':'
            port = consumePort(buffer);
        }
        return new Buffer[] {
                host, port };
    }

    /**
     * Consume a port, which according to RFC 3261 is:
     * 
     * port = 1*DIGIT
     * 
     * @param buffer
     * @return the buffer containing only digits or null if there was none fun.
     * @throws IOException
     */
    public static Buffer consumePort(final Buffer buffer) throws IOException {
        int count = 0;
        final int index = buffer.getReaderIndex();
        boolean done = false;
        while (!done && buffer.hasReadableBytes()) {
            if (isDigit(buffer.readByte())) {
                ++count;
            } else {
                done = true;
            }
        }

        buffer.setReaderIndex(index);
        if (count != 0) {
            return buffer.readBytes(count);
        }

        return null;
    }

    public static Buffer consumeHostname(final Buffer buffer) throws IOException {
        return null;
    }

    /**
     * Consume a m-type, which according to RFC3261 section 25.1 Basic Rules is:
     * 
     * <pre>
     * m-type           =  discrete-type / composite-type
     * discrete-type    =  "text" / "image" / "audio" / "video" / "application" / extension-token
     * composite-type   =  "message" / "multipart" / extension-token
     * extension-token  =  ietf-token / x-token
     * ietf-token       =  token
     * x-token          =  "x-" token
     * </pre>
     * 
     * And if you really read through all of that stuff it all boils down to
     * "token" so that is all we end up doing in this method. Hence, this method
     * is really only to put some context to consuming a media-type
     * 
     * @return
     * @throws IndexOutOfBoundsException
     * @throws IOException
     */
    public static Buffer consumeMType(final Buffer buffer) throws SipParseException {
        try {
            return consumeToken(buffer);
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Tried to consume m-type but buffer ended abruptly", e);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Tried to consume m-type but problem reading from underlying stream", e);
        }
    }

    /**
     * Consume a m-subtype, which according to RFC3261 section 25.1 Basic Rules
     * is:
     * 
     * <pre>
     * m-subtype        =  extension-token / iana-token
     * extension-token  =  ietf-token / x-token
     * ietf-token       =  token
     * x-token          =  "x-" token
     * iana-token       =  token
     * </pre>
     * 
     * And if you really read through all of that stuff it all boils down to
     * "token" so that is all we end up doing in this method. Hence, this method
     * is really only to put some context to consuming a media-type
     * 
     * @return
     * @throws IndexOutOfBoundsException
     * @throws IOException
     */
    public static Buffer consumeMSubtype(final Buffer buffer) throws SipParseException {
        try {
            return consumeToken(buffer);
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Tried to consume m-type but buffer ended abruptly", e);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Tried to consume m-type but problem reading from underlying stream", e);
        }
    }

    /**
     * Helper method that counts the number of bytes that are considered part of
     * the next token in the {@link Buffer}.
     * 
     * @param buffer
     * @return a count of the number of bytes the next token contains or zero if
     *         no token is to be found within the buffer.
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static int getTokenCount(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        boolean done = false;
        int count = 0;
        buffer.markReaderIndex();
        while (buffer.hasReadableBytes() && !done) {
            final byte b = buffer.readByte();
            final boolean ok = isAlphaNum(b) || b == DASH || b == PERIOD || b == EXCLAMATIONPOINT
                    || b == PERCENT || b == STAR || b == UNDERSCORE || b == PLUS || b == BACKTICK
                    || b == TICK || b == TILDE;
            if (ok) {
                ++count;
            } else {
                done = true;
            }
        }
        buffer.resetReaderIndex();
        return count;
    }

    /**
     * Helper method that counts the number of bytes that are considered part of
     * the next alphanum block.
     * 
     * @param buffer
     * @return a count of the number of bytes the next alphaum contains or zero
     *         if none is found.
     * @throws IndexOutOfBoundsException
     * @throws IOException
     */
    public static int getAlphaNumCount(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        boolean done = false;
        int count = 0;
        final int index = buffer.getReaderIndex();
        while (buffer.hasReadableBytes() && !done) {
            final byte b = buffer.readByte();
            if (isAlphaNum(b)) {
                ++count;
            } else {
                done = true;
            }
        }
        buffer.setReaderIndex(index);
        return count;
    }

    /**
     * Check whether next byte is a alpha numeric one.
     * 
     * @param buffer
     * @return true if the next byte is a alpha numeric character, otherwise
     *         false
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public static boolean isNextAlphaNum(final Buffer buffer) throws IndexOutOfBoundsException, IOException {
        if (buffer.hasReadableBytes()) {
            final byte b = buffer.peekByte();
            return isAlphaNum(b);
        }

        return false;
    }

    /**
     * Helper method for checking whether the supplied byte is a alphanumeric
     * character or not.
     * 
     * @param b
     * @return true if the byte is indeed a alphanumeric character, false
     *         otherwise
     */
    public static boolean isAlphaNum(final char ch) {
        return ch >= 97 && ch <= 122 || ch >= 48 && ch <= 57 || ch >= 65 && ch <= 90;
    }

    public static boolean isAlphaNum(final byte b) {
        return isAlphaNum((char) b);
    }

    public static boolean isDigit(final char ch) {
        return ch >= 48 && ch <= 57;
    }

    public static boolean isDigit(final byte b) {
        return isDigit((char) b);
    }

    /**
     * Consume linear whitespace (LWS), which according to RFC3261 section 25.1
     * Basic Rules is:
     * 
     * LWS = [*WSP CRLF] 1*WSP ; linear whitespace
     * 
     * @param buffer
     * @return the number of bytes consumed
     */
    public static int consumeLWS(final Buffer buffer) throws SipParseException {
        final int i = buffer.getReaderIndex();
        consumeWS(buffer);

        // if we consume a CRLF we expect at least ONE WS to be present next
        if (consumeCRLF(buffer) > 0) {
            expectWS(buffer);
        }
        consumeWS(buffer);
        if (buffer.getReaderIndex() == i) {
            throw new SipParseException(i, "Expected at least 1 WSP");
        }
        return buffer.getReaderIndex() - i;
    }

    /**
     * Consume CR + LF
     * 
     * @param buffer
     * @return true if we indeed did consume CRLF, false otherwise
     */
    public static int consumeCRLF(final Buffer buffer) throws SipParseException {
        try {
            buffer.markReaderIndex();
            final byte cr = buffer.readByte();
            final byte lf = buffer.readByte();
            if (cr == CR && lf == LF) {
                return 2;
            }
        } catch (final IndexOutOfBoundsException e) {
            // fall through
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
        buffer.resetReaderIndex();
        return 0;
    }

    /**
     * Check so that the next byte in the passed in buffer is the expected one.
     * 
     * @param buffer
     *            the buffer that we will check.
     * @param expected
     *            the buffer that contains the expected byte (note, it is 1
     *            byte, not bytes)
     * @throws ParseException
     *             in case the expected byte is not the next byte in the buffer
     */
    public static void expect(final Buffer buffer, final byte expected) throws SipParseException, IOException {
        final byte actual = buffer.readByte();
        if (actual != expected) {
            final String actualStr = new String(new byte[] { actual });
            final String expectedStr = new String(new byte[] { expected });
            throw new SipParseException(buffer.getReaderIndex(), "Expected '" + expected + "' (" + expectedStr
                    + ") got '" + actual + "' (" + actualStr + ")");
        }
    }

    /**
     * Consume all the whitespace we find (WS)
     * 
     * @param buffer
     * @return true if we did consume something, false otherwise
     */
    public static int consumeWS(final Buffer buffer) throws SipParseException {
        try {
            boolean done = false;
            int count = 0;
            while (buffer.hasReadableBytes() && !done) {
                if (isNext(buffer, SP) || isNext(buffer, HTAB)) {
                    buffer.readByte();
                    ++count;
                } else {
                    done = true;
                }
            }
            return count;
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * Check so that the next byte in the passed in buffer is the expected one.
     * 
     * @param buffer
     *            the buffer that we will check.
     * @param expected
     *            the expected char
     * @throws SipParseException
     *             in case the expected char is not the next char in the buffer
     *             or if there is an error reading from the underlying stream
     */
    public static void expect(final Buffer buffer, final char ch) throws SipParseException {
        try {
            final int i = buffer.readUnsignedByte();
            if (i != ch) {
                throw new SipParseException(buffer.getReaderIndex(), "Expected '" + ch + "' got '" + i + "'");
            }
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    public static List<SipHeader> nextHeaders(final Buffer buffer) throws SipParseException {
        try {
            final int startIndex = buffer.getReaderIndex();
            int nameIndex = 0;
            while (buffer.hasReadableBytes() && nameIndex == 0) {
                if (isNext(buffer, SP) || isNext(buffer, HTAB) || isNext(buffer, COLON)) {
                    nameIndex = buffer.getReaderIndex();
                } else {
                    buffer.readByte();
                }
            }

            // Bad header! No HCOLON found! (or beginning thereof anyway)
            if (nameIndex == 0) {
                // probably ran out of bytes to read so lets just return null
                return null;
            }

            final Buffer name = buffer.slice(startIndex, nameIndex);
            expectHCOLON(buffer);
            final List<Buffer> values = readHeaderValues(name, buffer);
            final List<SipHeader> headers = new ArrayList<SipHeader>(values.size());
            for (final Buffer value : values) {
                headers.add(new SipHeaderImpl(name, value));
            }
            return headers;
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * Not all headers allow for multiple values on a single line. This is a
     * basic check for validating whether or not that the header allows it or
     * not. Note, for headers such as Contact, it depends!
     * 
     * @param headerName
     * @return
     */
    private static boolean isHeaderAllowingMultipleValues(final Buffer headerName) {
        final int size = headerName.getReadableBytes();
        if (size == 7) {
            return !isSubjectHeader(headerName);
        } else if (size == 5) {
            return !isAllowHeader(headerName);
        } else if (size == 4) {
            return !isDateHeader(headerName);
        }

        return true;
    }

    /**
     * The date header also allows for comma within the value of the header.
     * 
     * @param name
     * @return
     */
    private static boolean isDateHeader(final Buffer name) {
        try {
            return name.getByte(0) == 'D' && name.getByte(1) == 'a' &&
                    name.getByte(2) == 't' && name.getByte(3) == 'e';
        } catch (final IOException e) {
            return false;
        }
    }

    private static boolean isAllowHeader(final Buffer name) {
        try {
            return name.getByte(0) == 'A' && name.getByte(1) == 'l' &&
                    name.getByte(2) == 'l' && name.getByte(3) == 'o' &&
                    name.getByte(4) == 'w';
        } catch (final IOException e) {
            return false;
        }
    }

    private static boolean isSubjectHeader(final Buffer name) {
        try {
            return name.getByte(0) == 'S' && name.getByte(1) == 'u' &&
                    name.getByte(2) == 'b' && name.getByte(3) == 'j' &&
                    name.getByte(4) == 'e' && name.getByte(5) == 'c' &&
                    name.getByte(6) == 't';
        } catch (final IOException e) {
            return false;
        }
    }

    private static List<Buffer> readHeaderValues(final Buffer headerName, final Buffer buffer) throws IOException {
        final List<Buffer> values = new ArrayList<Buffer>(2);
        int start = buffer.getReaderIndex();
        int stop = -1;
        boolean foundCR = false;
        boolean foundLF = false;
        boolean foundComma = false;
        boolean insideQuotedString = false;
        boolean done = false;
        boolean foldedLine = false;

        while (buffer.hasReadableBytes() && !done) {
            final byte b = buffer.readByte();
            switch (b) {
                case DOUBLE_QOUTE:
                    insideQuotedString = !insideQuotedString;
                    break;
                case LF:
                    foundLF = true;
                    stop = buffer.getReaderIndex() - 1;
                    break;
                case CR:
                    foundCR = true;
                    stop = buffer.getReaderIndex() - 1;
                    break;
                case COMMA:
                    // if we find a comma then we may have found the end of this
                    // header value depending whether or not the header we are
                    // framing actually allows multiple values on a single line
                    if (!insideQuotedString && isHeaderAllowingMultipleValues(headerName)) {
                        stop = buffer.getReaderIndex() - 1;
                        buffer.setReaderIndex(stop);
                        consumeCOMMA(buffer);
                        foundComma = true;
                        foundCR = true;
                    }
                    break;
                default:
                    break;
            }

            if (foundCR || foundLF) {
                values.add(buffer.slice(start, stop));
                if (isNext(buffer, LF)) {
                    buffer.readByte();
                }
                // Until we have implemented the CompositeBuffer I'm just going to cheat like this
                // and yes, it is stupid but folded lines aren't that common so...
                if (foldedLine) {
                    final Buffer line2 = values.remove(values.size() - 1);
                    final Buffer line1 = values.remove(values.size() - 1);
                    final Buffer folded = Buffers.wrap(line1 + " " + line2);
                    values.add(folded);
                    foldedLine = false;
                }

                if (isNext(buffer, SP) || isNext(buffer, HTAB)) {
                    consumeWS(buffer);
                    foldedLine = !foundComma;
                } else if (!foundComma) {
                    done = true;
                }

                foundCR = false;
                foundLF = false;
                foundComma = false;
                start = buffer.getReaderIndex();
            }
        }

        return values;
    }

    /**
     * Get the next header, which may actually be returning multiple if there
     * are multiple headers on the same line.
     * 
     * @param buffer
     * @return an array where the first element is the name of the buffer and
     *         the second element is the value of the buffer
     * @throws SipParseException
     */
    public static SipHeader nextHeader(final Buffer buffer) throws SipParseException {

        try {

            final int startIndex = buffer.getReaderIndex();
            int nameIndex = 0;
            while (buffer.hasReadableBytes() && nameIndex == 0) {
                if (isNext(buffer, SP) || isNext(buffer, HTAB) || isNext(buffer, COLON)) {
                    nameIndex = buffer.getReaderIndex();
                } else {
                    buffer.readByte();
                }
            }

            // Bad header! No HCOLON found! (or beginning thereof anyway)
            if (nameIndex == 0) {
                // probably ran out of bytes to read so lets just return null
                return null;
            }

            final Buffer name = buffer.slice(startIndex, nameIndex);

            expectHCOLON(buffer);

            // Note, we are framing headers from a sip request or response which
            // is why we safely can assume that there will ALWAYS be a CRLF
            // after each line
            Buffer valueBuffer = buffer.readLine();

            // Foled lines are rare so try and avoid the bulk of
            // the work if possible.
            if (isNext(buffer, SP) || isNext(buffer, HTAB)) {
                List<Buffer> foldedLines = null;
                boolean done = false;

                while (!done) {
                    if (isNext(buffer, SP) || isNext(buffer, HTAB)) {
                        consumeWS(buffer);
                        if (foldedLines == null) {
                            foldedLines = new ArrayList<Buffer>(2);
                        }
                        foldedLines.add(buffer.readLine());
                    } else {
                        done = true;
                    }
                }

                if (foldedLines != null) {
                    // even though stupid, folded lines are not that common
                    // so optimize if this ever becomes a problem.
                    String stupid = valueBuffer.toString();
                    for (final Buffer line : foldedLines) {
                        stupid += " " + line.toString();
                    }
                    valueBuffer = Buffers.wrap(stupid.getBytes());
                    consumeWS(valueBuffer);
                }
            }

            return new SipHeaderImpl(name, valueBuffer);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to read from stream", e);
        }
    }

    /**
     * Frame the supplied buffer into a {@link SipMessage}. No deep analysis of the message will be
     * performed by this framer so there is no guarantee that this {@link SipMessage} is actually a
     * well formed message.
     * 
     * @param buffer
     * @return the framed {@link SipMessage}
     */
    public static SipMessage frame(final Buffer buffer) throws IOException {
        if (!couldBeSipMessage(buffer)) {
            throw new SipParseException(0, "Cannot be a SIP message because is doesnt start with \"SIP\" "
                    + "(for responses) or a method (for requests)");
        }

        // we just assume that the initial line
        // indeed is a correct sip line
        final Buffer rawInitialLine = buffer.readLine();

        // which means that the headers are about
        // to start now.
        final int startHeaders = buffer.getReaderIndex();

        Buffer currentLine = null;
        while ((currentLine = buffer.readLine()) != null && currentLine.hasReadableBytes()) {
            // just moving along, we don't really care why
            // we stop, we have found what we want anyway, which
            // is the boundary between headers and the potential
            // payload (or end of message)
        }

        final Buffer headers = buffer.slice(startHeaders, buffer.getReaderIndex());
        Buffer payload = null;
        if (buffer.hasReadableBytes()) {
            payload = buffer.slice();
        }

        if (SipInitialLine.isResponseLine(rawInitialLine)) {
            return new SipResponseImpl(rawInitialLine, headers, payload);
        } else {
            return new SipRequestImpl(rawInitialLine, headers, payload);
        }
    }

    /**
     * Helper function that checks whether or not the data could be a SIP message. It is a very
     * basic check but if it doesn't go through it definitely is not a SIP message.
     * 
     * @param data
     * @return
     */
    public static boolean couldBeSipMessage(final Buffer data) throws IOException {
        final byte a = data.getByte(0);
        final byte b = data.getByte(1);
        final byte c = data.getByte(2);
        return a == 'S' && b == 'I' && c == 'P' || // response
                a == 'I' && b == 'N' && c == 'V' || // INVITE
                a == 'A' && b == 'C' && c == 'K' || // ACK
                a == 'B' && b == 'Y' && c == 'E' || // BYE
                a == 'O' && b == 'P' && c == 'T' || // OPTIONS
                a == 'C' && b == 'A' && c == 'N' || // CANCEL
                a == 'M' && b == 'E' && c == 'S' || // MESSAGE
                a == 'R' && b == 'E' && c == 'G' || // REGISTER
                a == 'I' && b == 'N' && c == 'F' || // INFO
                a == 'P' && b == 'R' && c == 'A' || // PRACK
                a == 'S' && b == 'U' && c == 'B' || // SUBSCRIBE
                a == 'N' && b == 'O' && c == 'T' || // NOTIFY
                a == 'U' && b == 'P' && c == 'D' || // UPDATE
                a == 'R' && b == 'E' && c == 'F' || // REFER
                a == 'P' && b == 'U' && c == 'B'; // PUBLISH

    }

}
