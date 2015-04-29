/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.header.impl;

import static jazmin.server.sip.io.pkts.buffer.Buffers.wrap;
import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.ifNull;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipParser;

/**
 * @author jonas@jonasborjesson.com
 */
public final class ParametersSupport {

    /**
     * This buffer is the full original slice of the parameters as we received them. We keep this
     * one around since it is very common in applications such as proxies etc that you only look at
     * the parameters but never actually change them so we want to keep this one around for
     * performance reasons. However, if there ever is a change we nullify this buffer so that we no
     * longer can use it ever.
     */
    private Buffer originalParams;

    /**
     * The buffer that contains all our parameters but as we consume them, they will be (well,
     * consumed) moved over to the parameter map for fast future access. Once all parameters have
     * been consumed, this buffer will actually be empty.
     * 
     * If we are asked to produce a buffer again through {@link #toBuffer()} or through
     * {@link #transferValue(Buffer)} we will serialize all parameters back to buffer form and
     * re-assign this params buffer. The {@link #paramMap} will be left untouched but unless there
     * is another change to the parameters we will just return the same params buffer again.
     */
    private Buffer params;

    private Map<Buffer, Buffer> paramMap;

    /**
     * If there is a change to the parameters that will force us to re-generate the full buffer
     * again.
     */
    private boolean isDirty;

    private final int estimatedSize = 0;

    /**
     * 
     * @param name
     * @param params
     */
    public ParametersSupport(final Buffer params) {
        if (params != null) {
            this.originalParams = params.slice();
            this.params = params;
        } else {
            this.originalParams = null;
            this.params = Buffers.EMPTY_BUFFER;
        }
    }

    /**
	 * @return the estimatedSize
	 */
	public int getEstimatedSize() {
		return estimatedSize;
	}

	public boolean hasParameter(final Buffer name) {
        return this.paramMap != null && this.paramMap.containsKey(name);
    }

    public Buffer getParameter(final Buffer name) throws SipParseException {
        if (name == null) {
            throw new IllegalArgumentException("The name of the parameter cannot be null");
        }

        if (this.paramMap != null && this.paramMap.containsKey(name)) {
            final Buffer value = this.paramMap.get(name);
            if (value == null) {
                return null;
            }
            return value.slice();
        }

        return consumeUntil(name);
    }

    /**
     * WARNING: should really only be used by internal implementations.
     * 
     * @return
     */
    public Set<Map.Entry<Buffer, Buffer>> getAllParameters() {
        consumeUntil(null);
        if (this.paramMap != null) {
            return this.paramMap.entrySet();
        }
        return null;
    }

    /**
     * Internal helper method that will consume all raw parameters until we find the specified name
     * or if the name is null, then that will be the same as "consume all".
     * 
     * @param name
     * @return
     */
    private Buffer consumeUntil(final Buffer name) {
        try {
            while (this.params.hasReadableBytes()) {
                SipParser.consumeSEMI(this.params);
                final Buffer[] keyValue = SipParser.consumeGenericParam(this.params);
                ensureParamsMap();
                final Buffer value = keyValue[1] == null ? Buffers.EMPTY_BUFFER : keyValue[1];
                this.paramMap.put(keyValue[0], value);

                if (name != null && name.equals(keyValue[0])) {
                    return value;
                }
            }
            return null;
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(this.params.getReaderIndex(),
                    "Unable to process the value due to a IndexOutOfBoundsException", e);
        } catch (final IOException e) {
            throw new SipParseException(this.params.getReaderIndex(),
                    "Could not read from the underlying stream while parsing the value");
        }

    }

    private void ensureParamsMap() {
        if (this.paramMap == null) {
            // default map size is 16 but params are rarely more than a few
            this.paramMap = new LinkedHashMap<Buffer, Buffer>(8);
        }
    }

    public Buffer getParameter(final String name) throws SipParseException {
        return getParameter(Buffers.wrap(name));
    }

    public void setParameter(final String name, final String value) throws SipParseException,
    IllegalArgumentException {
        setParameter(wrap(name), value == null ? Buffers.EMPTY_BUFFER : wrap(value));
    }

    public void setParameter(final Buffer name, final Buffer value) throws SipParseException,
    IllegalArgumentException {
        getParameter(name);
        ensureParamsMap();
        this.paramMap.put(name, ifNull(value, Buffers.EMPTY_BUFFER));
        this.isDirty = true;
        this.originalParams = null;
    }

    public Buffer toBuffer() {
        if (this.originalParams != null) {
            return this.originalParams.slice();
        }

        // hmmm... very side effect programming. Not nice at all.
        ensureParams();
        return this.params.slice();
    }

    /**
     * Make sure that the internal params buffer is actually valid etc.
     */
    private void ensureParams() {
        if (this.isDirty) {
            // note, it would only be dirty if we actually have inserted a value
            // so therefore no need to check that the parammap is null
            final Buffer restOfParams = this.params;
            this.params = allcoateNewParamBuffer();
            for (final Map.Entry<Buffer, Buffer> entry : this.paramMap.entrySet()) {
                this.params.write(SipParser.SEMI);
                final Buffer key = entry.getKey();
                final Buffer value = entry.getValue();
                key.getBytes(0, this.params);
                if (value != null && !value.isEmpty()) {
                    this.params.write(SipParser.EQ);
                    value.getBytes(0, this.params);
                }
            }
            this.paramMap.clear();
            restOfParams.getBytes(this.params);
            this.originalParams = this.params.slice();
            this.isDirty = false;
        }
    }

    public void transferValue(final Buffer dst) {
        if (this.isDirty) {
            ensureParams();
        }
        if (this.originalParams != null) {
            this.originalParams.getBytes(0, dst);
        } else {
            this.params.getBytes(0, dst);
        }
    }

    /**
     * Will create an appropriate sized buffer that will fit all parameters
     * 
     * @return
     */
    private Buffer allcoateNewParamBuffer() {
        // TODO: actually do what we claim that we are doing
        // and figure out how big of a buffer we need.
        return Buffers.createBuffer(256);
    }


}
