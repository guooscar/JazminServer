package jazmin.server.relay.webrtc;
/**
 * Code derived and adapted from the Jitsi client side SRTP framework.
 * 
 * Distributed under LGPL license.
 * See terms of license at gnu.org.
 */


import java.nio.ByteBuffer;

/**
 * When using TransformConnector, a RTP/RTCP packet is represented using
 * RawPacket. RawPacket stores the buffer holding the RTP/RTCP packet, as well
 * as the inner offset and length of RTP/RTCP packet data.
 *
 * After transformation, data is also store in RawPacket objects, either the
 * original RawPacket (in place transformation), or a newly created RawPacket.
 *
 * Besides packet info storage, RawPacket also provides some other operations
 * such as readInt() to ease the development process.
 *
 * @author Werner Dittmann (Werner.Dittmann@t-online.de)
 * @author Bing SU (nova.su@gmail.com)
 * @author Emil Ivov
 * @author Damian Minkov
 * @author Boris Grozev
 * @author Lyubomir Marinov
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 */
public class RawPacket {
    /**
     * The size of the extension header as defined by RFC 3550.
     */
    public static final int EXT_HEADER_SIZE = 4;

    /**
     * The size of the fixed part of the RTP header as defined by RFC 3550.
     */
    public static final int FIXED_HEADER_SIZE = 12;

    /**
     * Byte array storing the content of this Packet
     */
    private ByteBuffer buffer;

    /**
     * Initializes a new empty <tt>RawPacket</tt> instance.
     */
    public RawPacket() {
    	this.buffer = ByteBuffer.allocateDirect(RtpPacket.RTP_PACKET_MAX_SIZE);
    }

    /**
     * Initializes a new <tt>RawPacket</tt> instance with a specific
     * <tt>byte</tt> array buffer.
     *
     * @param buffer the <tt>byte</tt> array to be the buffer of the new
     * instance 
     * @param offset the offset in <tt>buffer</tt> at which the actual data to
     * be represented by the new instance starts
     * @param length the number of <tt>byte</tt>s in <tt>buffer</tt> which
     * constitute the actual data to be represented by the new instance
     */
    public RawPacket(byte[] data, int offset, int length) {
    	this.buffer = ByteBuffer.allocateDirect(RtpPacket.RTP_PACKET_MAX_SIZE);
        wrap(data, offset, length);
    }
    
    public void wrap(byte[] data, int offset, int length) {
    	this.buffer.clear();
    	this.buffer.rewind();
    	this.buffer.put(data, offset, length);
    	this.buffer.flip();
    	this.buffer.rewind();
    }
    
    public byte[] getData() {
    	this.buffer.rewind();
    	byte[] data = new byte[this.buffer.limit()];
    	this.buffer.get(data, 0, data.length);
    	return data;
    }
    
    /**
     * Append a byte array to the end of the packet. This may change the data
     * buffer of this packet.
     *
     * @param data byte array to append
     * @param len the number of bytes to append
     */
    public void append(byte[] data, int len) {
        if (data == null || len <= 0 || len > data.length)  {
            throw new IllegalArgumentException("Invalid combination of parameters data and length to append()");
        }

        int oldLimit = buffer.limit();
        // grow buffer if necessary
        grow(len);
        // set positing to begin writing immediately after the last byte of the current buffer
        buffer.position(oldLimit);
        // set the buffer limit to exactly the old size plus the new appendix length
        buffer.limit(oldLimit + len);
        // append data
        buffer.put(data, 0, len);
    }

    /**
     * Get buffer containing the content of this packet
     *
     * @return buffer containing the content of this packet
     */
    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    /**
     * Returns <tt>true</tt> if the extension bit of this packet has been set
     * and <tt>false</tt> otherwise.
     *
     * @return  <tt>true</tt> if the extension bit of this packet has been set
     * and <tt>false</tt> otherwise.
     */
    public boolean getExtensionBit() {
    	buffer.rewind();
        return (buffer.get() & 0x10) == 0x10;
    }

    /**
     * Returns the length of the extensions currently added to this packet.
     *
     * @return the length of the extensions currently added to this packet.
     */
    public int getExtensionLength() {
		int length = 0;
		if (getExtensionBit()) {
			// the extension length comes after the RTP header, the CSRC list,
			// and after two bytes in the extension header called "defined by profile"
			int extLenIndex = FIXED_HEADER_SIZE + getCsrcCount() * 4 + 2;
			length = ((buffer.get(extLenIndex) << 8) | buffer.get(extLenIndex + 1) * 4);
		}
		return length;

    }

    /**
     * Returns the number of CSRC identifiers currently included in this packet.
     *
     * @return the CSRC count for this <tt>RawPacket</tt>.
     */
    public int getCsrcCount() {
    	this.buffer.rewind();
        return (this.buffer.get() & 0x0f);
    }

    /**
     * Get RTP header length from a RTP packet
     *
     * @return RTP header length from source RTP packet
     */
    public int getHeaderLength() {
    	int length = FIXED_HEADER_SIZE + 4 * getCsrcCount();
        if(getExtensionBit()) {
           length += EXT_HEADER_SIZE + getExtensionLength();
        }
        return length;
    }

    /**
     * Get the length of this packet's data
     *
     * @return length of this packet's data
     */
    public int getLength() {
        return this.buffer.limit();
    }

    /**
     * Get RTP padding size from a RTP packet
     *
     * @return RTP padding size from source RTP packet
     */
    public int getPaddingSize() {
    	buffer.rewind();
        if ((this.buffer.get() & 0x20) == 0) {
            return 0;
        }
        return this.buffer.get(this.buffer.limit());
    }

