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
 * The RESERVATION-TOKEN attribute contains a token that identifies a
 * reservation port on a TURN server.
 * 
 * The value is on 64 bits (8 bytes).
 */
public class ReservationTokenAttribute extends StunAttribute {

	public static final String NAME = "RESERVATION-TOKEN";
	public static final char DATA_LENGTH = 8;

	private byte reservationToken[];

	public ReservationTokenAttribute() {
		super(StunAttribute.RESERVATION_TOKEN);
	}

	public byte[] getReservationToken() {
		if (reservationToken == null) {
			return null;
		}
		byte[] copy = new byte[reservationToken.length];
		System.arraycopy(reservationToken, 0, copy, 0, reservationToken.length);
		return reservationToken;
	}

	public void setReservationToken(byte[] reservationToken) {
		if (reservationToken == null) {
			this.reservationToken = null;
		} else {
			this.reservationToken = new byte[reservationToken.length];
			System.arraycopy(reservationToken, 0, this.reservationToken, 0,
					reservationToken.length);
		}
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
		if (other == null || !(other instanceof ReservationTokenAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		ReservationTokenAttribute att = (ReservationTokenAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| !Arrays.equals(att.reservationToken, this.reservationToken)) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] encode() {
		char type = getAttributeType();
		byte binValue[] = new byte[HEADER_LENGTH + 8];

		// Type
		binValue[0] = (byte) (type >> 8);
		binValue[1] = (byte) (type & 0x00FF);
		// Length
		binValue[2] = (byte) (8 >> 8);
		binValue[3] = (byte) (8 & 0x00FF);
		// reservationToken
		System.arraycopy(reservationToken, 0, binValue, 4, 8);

		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		// Where does the 8 come from?
		if (length != DATA_LENGTH) {
			throw new StunException("Length mismatch.");
		}
		reservationToken = new byte[length];
		System.arraycopy(data, offset, reservationToken, 0, length);
	}

}
