/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.address.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.address.SipURI;
import jazmin.server.sip.io.pkts.packet.sip.header.impl.ParametersSupport;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipParser;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipURIImpl extends URIImpl implements SipURI {

    /**
     * The full raw sip(s) URI
     */
    private Buffer buffer;

    /**
     * The "userinfo" part.
     */
    private final Buffer userInfo;

    /**
     * The host.
     */
    private final Buffer host;

    /**
     * The port
     */
    private Buffer port;

    /**
     * contains the uri-parameters and/or headers.
     */
    private final ParametersSupport paramsSupport;

    /**
     * Flag indicating whether this is a sips uri or not.
     */
    private final boolean isSecure;

    /**
     * Flag telling us whether we need to rebuild the buffer because values have
     * changed.
     */
    private boolean isDirty = false;

    /**
     * 
     * @param isSips
     *            whether this is a sip or sips URL
     * @param userInfo
     *            contains the so-called "userinfo" portion. Typically this is
     *            just the user but can optionally contain a password as well.
     *            See {@link SipParser#consumeUserInfoHostPort(Buffer)} for more
     *            information.
     * @param hostPort
     *            contains the so-called "hostport", which is the domain +
     *            optional port.
     * @param paramsHeaders
     *            any uri-parameters or headers that were on the SIP uri will be
     *            in this buffer. If empty or null then there were none.
     * @param original
     *            the original buffer just because as long as no one is changing
     *            the content we can just return this buffer fast and easy.
     */
    public SipURIImpl(final boolean isSips, final Buffer userInfo, final Buffer host, final Buffer port,
            final Buffer paramsHeaders,
            final Buffer original) {
        super(isSips ? SipParser.SCHEME_SIPS : SipParser.SCHEME_SIP);
        this.isSecure = isSips;
        this.userInfo = userInfo;
        this.host = host;
        this.port = port;
        // note, need to split out the header portion (if there is one)
        this.paramsSupport = new ParametersSupport(paramsHeaders);

        if (original == null) {
            this.isDirty = true;
        }
        this.buffer = original;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSipURI() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    public void getBytes(final Buffer dst) {
        if (!this.isDirty) {
            this.buffer.getBytes(dst);
        } else {
            if (this.isSecure) {
                SipParser.SCHEME_SIPS_COLON.getBytes(0, dst);
            } else {
                SipParser.SCHEME_SIP_COLON.getBytes(0, dst);
            }
            if (this.userInfo != null) {
                this.userInfo.getBytes(0, dst);
                dst.write(SipParser.AT);
            }
            this.host.getBytes(0, dst);
            if (this.port != null) {
                dst.write(SipParser.COLON);
                this.port.getBytes(0, dst);
            }

            this.paramsSupport.transferValue(dst);
        }
    }

    @Override
    public SipURI clone() {
        try {
            if (!this.isDirty) {
                return SipURI.frame(this.buffer.clone());
            }
            return SipURI.frame(toBuffer());
        } catch (SipParseException | IndexOutOfBoundsException | IOException e) {
            // shouldn't really be able to happen
            throw new RuntimeException("Unable to clone the SipURI due to exception ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer toBuffer() {
        if (!this.isDirty) {
            return this.buffer;
        }

        // TODO: need a better strategy around this.
        // Probably want to create a dynamic buffer
        // implementation where this is only the initial size
        final Buffer buffer = Buffers.createBuffer(1024);
        getBytes(buffer);
        this.isDirty = false;
        this.buffer = buffer.slice();
        return buffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getUser() {
        // TODO: this is not 100% correct since it may
        // actually contain a password as well.
        if (this.userInfo != null) {
            return this.userInfo.slice();
        }

        return Buffers.EMPTY_BUFFER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getHost() {
        return this.host.slice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPort() {
        if (this.port == null) {
            return -1;
        }

        try {
            return this.port.parseToInt();
        } catch (final NumberFormatException e) {
            // all of this should already have
            // been checked so should be impossible
            throw new RuntimeException(
                    "The port could not be parsed as an integer. This should not be possible. The port was "
                            + this.port);
        } catch (final IOException e) {
            throw new RuntimeException("IOException while extracting out the port. This should not be possible.");
        }
    }

    @Override
    public void setPort(final int port) {
        this.isDirty = true;
        if (port < 0) {
            this.port = null;
        } else {
            this.port = Buffers.wrap(port);
        }
    }

    @Override
    public Buffer getTransportParam() throws SipParseException {
        return getParameter(SipParser.TRANSPORT);
    }

    @Override
    public Buffer getUserParam() throws SipParseException {
        return getParameter(SipParser.USER);
    }

    @Override
    public int getTTLParam() throws SipParseException {
        final Buffer buffer = getParameter(SipParser.TTL);
        if (buffer == null || buffer.isEmpty()) {
            return -1;
        }
        try {
            return buffer.parseToInt();
        } catch (final IOException e) {
            throw new SipParseException(0, "Unable to parse buffer to an int", e);
        }
    }

    @Override
    public Buffer getMAddrParam() throws SipParseException {
        return getParameter(SipParser.MADDR);
    }

    @Override
    public Buffer getMethodParam() throws SipParseException {
        return getParameter(SipParser.METHOD);
    }


    /**
     * Comparing two {@link SipURI}s aren't trivial and the full set of rules are described in
     * RFC3261 section 19.1.4
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        try {
            final SipURIImpl o = (SipURIImpl) other;

            // a SIP and SIPS URI are NEVER equal
            if (this.isSecure ^ o.isSecure()) {
                return false;
            }

            // For two URIs to be equal, the user, password, host, and port
            // components must match. Note that the user-info part (user + password) are
            // actually case sensitive. The rest isn't though.
            //
            // TODO: must handle escaped characters
            if (!this.getUser().equals(o.getUser())) {
                return false;
            }

            if (!this.getHost().equalsIgnoreCase(o.getHost())) {
                return false;
            }

            if (this.getPort() != o.getPort()) {
                return false;
            }

            // the specification doesn't call out transport but their examples do
            if (getTransportParam() == null ^ o.getTransportParam() == null) {
                return false;
            }

            if (getUserParam() == null ^ o.getUserParam() == null) {
                return false;
            }
            if (getTTLParam() != o.getTTLParam()) {
                return false;
            }
            if (getMethodParam() == null ^ o.getMethodParam() == null) {
                return false;
            }
            if (getMAddrParam() == null ^ o.getMAddrParam() == null) {
                return false;
            }

            if (this.paramsSupport != null && o.paramsSupport != null) {
                final Set<Map.Entry<Buffer, Buffer>> entries = this.paramsSupport.getAllParameters();
                if (entries != null) {
                    final Iterator<Map.Entry<Buffer, Buffer>> it = entries.iterator();
                    while (it.hasNext()) {
                        final Map.Entry<Buffer, Buffer> entry = it.next();
                        final Buffer key = entry.getKey();
                        final Buffer value = entry.getValue();
                        final Buffer bValue = o.getParameter(key);
                        if (o.paramsSupport.hasParameter(key)) {
                            if (value == null ^ bValue == null) {
                                return false;
                            }
                            if (!value.equalsIgnoreCase(bValue)) {
                                return false;
                            }
                        }
                    };
                }
            }



        } catch (ClassCastException | NullPointerException e) {
            return false;
        }

        // we made it!
        return true;
    }

    /**
     * Now, the hash-code doesn't actually have to be unique for every little parameter and detail
     * as the {@link #equals(Object)} method is checking, we just need to take enough stuff into
     * account to have a good enough spread and then the equals-method would be used to sort out any
     * ties.
     * 
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1 + getPort();
        result = prime * result + getUser().hashCode();
        // note that the host portion is case IN-sensitive
        result = prime * result + getHost().toString().toLowerCase().hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toBuffer().toString();
    }


    @Override
    public Buffer getParameter(final Buffer name) throws SipParseException, IllegalArgumentException {
        return this.paramsSupport.getParameter(name);
    }

    @Override
    public Buffer getParameter(final String name) throws SipParseException, IllegalArgumentException {
        return this.paramsSupport.getParameter(name);
    }

    @Override
    public void setParameter(final Buffer name, final Buffer value) throws SipParseException,
    IllegalArgumentException {
        this.isDirty = true;
        this.paramsSupport.setParameter(name, value);
    }

    @Override
    public void setParameter(final String name, final String value) throws SipParseException,
    IllegalArgumentException {
        this.isDirty = true;
        this.paramsSupport.setParameter(name, value);
    }


    @Override
    public void setParameter(final Buffer name, final int value) throws SipParseException, IllegalArgumentException {
        setParameter(name, Buffers.wrap(value));
    }

    @Override
    public void setParameter(final String name, final int value) throws SipParseException, IllegalArgumentException {
        this.setParameter(Buffers.wrap(name), Buffers.wrap(value));
    }
}
