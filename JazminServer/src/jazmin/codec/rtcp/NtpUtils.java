/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package jazmin.codec.rtcp;

/**
 * Collection of utility functions that help compute NTP timestamps for RTCP
 * reports.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class NtpUtils {

	/**
	 * Calculates the time stamp of the last received SR.
	 * 
	 * @param ntp
	 *            The most significant word of the NTP time stamp
	 * @return The middle 32 bits out of 64 in the NTP timestamp received as
	 *         part of the most recent RTCP sender report (SR).
	 */
	public static long calculateLastSrTimestamp(long ntp1, long ntp2) {
		byte[] high = uIntLongToByteWord(ntp1);
		byte[] low = uIntLongToByteWord(ntp2);
		low[3] = low[1];
		low[2] = low[0];
		low[1] = high[3];
		low[0] = high[2];
		return bytesToUIntLong(low, 0);
	}
	
	/** 
	 * Converts an unsigned 32 bit integer, stored in a long, into an array of bytes.
	 * 
	 * @param j a long
	 * @return byte[4] representing the unsigned integer, most significant bit first. 
	 */
	private static byte[] uIntLongToByteWord(long j) {
		int i = (int) j;
		byte[] byteWord = new byte[4];
		byteWord[0] = (byte) ((i >>> 24) & 0x000000FF);
		byteWord[1] = (byte) ((i >> 16) & 0x000000FF);
		byteWord[2] = (byte) ((i >> 8) & 0x000000FF);
		byteWord[3] = (byte) (i & 0x00FF);
		return byteWord;
	}
	
	/** 
	 * Combines four bytes (most significant bit first) into a 32 bit unsigned integer.
	 * 
	 * @param bytes
	 * @param index of most significant byte
	 * @return long with the 32 bit unsigned integer
	 */
	private static long bytesToUIntLong(byte[] bytes, int index) {
		long accum = 0;
		int i = 3;
		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
			accum |= ( (long)( bytes[index + i] & 0xff ) ) << shiftBy;
			i--;
		}
		return accum;
	}
	
	
}
