/**
 * 
 */
package jazmin.server.sip.io.sip.header.impl;

import static jazmin.server.sip.io.sip.impl.PreConditions.assertNotNull;

import java.util.function.Supplier;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.header.Parameters;


/**
 * @author jonas@jonasborjesson.com
 */
public abstract class ParametersImpl extends SipHeaderImpl implements Parameters {

    private final ParametersSupport support;

    /**
     * 
     * @param name
     * @param params
     */
    protected ParametersImpl(final Buffer name, final Buffer params) {
        super(name, null);
        this.support = new ParametersSupport(params);
    }

    @Override
    public Buffer getParameter(final Buffer name) throws SipParseException {
        return this.support.getParameter(name);
    }

    @Override
    public Buffer getParameter(final String name) throws SipParseException {
        return this.support.getParameter(name);
    }

    @Override
    public void setParameter(final Buffer name, final Buffer value) throws SipParseException,
    IllegalArgumentException {
        this.support.setParameter(name, value);
    }

    @Override
    public void setParameter(final Buffer name, final Supplier<Buffer> value) throws SipParseException,
    IllegalArgumentException {
        assertNotNull(value);
        this.support.setParameter(name, value.get());
    }

    /**
     * Will only return the parameters. Sub-classes will have to build up the
     * rest of the buffer {@inheritDoc}
     */
    @Override
    public Buffer getValue() {
        return this.support.toBuffer();
    }

    @Override
    protected void transferValue(final Buffer dst) {
        this.support.transferValue(dst);
    }

}
