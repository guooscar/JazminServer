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
 * The NONCE attribute is used for authentication.
 */
public class NonceAttribute extends StunAttribute {

	public static final String NAME = "NONCE";

	private byte nonce[] = null;

	public NonceAttribute() {
		super(StunAttribute.NONCE);
	}

	public byte[] getNonce() {
		if (this.nonce == null) {
			return null;
		}
		return this.nonce.clone();
	}

	public void setNonce(byte[] nonce) {
		if (nonce == null) {
			this.nonce = null;
		}
		this.nonce = nonce.clone();
	}

	@Override
	public char getDataLength() {
		return (char) this.nonce.length;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof NonceAttribute)) {
			return false;
		}
		NonceAttribute att = (NonceAttribute) other;
		if (att.getAttributeType() == this.getAttributeType()
				&& att.getDataLength() == this.getDataLength()
				&& Arrays.equals(att.nonce, this.nonce)) {
			return true;
		}
		return false;
	}

	@Override
	public byte[] encode() {
		char type = getAttributeType();
		byte binValue[] = new byte[HEADER_LENGTH + getDataLength()
				+ (getDataLength() % 4)];

		// Type
		binValue[0] = (byte) (type >> 8);
		binValue[1] = (byte) (type & 0x00FF);

		// Length
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);

		// nonce
		System.arraycopy(this.nonce, 0, binValue, 4, (int) getDataLength());
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		this.nonce = new byte[length];
		System.arraycopy(data, offset, this.nonce, 0, length);
	}

}
