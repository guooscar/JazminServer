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
 * The REALM attribute contains a text which meets the grammar for "realm value"
 * as described in RFC3261 but without the double quotes.
 */
public class RealmAttribute extends StunAttribute {

	public static final String NAME = "REALM";

	private byte realm[];

	public RealmAttribute() {
		super(StunAttribute.REALM);
	}

	public byte[] getRealm() {
		if (this.realm == null) {
			return null;
		}
		return this.realm.clone();
	}

	public void setRealm(byte[] realm) {
		if (realm == null) {
			this.realm = null;
		} else {
			this.realm = new byte[realm.length];
			System.arraycopy(realm, 0, this.realm, 0, realm.length);
		}
	}

	@Override
	public char getDataLength() {
		return (char) this.realm.length;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof RealmAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		RealmAttribute att = (RealmAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| !Arrays.equals(att.realm, this.realm)) {
			return false;
		}
		return true;
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
		// realm
		System.arraycopy(realm, 0, binValue, 4, getDataLength());
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		this.realm = new byte[length];
		System.arraycopy(data, offset, this.realm, 0, length);
	}

}
