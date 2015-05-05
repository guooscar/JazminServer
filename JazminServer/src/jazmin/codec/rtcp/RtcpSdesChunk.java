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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author amit bhayani
 * 
 */
public class RtcpSdesChunk {

	public static final int MAX_ITEMS = 9;
	
	private long ssrc;

	private final List<RtcpSdesItem> rtcpSdesItems;

	private int itemCount = 0;

	public RtcpSdesChunk(long ssrc) {
		this.ssrc = ssrc;
		this.rtcpSdesItems = new ArrayList<RtcpSdesItem>(MAX_ITEMS);
	}

	protected RtcpSdesChunk() {
		this.rtcpSdesItems = new ArrayList<RtcpSdesItem>(MAX_ITEMS);
	}

	protected int decode(byte[] rawData, int offSet) {

		this.ssrc |= rawData[offSet++] & 0xFF;
		this.ssrc <<= 8;
		this.ssrc |= rawData[offSet++] & 0xFF;
		this.ssrc <<= 8;
		this.ssrc |= rawData[offSet++] & 0xFF;
		this.ssrc <<= 8;
		this.ssrc |= rawData[offSet++] & 0xFF;

		while (true) {
			RtcpSdesItem sdesItem = new RtcpSdesItem();
			offSet = sdesItem.decode(rawData, offSet);
			addRtcpSdesItem(sdesItem);

			if (RtcpSdesItem.RTCP_SDES_END == sdesItem.getType()) {
				break;
			}
		}

		return offSet;
	}

	public void addRtcpSdesItem(RtcpSdesItem rtcpSdesItem) {
		if(this.itemCount >= MAX_ITEMS) {
			throw new ArrayIndexOutOfBoundsException("Reached maximum number of items: "+ MAX_ITEMS);
		}
		this.rtcpSdesItems.add(rtcpSdesItem);
		this.itemCount++;
	}

	public long getSsrc() {
		return ssrc;
	}
	
	public String getCname() {
		for (RtcpSdesItem item : this.rtcpSdesItems) {
			if(RtcpSdesItem.RTCP_SDES_CNAME == item.getType()) {
				return item.getText();
			}
		}
		return "";
	}

	public RtcpSdesItem[] getRtcpSdesItems() {
		RtcpSdesItem[] items = new RtcpSdesItem[this.rtcpSdesItems.size()];
		return rtcpSdesItems.toArray(items);
	}

	public int getItemCount() {
		return itemCount;
	}

	protected int encode(byte[] rawData, int offSet) {

		int temp = offSet;

		rawData[offSet++] = ((byte) ((this.ssrc & 0xFF000000) >> 24));
		rawData[offSet++] = ((byte) ((this.ssrc & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.ssrc & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.ssrc & 0x000000FF)));

		for (RtcpSdesItem rtcpSdesItem : rtcpSdesItems) {
			if (rtcpSdesItem != null) {
				offSet = rtcpSdesItem.encode(rawData, offSet);
			} else {
				break;
			}
		}

		// This is End
		rawData[offSet++] = 0x00;

		int remainder = (offSet - temp) % 4;
		if (remainder != 0) {
			int pad = 4 - remainder;
			for (int i = 0; i < pad; i++) {
				rawData[offSet++] = 0x00;
			}
		}

		return offSet;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("SDES CHUNK:\n");
		builder.append("ssrc= ").append(this.ssrc).append(", ");
		builder.append("item count= ").append(this.itemCount);
		for (RtcpSdesItem item : this.rtcpSdesItems) {
			builder.append("\n").append(item.toString());
		}
		return builder.toString();
	}
}
