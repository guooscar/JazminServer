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
 * The EVEN-PORT attribute is used to ask the TURN server to allocate an even
 * port and optionally allocate the next higher port number.
 * 
 * There are one flag supported : <br/>
 * R : ask to reserve a second port.<br/>
 */
public class EvenPortAttribute extends StunAttribute {

	public static final String NAME = "EVEN-PORT";
	public static final char DATA_LENGTH = 1;

	boolean rFlag = false;

	public EvenPortAttribute() {
		super(StunAttribute.EVEN_PORT);
	}

	/**
	 * Set the R flag.
	 * 
	 * @param rFlag
	 *            true of false
	 */
	public void setRFlag(boolean rFlag) {
		this.rFlag = rFlag;
	}

	/**
	 * Is the R flag set
	 * 
	 * @return true if it is, false otherwise
	 */
	public boolean isRFlag() {
		return rFlag;
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
		if (other == null || !(other instanceof EvenPortAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		EvenPortAttribute att = (EvenPortAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| att.rFlag != this.rFlag) {
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
		binValue[4] = (byte) (this.rFlag ? 1 << 8 : 0);
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		if (length != DATA_LENGTH) {
			throw new StunException("Invalid length:" + length
					+ ". Allowed data lenght is " + DATA_LENGTH);
		}
		this.rFlag = (data[0] & 0x80) > 0;
	}

}
