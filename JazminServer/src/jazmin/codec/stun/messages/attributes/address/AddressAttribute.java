/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes.address;

import jazmin.codec.stun.StunException;
import jazmin.codec.stun.TransportAddress;
import jazmin.codec.stun.TransportAddress.TransportProtocol;
import jazmin.codec.stun.messages.attributes.StunAttribute;

/**
 * This class is used to represent Stun attributes that contain an address. Such
 * attributes are:
 *<p>
 * MAPPED-ADDRESS <br/>
 * RESPONSE-ADDRESS <br/>
 * SOURCE-ADDRESS <br/>
 * CHANGED-ADDRESS <br/>
 * REFLECTED-FROM <br/>
 * ALTERNATE-SERVER <br/>
 * XOR-PEER-ADDRESS <br/>
 * XOR-RELAYED-ADDRESS <br/>
 *</p>
 *<p>
 * The different attributes are distinguished by the attributeType of
 * org.ice4j.attribute.Attribute.
 *</p>
 *<p>
 * Address attributes indicate the mapped IP address and
 * port.  They consist of an eight bit address family, and a sixteen bit
 * port, followed by a fixed length value representing the IP address.
 *<code>
 *  0                   1                   2                   3          <br/>
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1        <br/>
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+       <br/>
 * |x x x x x x x x|    Family     |           Port                |       <br/>
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+       <br/>
 * |                             Address                           |       <br/>
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+       <br/>
 *                                                                         <br/>
 * </p>
 * <p>
 * The port is a network byte ordered representation of the mapped port.
 * The address family is always 0x01, corresponding to IPv4.  The first
 * 8 bits of the MAPPED-ADDRESS are ignored, for the purposes of
 * aligning parameters on natural boundaries.  The IPv4 address is 32
 * bits.
 * </p>
 */
public abstract class AddressAttribute extends StunAttribute {

	protected static final byte ADDRESS_FAMILY_IPV4 = 0x01;
	protected static final byte ADDRESS_FAMILY_IPV6 = 0x02;

	private static final char DATA_LENGTH_FOR_IPV4 = (char) 8;
	private static final char DATA_LENGTH_FOR_IPV6 = (char) 20;

	protected TransportAddress transportAddress;

	protected AddressAttribute(char attributeType) {
		super(attributeType);
		if (!isTypeValid(attributeType)) {
			throw new IllegalArgumentException("Invalid STUN attribut type: "
					+ (int) attributeType);
		}
	}

	/**
	 * Verifies that type is a valid address attribute type.
	 * 
	 * @param type
	 *            the type to test
	 * @return true if the type is a valid address attribute type and false
	 *         otherwise
	 */
	private boolean isTypeValid(char type) {
		return (type == MAPPED_ADDRESS || type == RESPONSE_ADDRESS
				|| type == SOURCE_ADDRESS || type == CHANGED_ADDRESS
				|| type == REFLECTED_FROM || type == XOR_MAPPED_ADDRESS
				|| type == ALTERNATE_SERVER || type == XOR_PEER_ADDRESS
				|| type == XOR_RELAYED_ADDRESS || type == DESTINATION_ADDRESS);
	}

	@Override
	protected void setAttributeType(char type) {
		if (!isTypeValid(type)) {
			throw new IllegalArgumentException("Invalid STUN attribut type: "
					+ (int) type);
		}
		super.setAttributeType(type);
	}

	public TransportAddress getAddress() {
		return this.transportAddress;
	}

	public void setAddress(TransportAddress address) {
		this.transportAddress = address;
	}

	public int getPort() {
		return this.transportAddress.getPort();
	}

	/**
	 * Returns the bytes of the address.
	 * 
	 * @return the <tt>byte[]</tt> array containing the address.
	 */
	public byte[] getAddressBytes() {
		return this.transportAddress.getAddressBytes();
	}

	/**
	 * Returns the family that the this.address belongs to.
	 * 
	 * @return the family that the this.address belongs to.
	 */
	public byte getFamily() {
		if (this.transportAddress.isIPv4()) {
			return ADDRESS_FAMILY_IPV4;
		}
		return ADDRESS_FAMILY_IPV6;
	}

	public char getDataLength() {
		if (getFamily() == ADDRESS_FAMILY_IPV4) {
			return DATA_LENGTH_FOR_IPV4;
		}
		return DATA_LENGTH_FOR_IPV6;
	}

	public byte[] encode() {
		char type = getAttributeType();
		byte binValue[] = new byte[HEADER_LENGTH + getDataLength()];

		// Type
		binValue[0] = (byte) (type >> 8);
		binValue[1] = (byte) (type & 0x00FF);
		// Length
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);
		// Not used
		binValue[4] = 0x00;
		// Family
		binValue[5] = getFamily();
		// port
		binValue[6] = (byte) (getPort() >> 8);
		binValue[7] = (byte) (getPort() & 0x00FF);

		// address
		if (this.transportAddress.isIPv4()) {
			System.arraycopy(getAddressBytes(), 0, binValue, 8, 4);
		} else {
			System.arraycopy(getAddressBytes(), 0, binValue, 8, 16);
		}
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] attributeValue, char offset,
			char length) throws StunException {
		// Skip through padding
		offset++;

		byte family = attributeValue[offset++];
		char port = ((char) ((attributeValue[offset++] << 8) | (attributeValue[offset++] & 0xFF)));
		int addressLength = (family == ADDRESS_FAMILY_IPV4) ? 4 : 16;
		byte addressData[] = new byte[addressLength];

		System.arraycopy(attributeValue, offset, addressData, 0, addressLength);
		TransportAddress address = new TransportAddress(
				new String(addressData), port, TransportProtocol.UDP);
		setAddress(address);
	}

	/**
	 * Compares two STUN Attributes. Attributes are considered equal when their
	 * type, length, and all data are the same.
	 * 
	 * @param other
	 *            the object to compare this attribute with.
	 * @return true if the attributes are equal and false otherwise.
	 */
	public boolean equals(Object other) {
		if (other == null || !(other instanceof AddressAttribute)) {
			return false;
		}

		if (other == this) {
			return true;
		}

		AddressAttribute att = (AddressAttribute) other;
		if (att.getAttributeType() != getAttributeType()
				|| att.getDataLength() != getDataLength()
				|| att.getFamily() != getFamily()
				|| (att.getAddress() != null && !this.transportAddress
						.equals(att.getAddress()))) {
			return false;
		}
		if (att.getAddress() == null && getAddress() == null) {
			return true;
		}
		return true;
	}

}
