/**
 * 
 */
package jazmin.server.sip.io.sip.header.impl;

import static jazmin.server.sip.io.sip.impl.PreConditions.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.header.Parameters;
import jazmin.server.sip.io.sip.header.SipHeader;
import jazmin.server.sip.io.sip.header.ViaHeader;
import jazmin.server.sip.io.sip.impl.SipParser;

/**
 * Not extending the {@link ParametersImpl} because the way we parse the
 * Via-header we have already parsed the parameters. This because the Via-header
 * requires the branch parameter to be there and as such the framing of the
 * Via-header is done in a way that would complain if there are no params etc.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class ViaHeaderImpl implements ViaHeader, SipHeader, Parameters {

    private static final Buffer BRANCH = Buffers.wrap("branch");
    private static final Buffer RECEIVED = Buffers.wrap("received");
    private static final Buffer RPORT = Buffers.wrap("rport");
   // private static final Buffer TTL = Buffers.wrap("ttl");

    /**
     * The original Via-header.
     */
    private final Buffer original;

    private final Buffer transport;

    private final Buffer host;

    private final Buffer rawPort;

    private int port = -2; // negative two indicates we haven't set it yet.

    /**
     * Contains a list of all the parameters. It may be more efficient to keep
     * them in a map but since there won't be that many I wouldn't bet too much
     * money on it. Sometimes simple is easier. Will probably do some
     * performance testing on that just out of curiosity at some point.
     */
    private final List<Buffer[]> params;

    private int indexOfBranch = -1;

    private int indexOfReceived = -1;

    private int indexOfRPort = -1;


    /**
     * Constructor mainly used by the {@link #frame(Buffer)} method.
     */
    public ViaHeaderImpl(final Buffer original, final Buffer transport, final Buffer host, final Buffer port,
            final List<Buffer[]> params) {
        this.original = original;
        this.transport = transport;
        this.host = host;
        this.rawPort = port;
        if (this.rawPort == null) {
            this.port = -1;
        }

        this.params = params;
    }

    /**
	 * @return the original
	 */
	public Buffer getOriginal() {
		return original;
	}

	/**
     * Constructor used mainly by the {@link HeaderFactoryImpl}
     * 
     * @param transport
     * @param host
     * @param port
     * @param branch
     */
    public ViaHeaderImpl(final Buffer transport, final Buffer host, final int port, final Buffer branch) {
        assert port >= -1;
        this.original = null;
        this.transport = transport;
        this.port = port;
        this.host = host;
        this.rawPort = null;
        this.params = new ArrayList<Buffer[]>();
        if (branch != null) {
            this.indexOfBranch = 0;
            this.params.add(new Buffer[] {BRANCH, branch});
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getParameter(final Buffer name) throws SipParseException, IllegalArgumentException {
        final int index = findParameter(name);
        if (index == -1) {
            return null;
        }

        return this.params.get(index)[1];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getParameter(final String name) throws SipParseException, IllegalArgumentException {
        return getParameter(Buffers.wrap(name));
    }

    @Override
    public void setParameter(final Buffer name, final Buffer value) throws SipParseException,
    IllegalArgumentException {
        final int index = findParameter(name);
        if (index == -1) {
            this.params.add(new Buffer[] { name, value });
        } else {
            this.params.get(index)[1] = value;
        }
    }

    @Override
    public void setParameter(final Buffer name, final Supplier<Buffer> value) throws SipParseException,
    IllegalArgumentException {
        assertNotNull(value);
        setParameter(name, value.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getName() {
        return ViaHeader.NAME;
    }

    @Override
    public Buffer getTransport() {
        return this.transport;
    }

    @Override
    public int getTTL() {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getValue() {
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        return buffer;
    }

    @Override
    public Buffer getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        if (this.port == -2) {
            // TODO: perhaps save it plus implement my own
            // to string function in buffer
            this.port = Integer.parseInt(this.rawPort.toString());
        }

        return this.port;
    }

    @Override
    public Buffer getReceived() {
        if (this.indexOfReceived == -1) {
            this.indexOfReceived = findParameter(RECEIVED);
        }
        if (this.indexOfReceived == -1) {
            return null;
        }
        return this.params.get(this.indexOfReceived)[1];
    }

    @Override
    public void setReceived(final Buffer received) {
        if (this.indexOfReceived == -1) {
            this.indexOfReceived = findParameter(RECEIVED);
        }
        if (this.indexOfReceived == -1) {
            this.indexOfReceived = this.params.size();
            this.params.add(new Buffer[] { RECEIVED, received });
        } else {
            this.params.get(this.indexOfReceived)[1] = received;
        }
    }

    @Override
    public boolean hasRPort() {
        if (this.indexOfRPort == -1) {
            this.indexOfRPort = findParameter(RPORT);
        }
        if (this.indexOfRPort == -1) {
            return false;
        }
        return true;
    }

    @Override
    public int getRPort() {
        if (this.indexOfRPort == -1) {
            this.indexOfRPort = findParameter(RPORT);
        }
        if (this.indexOfRPort == -1) {
            return -1;
        }
        final Buffer port = this.params.get(this.indexOfRPort)[1];
        if (port == null) {
            return -1;
        }
        // TODO: perhaps save it plus implement my own
        // to string function in buffer
        try {
            return port.parseToInt();
        } catch (final NumberFormatException e) {
            return -1;
        } catch (final IOException e) {
            return -1;
        }
    }

    @Override
    public void setRPort(final int port) {
        if (this.indexOfRPort == -1) {
            this.indexOfRPort = findParameter(RPORT);
        }
        if (this.indexOfRPort == -1) {
            this.indexOfRPort = this.params.size();
            this.params.add(new Buffer[] { RPORT, Buffers.wrap(port) });
        } else {
            this.params.get(this.indexOfRPort)[1] = Buffers.wrap(port);
        }
    }

    @Override
    public Buffer getBranch() {
        if (this.indexOfBranch == -1) {
            this.indexOfBranch = findParameter(BRANCH);
        }
        if (this.indexOfBranch == -1) {
            return null;
        }
        return this.params.get(this.indexOfBranch)[1];
    }

    private int findParameter(final Buffer param) {
        for (int i = 0; i < this.params.size(); ++i) {
            final Buffer[] keyValue = this.params.get(i);
            if (keyValue[0].equals(param)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void setBranch(final Buffer branch) {
        if (this.indexOfBranch == -1) {
            this.indexOfBranch = findParameter(BRANCH);
        }
        if (this.indexOfBranch == -1) {
            this.indexOfBranch = this.params.size();
            this.params.add(new Buffer[] {BRANCH, branch});
        } else {
            this.params.get(this.indexOfBranch)[1] = branch;
        }
    }

    @Override
    public boolean isUDP() {
        return SipParser.isUDP(this.transport);
    }

    @Override
    public boolean isTCP() {
        return SipParser.isTCP(this.transport);
    }

    @Override
    public boolean isTLS() {
        return SipParser.isTLS(this.transport);
    }

    @Override
    public boolean isSCTP() {
        return SipParser.isSCTP(this.transport);
    }

    /**
     * For a Via-header make sure that the branch parameter is present.
     * 
     * {@inheritDoc}
     */
    @Override
    public void verify() throws SipParseException {
        if (getBranch() == null) {
            throw new SipParseException(0, "Did not find the mandatory branch parameter. Via is illegal");
        }
    }

    @Override
    public String toString() {
        // TODO: need to do something else. This is probably
        // not that efficient but performance testing will reveal
        final Buffer buffer = Buffers.createBuffer(1024);
        getBytes(buffer);
        return buffer.toString();
    }

    @Override
    public void getBytes(final Buffer dst) {
        NAME.getBytes(0, dst);
        dst.write(SipParser.COLON);
        dst.write(SipParser.SP);
        transferValue(dst);
    }

    protected void transferValue(final Buffer dst) {
        SipParser.SIP2_0_SLASH.getBytes(0, dst);
        this.transport.getBytes(0, dst);
        dst.write(SipParser.SP);
        this.host.getBytes(0, dst);
        if (this.port == -2 && this.rawPort != null) {
            dst.write(SipParser.COLON);
            this.rawPort.getBytes(dst);
        } else if (this.port != -1) {
            dst.write(SipParser.COLON);
            dst.writeAsString(this.port);
        }

        for (final Buffer[] param : this.params) {
            dst.write(SipParser.SEMI);
            param[0].getBytes(0, dst);
            if (param[1] != null) {
                dst.write(SipParser.EQ);
                param[1].getBytes(0, dst);
            }
        }
    }

    @Override
    public ViaHeader clone() {
        // TODO: probably inefficient and could also be plain wrong in that
        // we may not generate a large enough buffer (probably less likely though).
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        try {
            return ViaHeader.frame(buffer);
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the Via-header", e);
        }
    }

    @Override
    public ViaHeader ensure() {
        return this;
    }

}
