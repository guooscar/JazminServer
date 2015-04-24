/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.impl;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.frame.PcapRecordHeader;
import jazmin.server.sip.io.pkts.packet.MACPacket;
import jazmin.server.sip.io.pkts.packet.PCapPacket;
import jazmin.server.sip.io.pkts.packet.PacketFactory;
import jazmin.server.sip.io.pkts.packet.TransportPacket;
import jazmin.server.sip.io.pkts.packet.TransportPacketFactory;
import jazmin.server.sip.io.pkts.packet.UDPPacket;
import jazmin.server.sip.io.pkts.protocol.IllegalProtocolException;
import jazmin.server.sip.io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class TransportPacketFactoryImpl implements TransportPacketFactory {

    /**
     * Raw Ethernet II frame with a source and destination mac address of
     * 00:00:00:00:00:00 and the type is set to IP (0800 - the last two bytes).
     */
    private final byte[] ehternetII = new byte[] {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00 };

    /**
     * Raw IPv4 frame with source and destination IP:s set to 127.0.0.1 and a
     * protocol for UDP. The length and checksums must be corrected when
     * generating a new packet based on this template.
     */
    private final byte[] ipv4 = new byte[] {
            (byte) 0x45, (byte) 0x00, (byte) 0x01, (byte) 0xed, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x40, (byte) 0x11, (byte) 0x3a, (byte) 0xfe, (byte) 0x7f, (byte) 0x00, (byte) 0x00, (byte) 0x01,
            (byte) 0x7f, (byte) 0x00, (byte) 0x00, (byte) 0x01 };

    /**
     * Raw UDP frame where the source port is 5090 and the destination port
     * 5060. You will certainly have to change the length of the UDP frame based
     * on your payload but you also need to re-calculate the checksum. And you
     * probably want to
     */
    private final byte[] udp = new byte[] {
            (byte) 0x13, (byte) 0xe2, (byte) 0x13, (byte) 0xc4, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xec };

    /**
     * The total size of an empty UDP packet.
     */
    private final int udpLength = this.ehternetII.length + this.ipv4.length + this.udp.length;

    /**
     * A reference to the main {@link PacketFactory}
     */
    private final PacketFactory packetFactory;

    /**
     * 
     */
    public TransportPacketFactoryImpl(final PacketFactory factory) {
        this.packetFactory = factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportPacket create(final Protocol protocol, final String srcAddress, final int srcPort,
            final String destAddress, final int destPort,
            final Buffer payload) throws IllegalArgumentException, IllegalProtocolException {

        final TransportPacket pkt = createUdpInternal(payload);
        pkt.setDestinationIP(destAddress);
        pkt.setSourceIP(srcAddress);
        pkt.setDestinationPort(destPort);
        pkt.setSourcePort(srcPort);
        pkt.reCalculateChecksum();
        return pkt;
    }

    private UDPPacket createUdpInternal(final long ts, final Buffer payload) {
        final int payloadSize = payload != null ? payload.getReadableBytes() : 0;
        final Buffer ethernet = Buffers.wrapAndClone(this.ehternetII);
        final Buffer ipv4 = Buffers.wrapAndClone(this.ipv4);

        final PcapRecordHeader pcapRecordHeader = PcapRecordHeader.createDefaultHeader(ts);
        pcapRecordHeader.setCapturedLength(this.udpLength + payloadSize);
        pcapRecordHeader.setTotalLength(this.udpLength + payloadSize);

        final PCapPacket pkt = new PCapPacketImpl(pcapRecordHeader, null);
        final MACPacket mac = MACPacketImpl.create(pkt, ethernet);

        final IPPacketImpl ipPacket = new IPPacketImpl(mac, ipv4, 0, null);
        ipPacket.setTotalLength(ipv4.getReadableBytes());

        final UdpPacketImpl udp = new UdpPacketImpl(ipPacket, Buffers.wrap(new byte[8]), payload);
        udp.setLength(8 + payloadSize);
        return udp;
    }

    private UDPPacket createUdpInternal(final Buffer payload) {
        final long ts = System.currentTimeMillis();
        return createUdpInternal(ts, payload);
    }

    @Override
    public TransportPacket create(final Protocol protocol, final byte[] srcAddress, final int srcPort,
            final byte[] destAddress, final int destPort, final Buffer payload) throws IllegalArgumentException,
            IllegalProtocolException {
        final TransportPacket pkt = createUdpInternal(payload);
        pkt.setSourceIP(srcAddress[0], srcAddress[1], srcAddress[2], srcAddress[3]);
        pkt.setDestinationIP(destAddress[0], destAddress[1], destAddress[2], destAddress[3]);
        pkt.setDestinationPort(destPort);
        pkt.setSourcePort(srcPort);
        pkt.reCalculateChecksum();
        return pkt;
    }

    @Override
    public UDPPacket createUDP(final long ts, final Buffer payload) throws IllegalArgumentException,
            IllegalProtocolException {
        return createUdpInternal(ts, payload);
    }

    @Override
    public UDPPacket createUDP(final Buffer payload) throws IllegalArgumentException, IllegalProtocolException {
        return createUdpInternal(payload);
    }

}
