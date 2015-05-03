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
 * The USERNAME attribute is used for message integrity.<br>
 * The value of USERNAME is a variable length value.
 */
public class UsernameAttribute extends StunAttribute {

	public static final String NAME = "USERNAME";

	private byte username[] = null;

	public UsernameAttribute() {
		super(StunAttribute.USERNAME);
	}

	public byte[] getUsername() {
		return (username == null) ? null : username.clone();
	}

	public void setUsername(byte[] username) {
		if (username == null) {
			this.username = null;
		} else {
			this.username = new byte[username.length];
			System.arraycopy(username, 0, this.username, 0, username.length);
		}
	}

	@Override
	public char getDataLength() {
		if (this.username == null) {
			return 0;
		}
		return (char) username.length;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof UsernameAttribute) || other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		UsernameAttribute att = (UsernameAttribute) other;
		if (att.getAttributeType() != getAttributeType()
				|| att.getDataLength() != getDataLength()
				|| !Arrays.equals(att.username, username)) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] encode() {
		char type = getAttributeType();
		byte binValue[] = new byte[HEADER_LENGTH + getDataLength()
		// add padding
				+ (4 - getDataLength() % 4) % 4];

		// Type
		binValue[0] = (byte) (type >> 8);
		binValue[1] = (byte) (type & 0x00FF);

		// Length
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);

		// username
		System.arraycopy(username, 0, binValue, 4, getDataLength());

		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		this.username = new byte[length];
		System.arraycopy(data, offset, this.username, 0, length);
	}

}
