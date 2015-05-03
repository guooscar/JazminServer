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
 * This class is used to represent the PRIORITY attribute used for ICE
 * processing.
 * 
 * This attribute is not in the original specification of STUN It is added as an
 * extension to STUN to be used for ICE implementations
 * 
 * PRIORITY attribute contains a 32 bit priority value. It is used in STUN
 * binding requests sent from ICE-Agents to their peers.
 */
public class PriorityAttribute extends StunAttribute {

	private static final String NAME = "PRIORITY";
	private static final char DATA_LENGTH = 4;

	/**
	 * The priority value specified in the attribute.
	 * 
	 * An integer should be enough to store this value, but long is used, since
	 * candidate and candidate-pair classes use long to store priority values
	 */
	private long priority;

	public PriorityAttribute() {
		super(StunAttribute.PRIORITY);
		this.priority = 0;
	}

	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		// Priority must be between 1 and (2^31 - 1)
		if (priority <= 0 || priority > 0x7FFFFFFFL) {
			throw new IllegalArgumentException(
					"Priority must be between 0 and (2^31 - 1)");
		}
		this.priority = priority;
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
		if (other == null || !(other instanceof PriorityAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		PriorityAttribute att = (PriorityAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| (att.priority != this.priority)) {
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
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);

		// Priority
		binValue[4] = (byte) ((priority & 0xFF000000L) >> 24);
		binValue[5] = (byte) ((priority & 0x00FF0000L) >> 16);
		binValue[6] = (byte) ((priority & 0x0000FF00L) >> 8);
		binValue[7] = (byte) (priority & 0x000000FFL);

		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		long[] values = new long[4];
		// Reading in the network byte order (Big-Endian)
		values[0] = (long) ((data[offset++] & 0xff) << 24);
		values[1] = (long) ((data[offset++] & 0xff) << 16);
		values[2] = (long) ((data[offset++] & 0xff) << 8);
		values[3] = (long) (data[offset++] & 0xff);
		// reconstructing the priority value
		this.priority = values[0] | values[1] | values[2] | values[3];
	}

}
