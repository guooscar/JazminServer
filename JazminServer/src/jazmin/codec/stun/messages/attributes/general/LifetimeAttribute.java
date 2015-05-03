/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes.general;

import jazmin.codec.stun.StunException;
import jazmin.codec.stun.messages.attributes.StunAttribute;

/**
 * The LIFETIME attribute is used to know the lifetime of TURN allocations.
 */
public class LifetimeAttribute extends StunAttribute {

	public static final String NAME = "LIFETIME";
	public static final char DATA_LENGTH = 4;

	private int lifetime;

	public LifetimeAttribute() {
		super(StunAttribute.LIFETIME);
		this.lifetime = 0;
	}

	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}

	public int getLifetime() {
		return lifetime;
	}

	@Override
	public char getDataLength() {
		return DATA_LENGTH;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof LifetimeAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		LifetimeAttribute att = (LifetimeAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| att.lifetime != this.lifetime) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] encode() {
		byte binValue[] = new byte[HEADER_LENGTH + DATA_LENGTH];

		// Type
		binValue[0] = (byte) (getAttributeType() >> 8);
		binValue[1] = (byte) (getAttributeType() & 0x00FF);
		// Length
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);
		// Data
		binValue[4] = (byte) ((lifetime >> 24) & 0xff);
		binValue[5] = (byte) ((lifetime >> 16) & 0xff);
		binValue[6] = (byte) ((lifetime >> 8) & 0xff);
		binValue[7] = (byte) ((lifetime) & 0xff);

		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		if (length != DATA_LENGTH) {
			throw new StunException("Invalid length:" + length);
		}

		this.lifetime = ((data[0] << 24) & 0xff000000)
				+ ((data[1] << 16) & 0x00ff0000)
				+ ((data[2] << 8) & 0x0000ff00) + (data[3] & 0x000000ff);
	}

}
