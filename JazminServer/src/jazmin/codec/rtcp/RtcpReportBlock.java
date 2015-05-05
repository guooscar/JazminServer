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
public class RtcpReportBlock {

	/*
	 * Reception report block
	 */

	/* data source being reported */
	private long ssrc = 0;

	/* fraction lost since last SR/RR */
	private int fraction = 0;

	/* cumul. no. pkts lost (signed!) */
	private int lost = 0;

	/* corresponding count of sequence number cycles */
	private int SeqNumCycle;
	
	/* extended last seq. no. received */
	private long lastSeq = 0;

	/* interarrival jitter */
	private int jitter = 0;

	/* last SR packet from this source */
	private long lsr = 0;

	/* delay since last SR packet */
	private long dlsr = 0;

	protected RtcpReportBlock() {

	}

	public RtcpReportBlock(long ssrc, int fraction, int lost, int SeqNumCycle, long lastSeq, int jitter, long lsr, long dlsr) {
		this.ssrc = ssrc;
		this.fraction = fraction;
		this.lost = lost;
		this.SeqNumCycle = SeqNumCycle;
		this.lastSeq = lastSeq;
		this.jitter = jitter;
		this.lsr = lsr;
		this.dlsr = dlsr;
	}

	public long getSsrc() {
		return ssrc;
	}

	public int getFraction() {
		return fraction;
	}

	public int getLost() {
		return lost;
	}
	
	public int getSeqNumCycle() {
		return SeqNumCycle;
	}

	public long getLastSeq() {
		return lastSeq;
	}

	public int getJitter() {
		return jitter;
	}

	public long getLsr() {
		return lsr;
	}

	public long getDlsr() {
		return dlsr;
	}

	protected int decode(byte[] rawData, int offSet) {

		this.ssrc |= rawData[offSet++] & 0xFF;
		this.ssrc <<= 8;
		this.ssrc |= rawData[offSet++] & 0xFF;
		this.ssrc <<= 8;
		this.ssrc |= rawData[offSet++] & 0xFF;
		this.ssrc <<= 8;
		this.ssrc |= rawData[offSet++] & 0xFF;

		this.fraction = rawData[offSet++];

		this.lost |= rawData[offSet++] & 0xFF;
		this.lost <<= 8;
		this.lost |= rawData[offSet++] & 0xFF;
		this.lost <<= 8;
		this.lost |= rawData[offSet++] & 0xFF;

		this.SeqNumCycle |= rawData[offSet++] & 0xFF;
		this.SeqNumCycle <<= 8;
		this.SeqNumCycle |= rawData[offSet++] & 0xFF;
		
		
		this.lastSeq |= rawData[offSet++] & 0xFF;
		this.lastSeq <<= 8;
		this.lastSeq |= rawData[offSet++] & 0xFF;

		this.jitter |= rawData[offSet++] & 0xFF;
		this.jitter <<= 8;
		this.jitter |= rawData[offSet++] & 0xFF;
		this.jitter <<= 8;
		this.jitter |= rawData[offSet++] & 0xFF;
		this.jitter <<= 8;
		this.jitter |= rawData[offSet++] & 0xFF;

		this.lsr |= rawData[offSet++] & 0xFF;
		this.lsr <<= 8;
		this.lsr |= rawData[offSet++] & 0xFF;
		this.lsr <<= 8;
		this.lsr |= rawData[offSet++] & 0xFF;
		this.lsr <<= 8;
		this.lsr |= rawData[offSet++] & 0xFF;

		this.dlsr |= rawData[offSet++] & 0xFF;
		this.dlsr <<= 8;
		this.dlsr |= rawData[offSet++] & 0xFF;
		this.dlsr <<= 8;
		this.dlsr |= rawData[offSet++] & 0xFF;
		this.dlsr <<= 8;
		this.dlsr |= rawData[offSet++] & 0xFF;

		return offSet;
	}

	protected int encode(byte[] rawData, int offSet) {

		rawData[offSet++] = ((byte) ((this.ssrc & 0xFF000000) >> 24));
		rawData[offSet++] = ((byte) ((this.ssrc & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.ssrc & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.ssrc & 0x000000FF)));

		rawData[offSet++] = ((byte) ((this.fraction & 0x000000FF)));

		rawData[offSet++] = ((byte) ((this.lost & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.lost & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.lost & 0x000000FF)));

		//rawData[offSet++] = ((byte) ((this.lastSeq & 0xFF000000) >> 24));
		//rawData[offSet++] = ((byte) ((this.lastSeq & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.SeqNumCycle & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.SeqNumCycle & 0x000000FF)));
		
		rawData[offSet++] = ((byte) ((this.lastSeq & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.lastSeq & 0x000000FF)));

		rawData[offSet++] = ((byte) ((this.jitter & 0xFF000000) >> 24));
		rawData[offSet++] = ((byte) ((this.jitter & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.jitter & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.jitter & 0x000000FF)));

		rawData[offSet++] = ((byte) ((this.lsr & 0xFF000000) >> 24));
		rawData[offSet++] = ((byte) ((this.lsr & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.lsr & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.lsr & 0x000000FF)));

		rawData[offSet++] = ((byte) ((this.dlsr & 0xFF000000) >> 24));
		rawData[offSet++] = ((byte) ((this.dlsr & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.dlsr & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.dlsr & 0x000000FF)));

		return offSet;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("REPORT BLOCK: \n");
		builder.append("ssrc=").append(this.ssrc).append(", ");
		builder.append("fraction lost=").append(this.fraction).append(", ");
		builder.append("packets lots=").append(this.lost).append(", ");
		builder.append("extended highest seq number=").append(this.lastSeq).append(", ");
		builder.append("jitter=").append(this.jitter).append(", ");
		builder.append("last SR=").append(this.lsr).append(", ");
		builder.append("delay since last SR=").append(this.dlsr).append("\n");
		return builder.toString();
	}

}
