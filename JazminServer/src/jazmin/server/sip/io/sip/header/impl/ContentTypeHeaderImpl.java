/**
 * 
 */
package jazmin.server.sip.io.sip.header.impl;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.header.ContentTypeHeader;


/**
 * @author jonas@jonasborjesson.com
 */
public final class ContentTypeHeaderImpl extends MediaTypeHeaderImpl implements ContentTypeHeader {

    /**
     * @param name
     * @param params
     */
    public ContentTypeHeaderImpl(final Buffer mType, final Buffer subType, final Buffer params) {
        super(ContentTypeHeader.NAME, mType, subType, params);
    }

    @Override
    public ContentTypeHeader clone() {
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        try {
            return ContentTypeHeader.frame(buffer);
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the ContentType-header", e);
        }
    }

    @Override
    public ContentTypeHeader ensure() {
        return this;
    }
}
