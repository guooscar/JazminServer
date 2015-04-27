package jazmin.server.sip.io.pkts.packet.sip.header.impl;

import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.assertArgument;
import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.header.ContentLengthHeader;

public class ContentLengthHeaderImpl extends SipHeaderImpl implements ContentLengthHeader {

    private int contentLength;

    public ContentLengthHeaderImpl(final int contentLength) {
        super(ContentLengthHeader.NAME, null);
        this.contentLength = contentLength;
    }

    @Override
    public int getContentLength() {
        return this.contentLength;
    }

    @Override
    public void setContentLength(final int ct) {
        assertArgument(ct >= 0, "The value must be greater or equal to zero");
        this.contentLength = ct;
    }

    @Override
    public Buffer getValue() {
        return Buffers.wrap(this.contentLength);
    }

    @Override
    public ContentLengthHeader clone() {
        return new ContentLengthHeaderImpl(this.contentLength);
    }

    @Override
    public ContentLengthHeader ensure() {
        return this;
    }
}
