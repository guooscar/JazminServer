/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.header;

import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.assertArgument;
import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.header.impl.CSeqHeaderImpl;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipParser;

/**
 * @author jonas@jonasborjesson.com
 */
public interface CSeqHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("CSeq");

    Buffer getMethod();

    long getSeqNumber();

    @Override
    CSeqHeader clone();

    /**
     * Parse the value as a cseq value. This method assumes that you have already parsed out the
     * actual header name "CSeq: "
     * 
     * @param value
     * @return
     * @throws SipParseException
     */
    public static CSeqHeader frame(final Buffer value) throws SipParseException {
        try {
            final Buffer valueCopy = value.slice();
            final Buffer cseq = SipParser.expectDigit(value);
            final long number = Long.parseLong(cseq.toString());
            SipParser.consumeWS(value);
            final Buffer method = value.readLine();
            return new CSeqHeaderImpl(number, method, valueCopy);
        } catch (final IOException e) {
            throw new SipParseException(value.getReaderIndex(),
                    "Could not read from the underlying stream while parsing method");
        }
    }

    static CSeqHeaderBuilder with() {
        return new CSeqHeaderBuilder();
    }

    static class CSeqHeaderBuilder {

        private long cseq;
        private Buffer method;

        private CSeqHeaderBuilder() {
            // left empty intentionally
        }

        /**
         * 
         * @param cseq
         * @return
         * @throws SipParseException in case the specified sequence number is less than zero.
         */
        public CSeqHeaderBuilder cseq(final long cseq) throws SipParseException {
            assertArgument(cseq >= 0, "Sequence number must be greater or equal to zer");
            this.cseq = cseq;
            return this;
        }

        public CSeqHeaderBuilder method(final Buffer method) throws SipParseException {
            this.method = assertNotEmpty(method, "Method cannot be null or empty");
            return this;
        }

        public CSeqHeaderBuilder method(final String method) throws SipParseException {
            this.method = Buffers.wrap(assertNotEmpty(method, "Method cannot be null or empty"));
            return this;
        }

        public CSeqHeader build() {
            assertNotEmpty(this.method, "Method cannot be null or empty");
            return new CSeqHeaderImpl(cseq, method, null);
        }

    }

}
