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
 * This class implements the USE-CANDIDATE attribute.<br>
 * This attribute is an extension to the original STUN protocol.
 * 
 * This is used only during an ICE implementation.<br>
 * This attribute serves as only a flag, it does not have any data so the data
 * length is zero
 */
public class UseCandidateAttribute extends StunAttribute {

	private static final String NAME = "USE-CANDIDATE";
	private static final char DATA_LENGTH = 0;

	public UseCandidateAttribute() {
		super(StunAttribute.USE_CANDIDATE);
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
		if (other == null || !(other instanceof UseCandidateAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		UseCandidateAttribute useCandidateAtt = (UseCandidateAttribute) other;
		if (useCandidateAtt.getAttributeType() != this.getAttributeType()
				|| useCandidateAtt.getDataLength() != this.getDataLength()) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] encode() {
		char type = getAttributeType();
		byte[] binValue = new byte[HEADER_LENGTH + DATA_LENGTH];

		// Type
		binValue[0] = (byte) (type >> 8);
		binValue[1] = (byte) (type & 0x00FF);

		// Length
		binValue[2] = (byte) (DATA_LENGTH >> 8);
		binValue[3] = (byte) (DATA_LENGTH & 0x00FF);

		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		// Do nothing, body is empty
	}

}
