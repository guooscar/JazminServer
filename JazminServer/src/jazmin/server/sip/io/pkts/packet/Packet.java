/**
 * 
 */
package jazmin.server.sip.io.pkts.packet;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import jazmin.server.sip.io.pkts.PcapOutputStream;
import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.sip.SipPacket;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * Represents a captured packet.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Packet extends Cloneable {

    /**
     * The arrival time of this packet in microseconds relative to epoch
     * (midnight UTC of January 1, 1970).
     * 
     * Note, since this returns with microseconds precision (which may or may
     * not be relevant depending on the hardware on which the packet was
     * captured on) and you wish to format this arrival time into a more human
     * readable format you could use the {@link SimpleDateFormat} but it can
     * only handle milliseconds precision (you will have to write your own date
     * formatter if you want microseconds).
     * 
     * Here is a snippet illustrating how to turn the arrival time of the packet
     * into a human readable date
     * 
     * <pre>
     * Packet p = ...;
     * SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
     * Date date = new Date(p.getArrivalTime() / 1000);
     * System.out.println("Arrival time: " + formatter.format(date));
     * </pre>
     * 
     * Note how an integer devision is performed on the arrival time to
     * "cut off" the microseconds from the time stamp
     * 
     * @return the arrival time of the packet in microseconds since the start of
     *         the epoch
     */
    long getArrivalTime();

    /**
     * Calling this method will force the packet to completely parse its data
     * and check so that all the information conforms to whatever rules this
     * packet needs to follow. E.g., if this happens to be a SIP packet, then it
     * will check if it has the mandatory headers etc.
     * 
     * Some simpler packets, such as the {@link IPPacket}, hardly does anything
     * in this method but more complex protocols such as SIP (once again), HTTP
     * etc can spend quite some time verifying everything, which is why you
     * don't want to do it unless you really have to.
     * 
     * In general, yajpcap has the philosophy of
     * "assume that everything is ok until things blow up and then deal with it"
     */
    void verify();

    /**
     * Write this packet to the {@link OutputStream}. Typically, the
     * {@link OutputStream} would be a {@link PcapOutputStream} so when writing
     * packets to this stream they will still be a valid pcap.
     * 
     * @param out
     * @throws IOException
     */
    void write(OutputStream out) throws IOException;

    /**
     * Writes this packet to the {@link OutputStream} with the supplied payload.
     * You can use this method to e.g. write a raw {@link UDPPacket} to the
     * stream with this payload. Note, if the {@link UDPPacket} already had a
     * payload it will be ignored so use this method with care.
     * 
     * @param out
     * @param payload
     * @throws IOException
     */
    void write(OutputStream out, Buffer payload) throws IOException;

    Packet clone();

    /**
     * Check whether this packet contains a particular protocol. This will cause
     * the packet to examine all the containing packets to check whether they
     * are indeed the protocol the user asked for.
     * 
     * @param p
     * @return
     * @throws IOException
     *             in case something goes wrong when framing the rest of the
     *             protocol stack
     * @throws Exception 
     */
    boolean hasProtocol(Protocol p) throws Exception;

    /**
     * Get the protocol of this frame.
     * 
     * @return
     */
    Protocol getProtocol();

    /**
     * Find the packet for protocol p.
     * 
     * @param p
     * @return the packets that encapsulates the protocol or null if this
     *         protocol doesn't exist
     * @throws IOException
     *             in case something goes wrong when framing the rest of the
     *             protocol stack
     * @throws Exception 
     */
    Packet getPacket(Protocol p) throws Exception;

    /**
     * Get the name of the packet. Wireshark will give you a short description
     * of all the known protocols within its "super" packet. E.g., if you
     * "click" on the Pcap Frame it will have a field called
     * "protocols in frame" and will display something like
     * "eth:ip:udp:sip:sdp", this function will return a short name like that.
     * 
     * @return
     */
    String getName();

    /**
     * Get the next frame, or null if there is none. Note, if there isn't
     * another frame but there is still raw data within this frame, it only
     * means that we didn't recognize the payload.
     * 
     * @return
     * @throws IOException
     * @throws Exception 
     */
    Packet getNextPacket() throws Exception;

    /**
     * Almost all packets have a parent, which is the encapsulating protocol.
     * E.g., the parent of a {@link SipPacket} is typically a
     * {@link TransportPacket} such as {@link UDPPacket} or a {@link TCPPacket}.
     * The parent of a {@link TransportPacket} is usually a {@link IPPacket} and
     * so on.
     * 
     * @return
     */
    Packet getParentPacket();

    /**
     * Get the payload of the frame. If null, then this frame doesn't have any
     * payload
     * 
     * @return
     */
    Buffer getPayload();

}
