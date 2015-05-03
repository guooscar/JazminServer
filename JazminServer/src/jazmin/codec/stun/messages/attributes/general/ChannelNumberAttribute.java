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
 * The CHANNEL-NUMBER attribute is used to known on which channel the TURN
 * client want to send data.
 */
public class ChannelNumberAttribute extends StunAttribute {

	public static final String NAME = "CHANNEL-NUMBER";
	public static final char DATA_LENGTH = 4;

	private char channelNumber;

	public ChannelNumberAttribute() {
		super(StunAttribute.CHANNEL_NUMBER);
		this.channelNumber = (char) 0;
	}

	public char getChannelNumber() {
		return channelNumber;
	}

	public void setChannelNumber(char channelNumber) {
		this.channelNumber = channelNumber;
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
		if (other == null || !(other instanceof ChannelNumberAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		ChannelNumberAttribute att = (ChannelNumberAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| att.channelNumber != this.channelNumber) {
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
		binValue[4] = (byte) ((this.channelNumber >> 8) & 0xff);
		binValue[5] = (byte) ((this.channelNumber) & 0xff);
		binValue[6] = 0x00;
		binValue[7] = 0x00;
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		if (length != DATA_LENGTH) {
			throw new StunException("Invalid Length: " + (int) length
					+ ". Should be: " + (int) DATA_LENGTH);
		}
		this.channelNumber = ((char) ((data[0] << 8) | (data[1] & 0xFF)));
	}
}
