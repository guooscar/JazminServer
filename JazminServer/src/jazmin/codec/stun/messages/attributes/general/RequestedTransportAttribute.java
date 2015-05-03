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
 * The REQUESTED-TRANSPORT attribute is used to allocate a TURN address of
 * certain transport protocol.
 * 
 * In the original TURN specification, only UDP is supported.<br>
 * Support of TCP is detailed in draft-ietf-behave-turn-tcp-07.
 */
public class RequestedTransportAttribute extends StunAttribute {

	public static final String NAME = "REQUESTED-TRANSPORT";
	public static final char DATA_LENGTH = 4;

	private static final byte UDP = 17;
	private static final byte TCP = 6;

	private byte requestedTransport;

	public RequestedTransportAttribute() {
		super(StunAttribute.REQUESTED_TRANSPORT);
		this.requestedTransport = UDP;
	}

	public byte getRequestedTransport() {
		return requestedTransport;
	}

	public void setRequestedTransport(byte transport) {
		this.requestedTransport = transport;
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
		if (other == null || !(other instanceof RequestedTransportAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		RequestedTransportAttribute att = (RequestedTransportAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| att.requestedTransport != this.requestedTransport) {
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
		binValue[4] = this.requestedTransport;
		binValue[5] = 0x00;
		binValue[6] = 0x00;
		binValue[7] = 0x00;
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		if (length != DATA_LENGTH) {
			throw new StunException("Invalid Length");
		}
		this.requestedTransport = data[0];
	}

}
