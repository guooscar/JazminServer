/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages;

import java.util.Arrays;
import java.util.Random;

/**
 * Represents a STUN transaction ID.
 * 
 * @author Henrique Rosa
 */
public class StunTransactionId {

	public static final int DATA_LENGTH = 12;
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	private final byte value[];
	private int hashCode = 0;

	protected StunTransactionId() {
		this.value = new byte[DATA_LENGTH];
		generateValue();
		generateHashCode();
	}

	private void generateValue() {
		// the first nb/2 bytes of the id
		long left = System.currentTimeMillis();

		// the last nb/2 bytes of the id
		long right = RANDOM.nextLong();
		int b = DATA_LENGTH / 2;

		for (int i = 0; i < b; i++) {
			this.value[i] = (byte) ((left >> (i * 8)) & 0xFFl);
			this.value[i + b] = (byte) ((right >> (i * 8)) & 0xFFl);
		}
	}

	private int generateHashCode() {
		return (value[3] << 24 & 0xFF000000) | (value[2] << 16 & 0x00FF0000)
				| (value[1] << 8 & 0x0000FF00) | (value[0] & 0x000000FF);
	}

	public byte[] getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	public boolean equals(Object other) {
		// Compare by type
		if (other == null || !(other instanceof StunTransactionId)) {
			return false;
		}
		// Compare by reference
		if (this == other) {
			return true;
		}
		// Compare by value
		byte otherValue[] = ((StunTransactionId) other).value;
		return Arrays.equals(this.value, otherValue);
	}

	/**
	 * Compares two Transaction ID by value.
	 * 
	 * @param other
	 *            the id to compare with ours.
	 * @return true if targetID matches this transaction id.
	 */
	public boolean equals(byte[] other) {
		return Arrays.equals(this.value, other);
	}

	public String toString() {
		StringBuilder idStr = new StringBuilder("0x");
		for (int i = 0; i < this.value.length; i++) {
			if ((this.value[i] & 0xFF) <= 15) {
				idStr.append("0");
			}
			idStr.append(Integer.toHexString(this.value[i] & 0xff)
					.toUpperCase());
		}
		return idStr.toString();
	}
	
	public static String readableFormat(byte[] transactionId) {
		StringBuilder idStr = new StringBuilder("0x");
		for (int i = 0; i < transactionId.length; i++) {
			if ((transactionId[i] & 0xFF) <= 15) {
				idStr.append("0");
			}
			idStr.append(Integer.toHexString(transactionId[i] & 0xff)
					.toUpperCase());
		}
		return idStr.toString();
	}

}
