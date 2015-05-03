/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes.general;

import java.util.Arrays;

import jazmin.codec.stun.StunException;
import jazmin.codec.stun.messages.attributes.StunAttribute;

/**
 * The SOFTWARE attribute contains a textual description of the software being
 * used by the software or the client, including manufacturer and version
 * number.
 * 
 * The attribute has no impact on operation of the protocol, and serves only as
 * a tool for diagnostic and debugging purposes.
 * 
 * The value of SOFTWARE is variable length. Its length MUST be a multiple of 4
 * (measured in bytes) in order to guarantee alignment of attributes on word
 * boundaries.
 */
public class SoftwareAttribute extends StunAttribute {

	private static final String NAME = "SOFTWARE";

	private byte[] software = null;

	public SoftwareAttribute() {
		super(StunAttribute.SOFTWARE);
	}

	public byte[] getSoftware() {
		if (software == null) {
			return null;
		}
		byte[] copy = new byte[software.length];
		System.arraycopy(software, 0, copy, 0, software.length);
		return software;
	}

	public void setSoftware(byte[] software) {
		if (software == null) {
			this.software = null;
		} else {
			this.software = new byte[software.length];
			System.arraycopy(software, 0, this.software, 0, software.length);
		}
	}

	@Override
	public char getDataLength() {
		if (this.software == null) {
			return 0;
		}
		return (char) this.software.length;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof SoftwareAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		SoftwareAttribute att = (SoftwareAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| !Arrays.equals(att.software, this.software)) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] encode() {
		char type = getAttributeType();
		// with padding
		byte binValue[] = new byte[HEADER_LENGTH + getDataLength()
				+ (4 - getDataLength() % 4) % 4];

		// Type
		binValue[0] = (byte) (type >> 8);
		binValue[1] = (byte) (type & 0x00FF);
		// Length
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);
		// software
		System.arraycopy(software, 0, binValue, 4, getDataLength());
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		this.software = new byte[length];
		System.arraycopy(data, offset, this.software, 0, length);
	}

}