    /**
     * Get the RTP payload (bytes) of this RTP packet.
     *
     * @return an array of <tt>byte</tt>s which represents the RTP payload of
     * this RTP packet
     */
    public byte[] getPayload() {
        return readRegion(getHeaderLength(), getPayloadLength());
    }

    /**
     * Get RTP payload length from a RTP packet
     *
     * @return RTP payload length from source RTP packet
     */
    public int getPayloadLength() {
        return getLength() - getHeaderLength();
    }

    /**
     * Get RTP payload type from a RTP packet
     *
     * @return RTP payload type of source RTP packet
     */
    public byte getPayloadType() {
    	buffer.rewind();
        return (byte) (this.buffer.get(1) & (byte)0x7F);
    }

    /**
     * Get RTCP SSRC from a RTCP packet
     *
     * @return RTP SSRC from source RTP packet
     */
    public int getRTCPSSRC() {
        return readInt(4);
    }

    /**
     * Get RTP sequence number from a RTP packet
     *
     * @return RTP sequence num from source packet
     */
    public int getSequenceNumber()
    {
        return readUnsignedShortAsInt(2);
    }

    /**
     * Get SRTCP sequence number from a SRTCP packet
     *
     * @param authTagLen authentication tag length
     * @return SRTCP sequence num from source packet
     */
    public int getSRTCPIndex(int authTagLen) {
        int offset = getLength() - (4 + authTagLen);
        return readInt(offset);
    }

    /**
     * Get RTP SSRC from a RTP packet
     *
     * @return RTP SSRC from source RTP packet
     */
    public int getSSRC() {
        return readInt(8);
    }

    /**
     * Returns the timestamp for this RTP <tt>RawPacket</tt>.
     *
     * @return the timestamp for this RTP <tt>RawPacket</tt>.
     */
    public long getTimestamp() {
        return readInt(4);
    }

    /**
     * Grow the internal packet buffer.
     *
     * This will change the data buffer of this packet but not the
     * length of the valid data. Use this to grow the internal buffer
     * to avoid buffer re-allocations when appending data.
     *
     * @param delta number of bytes to grow
     */
	public void grow(int delta) {
		if (delta == 0) {
			return;
		}
		
		int newLen = buffer.limit() + delta;
		if (newLen <= buffer.capacity()) {
			// there is more room in the underlying reserved buffer memory
			buffer.limit(newLen);
			return;
		} else {
			// create a new bigger buffer
			ByteBuffer newBuffer = buffer.isDirect() ? ByteBuffer.allocateDirect(newLen) : ByteBuffer.allocate(newLen);
			buffer.rewind();
			newBuffer.put(buffer);
			newBuffer.limit(newLen);
			// switch to new buffer
			buffer = newBuffer;
		}
	}

    /**
     * Read a integer from this packet at specified offset
     *
     * @param off start offset of the integer to be read
     * @return the integer to be read
     */
	public int readInt(int off) {
		this.buffer.rewind();
		return ((buffer.get(off++) & 0xFF) << 24)
				| ((buffer.get(off++) & 0xFF) << 16)
				| ((buffer.get(off++) & 0xFF) << 8)
				| (buffer.get(off++) & 0xFF);
	}

    /**
     * Read a byte region from specified offset with specified length
     *
     * @param off start offset of the region to be read
     * @param len length of the region to be read
     * @return byte array of [offset, offset + length)
     */
    public byte[] readRegion(int off, int len) {
    	this.buffer.rewind();
        if (off < 0 || len <= 0 || off + len > this.buffer.limit()) {
            return null;
        }

        byte[] region = new byte[len];
        this.buffer.get(region, off, len);
        return region;
    }

	/**
	 * Read a byte region from specified offset in the RTP packet and with
	 * specified length into a given buffer
	 * 
	 * @param off
	 *            start offset in the RTP packet of the region to be read
	 * @param len
	 *            length of the region to be read
	 * @param outBuff
	 *            output buffer
	 */
	public void readRegionToBuff(int off, int len, byte[] outBuff) {
		assert off >= 0;
		assert len > 0;
		assert outBuff != null;
		assert outBuff.length >= len;
		assert buffer.limit() >= off + len;

		buffer.position(off);
		buffer.get(outBuff, 0, len);
	}
	
    /**
     * Read an unsigned short at specified offset as a int
     *
     * @param off start offset of the unsigned short
     * @return the int value of the unsigned short at offset
     */
    public int readUnsignedShortAsInt(int off) {
    	this.buffer.position(off);
        int b1 = (0x000000FF & (this.buffer.get()));
        int b2 = (0x000000FF & (this.buffer.get()));
        int val = b1 << 8 | b2;
        return val;
    }
    
    /**
     * Read an unsigned integer as long at specified offset
     *
     * @param off start offset of this unsigned integer
     * @return unsigned integer as long at offset
     */
    public long readUnsignedIntAsLong(int off) {
    	buffer.position(off);
        return (((long)(buffer.get() & 0xff) << 24) |
                ((long)(buffer.get() & 0xff) << 16) |
                ((long)(buffer.get() & 0xff) << 8) |
                ((long)(buffer.get() & 0xff))) & 0xFFFFFFFFL;
    }

    /**
     * Shrink the buffer of this packet by specified length
     *
     * @param len length to shrink
     */
    public void shrink(int delta) {
        if (delta <= 0) {
            return;
        }

        int newLimit = buffer.limit() - delta;
        if (newLimit <= 0) {
            newLimit = 0;
        }
        this.buffer.limit(newLimit);
    } 
    
}