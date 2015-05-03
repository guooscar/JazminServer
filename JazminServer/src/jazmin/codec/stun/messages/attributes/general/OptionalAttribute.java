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
 * This class is used for representing attributes not explicitly supported by
 * the STUN stack.
 * <p>
 * Such attributes will generally be kept in binary form and won't be subdued to
 * any processing by the stack.<br>
 * One could use this class for both dealing with attributes in received
 * messages, and generating messages containing attributes not explicitly
 * supported by the stack.
 * </p>
 */
public class OptionalAttribute extends StunAttribute {

	private static final String NAME = "OPTIONAL";

	byte[] body = null;

	public OptionalAttribute(char attributeType) {
		super(attributeType);
	}

	public byte[] getBody() {
		if (this.body == null) {
			return null;
		}
		return body.clone();
	}

	public void setBody(byte[] body, int offset, int length) {
		this.body = new byte[length];
		System.arraycopy(body, offset, this.body, 0, length);
	}

	@Override
	public char getDataLength() {
		return (char) this.body.length;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof OptionalAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		OptionalAttribute att = (OptionalAttribute) other;
		return Arrays.equals(this.body, att.body);
	}

	@Override
	public byte[] encode() {
		char type = getAttributeType();
		byte binValue[] = new byte[HEADER_LENGTH + this.body.length];

		// Type
		binValue[0] = (byte) (type >> 8);
		binValue[1] = (byte) (type & 0x00FF);
		// Length
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);

		System.arraycopy(this.body, 0, binValue, HEADER_LENGTH,
				this.body.length);
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		this.body = new byte[length];
		System.arraycopy(data, offset, this.body, 0, length);
	}

}
