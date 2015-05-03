/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes.data;

import java.util.Arrays;

import jazmin.codec.stun.StunException;
import jazmin.codec.stun.messages.attributes.StunAttribute;

/**
 * The DATA attribute contains the data the client wants to relay to the TURN
 * server or the TURN server to forward the response data.
 * 
 * The value of DATA is variable length. Its length MUST be a multiple of 4
 * (measured in bytes) in order to guarantee alignment of attributes on word
 * boundaries.
 */
public class DataAttribute extends StunAttribute {

	public static final String NAME = "DATA";

	private byte data[] = null;
	private boolean padding;

	public DataAttribute() {
		this(true);
	}

	public DataAttribute(boolean padding) {
		super(StunAttribute.DATA);
		this.padding = padding;
	}

	public byte[] getData() {
		if (data == null) {
			return null;
		}
		return data.clone();
	}

	public void setData(byte[] data) {
		if (data == null) {
			this.data = null;
		} else {
			this.data = new byte[data.length];
			System.arraycopy(data, 0, this.data, 0, data.length);
		}
	}

	@Override
	public char getDataLength() {
		return (char) this.data.length;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof DataAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}

		DataAttribute att = (DataAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| !Arrays.equals(att.data, this.data)) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] encode() {
		char type = getAttributeType();
		byte binValue[] = new byte[HEADER_LENGTH + getDataLength()
				+ (padding ? (getDataLength() % 4) : 0)];
		// Type
		binValue[0] = (byte) (type >> 8);
		binValue[1] = (byte) (type & 0x00FF);
		// Length
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);
		// data
		System.arraycopy(data, 0, binValue, 4, getDataLength());
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		this.data = new byte[length];
		System.arraycopy(data, offset, this.data, 0, length);
	}
}
