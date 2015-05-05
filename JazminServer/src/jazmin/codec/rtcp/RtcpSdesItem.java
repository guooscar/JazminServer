/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package jazmin.codec.rtcp;

/**
 * 
 * @author amit bhayani
 * 
 */
public class RtcpSdesItem {

	public static final short RTCP_SDES_END = 0;
	public static final short RTCP_SDES_CNAME = 1;
	public static final short RTCP_SDES_NAME = 2;
	public static final short RTCP_SDES_EMAIL = 3;
	public static final short RTCP_SDES_PHONE = 4;
	public static final short RTCP_SDES_LOC = 5;
	public static final short RTCP_SDES_TOOL = 6;
	public static final short RTCP_SDES_NOTE = 7;
	public static final short RTCP_SDES_PRIV = 8;

	/*
	 * SDES item
	 */

	/* type of item (rtcp_sdes_type_t) */
	private int type = 0;

	/* length of item (in octets) */
	private int length = 0;

	/* text, not null-terminated */
	private String text = null;

	public RtcpSdesItem(short type, String text) {
		this.type = type;
		this.text = text;
	}

	protected RtcpSdesItem() {

	}

	protected int decode(byte[] rawData, int offSet) {
		this.type = rawData[offSet++] & 0xFF;

		if (type == RtcpSdesItem.RTCP_SDES_END) {
			while (offSet < rawData.length) {
				if (rawData[offSet] != 0x00) {
					break;
				}
				offSet++;
			}
			return offSet;
		}

		this.length = (short) rawData[offSet++] & 0xFF;

		byte[] chunkData = new byte[length];
		System.arraycopy(rawData, offSet, chunkData, 0, length);
		this.text = new String(chunkData);

		offSet += length;

		return offSet;

	}

	protected int encode(byte[] rawData, int offSet) {
		byte[] textData = this.text.getBytes();
		this.length = (short) textData.length;
		rawData[offSet++] = ((byte) ((this.type & 0x000000FF)));
		rawData[offSet++] = ((byte) ((this.length & 0x000000FF)));
		System.arraycopy(textData, 0, rawData, offSet, this.length);
		return (offSet + this.length);
	}

	public int getType() {
		return type;
	}

	public int getLength() {
		return length;
	}

	public String getText() {
		return text;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("SDES ITEM: \n");
		builder.append("type= ").append(resolveType(this.type)).append(", ");
		builder.append("value= ").append(this.text).append(", ");
		builder.append("length= ").append(this.length).append("\n");
		return builder.toString();
	}

	private String resolveType(int type) {
		switch (type) {
		case RTCP_SDES_END:
			return "END";
		case RTCP_SDES_CNAME:
			return "CNAME";
		case RTCP_SDES_NAME:
			return "NAME";
		case RTCP_SDES_EMAIL:
			return "EMAIL";
		case RTCP_SDES_PHONE:
			return "PHONE";
		case RTCP_SDES_LOC:
			return "LOC";
		case RTCP_SDES_TOOL:
			return "TOOL";
		case RTCP_SDES_NOTE:
			return "NOTE";
		case RTCP_SDES_PRIV:
			return "PRIV";
		default:
			return "UNKNOWN";
		}
	}
}
