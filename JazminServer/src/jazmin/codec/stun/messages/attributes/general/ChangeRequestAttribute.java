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

public class ChangeRequestAttribute extends StunAttribute {

	public static final String NAME = "CHANGE-REQUEST";
	public static final char DATA_LENGTH = 4;

	private boolean changeAddress = false;
	private boolean changePort = false;

	public ChangeRequestAttribute() {
		super(StunAttribute.CHANGE_REQUEST);
	}

	public boolean isAddressChanging() {
		return this.changeAddress;
	}

	public void setAddressChanging(boolean change) {
		this.changeAddress = change;
	}

	public boolean isPortChanging() {
		return this.changePort;
	}

	public void setPortChanging(boolean change) {
		this.changePort = change;
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
		if (other == null || !(other instanceof ChangeRequestAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		ChangeRequestAttribute att = (ChangeRequestAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| att.isAddressChanging() != this.isAddressChanging()
				|| att.isPortChanging() != this.isPortChanging()) {
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
		binValue[4] = 0x00;
		binValue[5] = 0x00;
		binValue[6] = 0x00;
		binValue[7] = (byte) ((isAddressChanging() ? 4 : 0) + (isPortChanging() ? 2
				: 0));
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] attributeValue, char offset,
			char length) throws StunException {
		// first three bytes are not used
		offset += 3;

		setAddressChanging((attributeValue[offset] & 4) > 0);
		setPortChanging((attributeValue[offset] & 0x2) > 0);
	}

}
