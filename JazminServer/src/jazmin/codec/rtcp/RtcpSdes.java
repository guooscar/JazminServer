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
public class RtcpSdes extends RtcpHeader {

	/**
	 * SDES
	 */
	private final List<RtcpSdesChunk> sdesChunks;

	protected RtcpSdes() {
		this.sdesChunks = new ArrayList<RtcpSdesChunk>(RtcpPacket.MAX_SOURCES);
	}

	public RtcpSdes(boolean padding) {
		super(padding, RtcpHeader.RTCP_SDES);
		this.sdesChunks = new ArrayList<RtcpSdesChunk>(RtcpPacket.MAX_SOURCES);
	}

	protected int decode(byte[] rawData, int offSet) {
		int tmp = offSet;
		offSet = super.decode(rawData, offSet);

		while ((offSet - tmp) < this.length) {
			RtcpSdesChunk rtcpSdesChunk = new RtcpSdesChunk();
			offSet = rtcpSdesChunk.decode(rawData, offSet);
			this.sdesChunks.add(rtcpSdesChunk);
		}
		return offSet;
	}

	protected int encode(byte[] rawData, int offSet) {
		int startPosition = offSet;

		offSet = super.encode(rawData, offSet);
		for (RtcpSdesChunk rtcpSdesChunk : sdesChunks) {
			if (rtcpSdesChunk != null) {
				offSet = rtcpSdesChunk.encode(rawData, offSet);
			} else {
				break;
			}
		}

		/* Reduce 4 octest of header and length is in terms 32bits word */
		this.length = (offSet - startPosition - 4) / 4;

		rawData[startPosition + 2] = ((byte) ((this.length & 0xFF00) >> 8));
		rawData[startPosition + 3] = ((byte) (this.length & 0x00FF));

		return offSet;
	}

	public void addRtcpSdesChunk(RtcpSdesChunk rtcpSdesChunk) {
		if(this.count >= RtcpPacket.MAX_SOURCES) {
			throw new ArrayIndexOutOfBoundsException("Reached maximum number of chunks: "+ RtcpPacket.MAX_SOURCES);
		}
		this.sdesChunks.add(rtcpSdesChunk);
		this.count++;
	}

	public RtcpSdesChunk[] getSdesChunks() {
		RtcpSdesChunk[] chunks = new RtcpSdesChunk[this.sdesChunks.size()];
		return this.sdesChunks.toArray(chunks);
	}
	
	public String getCname() {
		for (RtcpSdesChunk chunk : this.sdesChunks) {
			String cname = chunk.getCname();
			if(cname != null && !cname.isEmpty()) {
				return cname;
			}
		}
		return "";
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("SDES:\n");
		builder.append("version= ").append(this.version).append(", ");
		builder.append("padding= ").append(this.padding).append(", ");
		builder.append("source count= ").append(this.count).append(", ");
		builder.append("packet type= ").append(this.packetType).append(", ");
		builder.append("length= ").append(this.length).append(", ");
		for (RtcpSdesChunk chunk : this.sdesChunks) {
			builder.append("\n").append(chunk.toString());
		}
		return builder.toString();
	}

}
