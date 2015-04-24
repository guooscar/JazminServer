/**
 * 
 */
package jazmin.server.sip.io.pkts;

import java.io.IOException;
import java.io.OutputStream;

import jazmin.server.sip.io.pkts.frame.Frame;
import jazmin.server.sip.io.pkts.frame.PcapGlobalHeader;
import jazmin.server.sip.io.pkts.packet.Packet;


/**
 * @author jonas@jonasborjesson.com
 */
public class PcapOutputStream extends OutputStream {

    /**
     * The underlying {@link OutputStream} we will be using for writing the
     * actual data.
     */
    private final OutputStream out;

    /**
     * The {@link PcapGlobalHeader} that tells us how to write out the various
     * info to the stream such as the byte order.
     */
    private final PcapGlobalHeader pcapHeader;

    public static PcapOutputStream create(final PcapGlobalHeader pcapHeader, final OutputStream out) {
        if (out == null) {
            throw new IllegalArgumentException("The OutputStream cannot be null");
        }

        if (pcapHeader == null) {
            throw new IllegalArgumentException("The OutputStream cannot be null");
        }

        try {
            pcapHeader.write(out);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Could not write the pcapheader to the stream due to IOException.", e);
        }

        return new PcapOutputStream(pcapHeader, out);
    }


    /**
     * 
     */
    private PcapOutputStream(final PcapGlobalHeader pcapHeader, final OutputStream out) {
        this.out = out;
        this.pcapHeader = pcapHeader;
    }

    /**
     * Write a {@link Frame} to the outputstream.
     * 
     * @param frame
     *            the frame to write. If null is passed in, it will silently be
     *            ignored.
     */
    public void write(final Frame frame) throws IOException {
        if (frame == null) {
            return;
        }

        frame.write(this);
    }

    /**
     * Write a {@link Packet} to the outputstream.
     * 
     * @param packet
     *            the packet to write. If null is passed in, it will silently be
     *            ignored.
     */
    public void write(final Packet packet) throws IOException {
        if (packet == null) {
            return;
        }
        packet.write(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final int b) throws IOException {
        // TODO: should we allow this?
        this.out.write(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        super.flush();
        this.out.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        super.close();
        this.out.close();
    }

}
