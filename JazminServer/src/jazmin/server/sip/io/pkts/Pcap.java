package jazmin.server.sip.io.pkts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.filters.Filter;
import jazmin.server.sip.io.pkts.filters.FilterException;
import jazmin.server.sip.io.pkts.filters.FilterFactory;
import jazmin.server.sip.io.pkts.filters.FilterParseException;
import jazmin.server.sip.io.pkts.frame.PcapGlobalHeader;
import jazmin.server.sip.io.pkts.framer.FramerManager;
import jazmin.server.sip.io.pkts.framer.PcapFramer;
import jazmin.server.sip.io.pkts.packet.Packet;

/**
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public class Pcap {

    private final PcapGlobalHeader header;
    private final Buffer buffer;
    private final FramerManager framerManager;

    /**
     * If the filter is set then only frames that are accepted by the filter
     * will be further processed.
     */
    private Filter filter = null;

    private final FilterFactory filterFactory = FilterFactory.getInstance();

    private Pcap(final PcapGlobalHeader header, final Buffer buffer) {
        assert header != null;
        assert buffer != null;
        this.header = header;
        this.buffer = buffer;
        this.framerManager = FramerManager.getInstance();
    }

    /**
     * It is possible to specify a filter so that only packets that matches the
     * filter will be passed onto the registered {@link PacketHandler}.
     * 
     * E.g., to only accept packets of type sip with the Call-ID of "123" you
     * could pass in the following filter:
     * 
     * "sip.Call-ID == 123"
     * 
     * @param expression
     *            the expression. If the expression is null or the empty string,
     *            it will silently be ignored.
     * @throws FilterParseException
     *             in case the expression is not a valid filter expression.
     */
    public void setFilter(final String expression) throws FilterParseException {
        if (expression != null && !expression.isEmpty()) {
            this.filter = this.filterFactory.createFilter(expression);
        }
    }

    public void loop(final PacketHandler callback) throws IOException {
        final ByteOrder byteOrder = this.header.getByteOrder();
        final PcapFramer framer = new PcapFramer(this.header, this.framerManager);

        Packet packet = null;
        while ((packet = framer.frame(null, this.buffer)) != null) {
            try {
                final long time = packet.getArrivalTime();
                this.framerManager.tick(time);
                if (this.filter == null) {
                    callback.nextPacket(packet);
                } else if (this.filter != null && this.filter.accept(packet)) {
                    callback.nextPacket(packet);
                }
            } catch (final FilterException e) {
                // TODO: use the callback instead to signal
                // exceptions
                System.err.println("WARN: the filter complained about the last frame. Msg (if any) - " +
                        e.getMessage());
            }
        }
    }

    /**
     * Create an {@link PcapOutputStream} based on this {@link Pcap}. The new
     * {@link PcapOutputStream} is configured to use the same
     * {@link PcapGlobalHeader} as the {@link Pcap} is using which means that
     * you can just safely write frames back out to this
     * {@link PcapOutputStream}. Good for those applications that needs to
     * filter a {@link Pcap} and write out new files.
     * 
     * @param out
     * @return
     * @throws IllegalArgumentException
     */
    public PcapOutputStream createOutputStream(final OutputStream out) throws IllegalArgumentException {
        return PcapOutputStream.create(this.header, out);
    }

    /**
     * Capture packets from the input stream
     * 
     * @param is
     * @return
     * @throws IOException
     */
    public static Pcap openStream(final InputStream is) throws IOException {
        final Buffer stream = Buffers.wrap(is);
        final PcapGlobalHeader header = PcapGlobalHeader.parse(stream);
        return new Pcap(header, stream);
    }

    /**
     * 
     * @param file
     *            the pcap file
     * @return a new {@link Pcap}
     * @throws FileNotFoundException
     *             in case the file doesn't exist.
     * @throws IOException
     */
    public static Pcap openStream(final File file) throws FileNotFoundException, IOException {
        final InputStream is = new FileInputStream(file);
        return openStream(is);
    }

    /**
     * 
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Pcap openStream(final String file) throws FileNotFoundException, IOException {
        return openStream(new File(file));
    }

    public void close() {
        // TODO
    }

}
