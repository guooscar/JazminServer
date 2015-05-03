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
 * The DONT-FRAGMENT attribute is used to inform TURN server (if it supports
 * this attribute) that it should set DF bit to 1 in IPv4 headers when relaying
 * client data.
 */
public class DontFragmentAttribute extends StunAttribute {

	public static final String NAME = "DONT-FRAGMENT";
	public static final char DATA_LENGTH = 0;

	public DontFragmentAttribute() {
		super(StunAttribute.DONT_FRAGMENT);
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
		if (other == null || !(other instanceof DontFragmentAttribute)) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] encode() {
		// there is no data
		byte binValue[] = new byte[HEADER_LENGTH];
		// Type
		binValue[0] = (byte) (getAttributeType() >> 8);
		binValue[1] = (byte) (getAttributeType() & 0x00FF);
		// Length
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		if (length != 0) {
			throw new StunException(
					"There is no data to decode so length must be zero.");
		}
	}

}
