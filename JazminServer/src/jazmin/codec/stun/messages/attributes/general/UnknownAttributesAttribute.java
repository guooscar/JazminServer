/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes.general;

import java.util.ArrayList;
import java.util.Iterator;

import jazmin.codec.stun.StunException;
import jazmin.codec.stun.messages.attributes.StunAttribute;

public class UnknownAttributesAttribute extends StunAttribute {

	public static String NAME = "UNKNOWN-ATTRIBUTES";

	private ArrayList<Character> unknownAttributes;

	public UnknownAttributesAttribute() {
		super(StunAttribute.UNKNOWN_ATTRIBUTES);
		this.unknownAttributes = new ArrayList<Character>();
	}

	/**
	 * Returns the length (in bytes) of this attribute's body.
	 * 
	 * If the number of unknown attributes is an odd number, one of the
	 * attributes MUST be repeated in the list, so that the total length of the
	 * list is a multiple of 4 bytes.
	 * 
	 * @return the length of this attribute's value (a multiple of 4).
	 */
	@Override
	public char getDataLength() {
		if (this.unknownAttributes == null) {
			return 0;
		}

		char length = (char) unknownAttributes.size();
		if ((length % 2) != 0) {
			length++;
		}
		return (char) (length * 2);
	}

	/**
	 * Verifies whether the specified attributeID is contained by this
	 * attribute.
	 * 
	 * @param attributeID
	 *            the attribute id to look for.
	 * @return true if this attribute contains the specified attribute id.
	 */
	public boolean contains(char attributeID) {
		return unknownAttributes.contains(new Character(attributeID));
	}

	public void addAttributeId(char attributeId) {
		if (!contains(attributeId)) {
			unknownAttributes.add(new Character(attributeId));
		}
	}

	/**
	 * Returns an iterator over the list of attribute IDs contained by this
	 * attribute.
	 * 
	 * @return an iterator over the list of attribute IDs contained by this
	 *         attribute.
	 */
	public Iterator<Character> getAttributes() {
		return unknownAttributes.iterator();
	}

	/**
	 * Returns the number of attribute IDs contained by this class.
	 * 
	 * @return the number of attribute IDs contained by this class.
	 */
	public int getAttributeCount() {
		return unknownAttributes.size();
	}

	/**
	 * Returns the attribute id with index i.
	 * 
	 * @param index
	 *            the index of the attribute id to return.
	 * @return the attribute id with index i.
	 */
	public char getAttribute(int index) {
		return (unknownAttributes.get(index)).charValue();
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof UnknownAttributesAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		UnknownAttributesAttribute att = (UnknownAttributesAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| !this.unknownAttributes.equals(att.unknownAttributes)) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] encode() {
		byte binValue[] = new byte[getDataLength() + HEADER_LENGTH];
		int offset = 0;

		// Type
		binValue[offset++] = (byte) (getAttributeType() >> 8);
		binValue[offset++] = (byte) (getAttributeType() & 0x00FF);

		// Length
		binValue[offset++] = (byte) (getDataLength() >> 8);
		binValue[offset++] = (byte) (getDataLength() & 0x00FF);

		Iterator<Character> attributes = getAttributes();
		while (attributes.hasNext()) {
			char att = attributes.next().charValue();
			binValue[offset++] = (byte) (att >> 8);
			binValue[offset++] = (byte) (att & 0x00FF);
		}

		// If the number of unknown attributes is an odd number, one of the
		// attributes MUST be repeated in the list, so that the total length of
		// the list is a multiple of 4 bytes.
		if (offset < binValue.length) {
			char att = getAttribute(0);
			binValue[offset++] = (byte) (att >> 8);
			binValue[offset++] = (byte) (att & 0x00FF);
		}

		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		if ((length % 2) != 0) {
			throw new StunException("Attribute IDs are 2 bytes long and the "
					+ "passed binary array has an odd length " + "value.");
		}
		char originalOffset = offset;
		for (int i = offset; i < originalOffset + length; i += 2) {
			char attributeID = (char) ((data[offset++] << 8) | (data[offset++]));
			addAttributeId(attributeID);
		}
	}

}
