/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes.control;

import jazmin.codec.stun.StunException;
import jazmin.codec.stun.messages.attributes.StunAttribute;

public abstract class ControlAttribute extends StunAttribute {

	protected static final String ICE_CONTROLLED_NAME = "ICE_CONTROLLED";
	protected static final String ICE_CONTROLLING_NAME = "ICE_CONTROLLING";

	protected static final char DATA_LENGTH = 8;

	protected long tieBreaker;
	protected boolean controlling;

	protected ControlAttribute(boolean isControlling) {
		super(isControlling ? StunAttribute.ICE_CONTROLLING
				: StunAttribute.ICE_CONTROLLED);
		this.tieBreaker = -1;
		this.controlling = isControlling;
	}

	public long getTieBreaker() {
		return tieBreaker;
	}
	
	public void setTieBreaker(long tieBreaker) {
		this.tieBreaker = tieBreaker;
	}

	public boolean isControlling() {
		return controlling;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		// Reading in the network byte order (Big-Endian)
		tieBreaker = ((data[offset++] & 0xffl) << 56)
				| ((data[offset++] & 0xffl) << 48)
				| ((data[offset++] & 0xffl) << 40)
				| ((data[offset++] & 0xffl) << 32)
				| ((data[offset++] & 0xffl) << 24)
				| ((data[offset++] & 0xffl) << 16)
				| ((data[offset++] & 0xffl) << 8) | (data[offset++] & 0xffl);
	}

	@Override
	public char getDataLength() {
		return DATA_LENGTH;
	}

	@Override
	public String getName() {
		if (this.controlling) {
			return ICE_CONTROLLING_NAME;
		}
		return ICE_CONTROLLED_NAME;
	}

	@Override
	public byte[] encode() {
		char type = getAttributeType();
		byte[] binValue = new byte[HEADER_LENGTH + getDataLength()];
		// Type
		binValue[0] = (byte) (type >> 8);
		binValue[1] = (byte) (type & 0x00FF);
		// Length
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);
		// Tie-Breaker
		binValue[4] = (byte) ((tieBreaker & 0xFF00000000000000L) >> 56);
		binValue[5] = (byte) ((tieBreaker & 0x00FF000000000000L) >> 48);
		binValue[6] = (byte) ((tieBreaker & 0x0000FF0000000000L) >> 40);
		binValue[7] = (byte) ((tieBreaker & 0x000000FF00000000L) >> 32);
		binValue[8] = (byte) ((tieBreaker & 0x00000000FF000000L) >> 24);
		binValue[9] = (byte) ((tieBreaker & 0x0000000000FF0000L) >> 16);
		binValue[10] = (byte) ((tieBreaker & 0x000000000000FF00L) >> 8);
		binValue[11] = (byte) (tieBreaker & 0x00000000000000FFL);
		return binValue;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof ControlAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		ControlAttribute iceControlAtt = (ControlAttribute) other;
		if (iceControlAtt.getAttributeType() != this.getAttributeType()
				|| iceControlAtt.controlling != this.controlling
				|| iceControlAtt.getDataLength() != DATA_LENGTH
				|| iceControlAtt.getTieBreaker() != this.tieBreaker) {
			return false;
		}

		return true;
	}
}
