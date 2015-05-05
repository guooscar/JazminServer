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
public class RtcpSenderReport extends RtcpReport {
	
	private static final boolean IS_SENDER = true;

	/** NTP timestamp */
	private long ntpSec;

	/** NTP fraction */
	private long ntpFrac;
	
	/** The middle 32 bits out of 64 in the NTP timestamp */
	private long ntpTs;

	/** RTP timestamp */
	private long rtpTs;

	/** packets sent */
	private long psent;

	/** octets sent */
	private long osent;

	protected RtcpSenderReport() {
		super();
	}

	public RtcpSenderReport(boolean padding, long ssrc, long ntpSec, long ntpFrac, long rtpTs, long psent, long osent) {
		super(padding, ssrc, RtcpHeader.RTCP_SR);
		this.ntpSec = ntpSec;
		this.ntpFrac = ntpFrac;
		this.ntpTs = NtpUtils.calculateLastSrTimestamp(ntpSec, ntpFrac);
		this.rtpTs = rtpTs;
		this.psent = psent;
		this.osent = osent;
	}

	protected int decode(byte[] rawData, int offSet) {

		int tmp = offSet;

		offSet = super.decode(rawData, offSet);

		this.ssrc |= rawData[offSet++] & 0xFF;
		this.ssrc <<= 8;
		this.ssrc |= rawData[offSet++] & 0xFF;
		this.ssrc <<= 8;
		this.ssrc |= rawData[offSet++] & 0xFF;
		this.ssrc <<= 8;
		this.ssrc |= rawData[offSet++] & 0xFF;

		this.ntpSec |= rawData[offSet++] & 0xFF;
		this.ntpSec <<= 8;
		this.ntpSec |= rawData[offSet++] & 0xFF;
		this.ntpSec <<= 8;
		this.ntpSec |= rawData[offSet++] & 0xFF;
		this.ntpSec <<= 8;
		this.ntpSec |= rawData[offSet++] & 0xFF;

		this.ntpFrac |= rawData[offSet++] & 0xFF;
		this.ntpFrac <<= 8;
		this.ntpFrac |= rawData[offSet++] & 0xFF;
		this.ntpFrac <<= 8;
		this.ntpFrac |= rawData[offSet++] & 0xFF;
		this.ntpFrac <<= 8;
		this.ntpFrac |= rawData[offSet++] & 0xFF;
		
		this.ntpTs = NtpUtils.calculateLastSrTimestamp(ntpSec, ntpFrac);

		this.rtpTs |= rawData[offSet++] & 0xFF;
		this.rtpTs <<= 8;
		this.rtpTs |= rawData[offSet++] & 0xFF;
		this.rtpTs <<= 8;
		this.rtpTs |= rawData[offSet++] & 0xFF;
		this.rtpTs <<= 8;
		this.rtpTs |= rawData[offSet++] & 0xFF;

		this.psent |= rawData[offSet++] & 0xFF;
		this.psent <<= 8;
		this.psent |= rawData[offSet++] & 0xFF;
		this.psent <<= 8;
		this.psent |= rawData[offSet++] & 0xFF;
		this.psent <<= 8;
		this.psent |= rawData[offSet++] & 0xFF;

		this.osent |= rawData[offSet++] & 0xFF;
		this.osent <<= 8;
		this.osent |= rawData[offSet++] & 0xFF;
		this.osent <<= 8;
		this.osent |= rawData[offSet++] & 0xFF;
		this.osent <<= 8;
		this.osent |= rawData[offSet++] & 0xFF;

		int tmpCount = 0;
		while ((offSet - tmp) < this.length) {
			RtcpReportBlock rtcpReceptionReportItem = new RtcpReportBlock();
			offSet = rtcpReceptionReportItem.decode(rawData, offSet);
			addReceiverReport(rtcpReceptionReportItem);
			tmpCount++;
		}

		return offSet;
	}

	protected int encode(byte[] rawData, int offSet) {
		int startPosition = offSet;

		offSet = super.encode(rawData, offSet);

		rawData[offSet++] = ((byte) ((this.ssrc & 0xFF000000) >> 24));
		rawData[offSet++] = ((byte) ((this.ssrc & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.ssrc & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.ssrc & 0x000000FF)));

		rawData[offSet++] = ((byte) ((this.ntpSec & 0xFF000000) >> 24));
		rawData[offSet++] = ((byte) ((this.ntpSec & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.ntpSec & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.ntpSec & 0x000000FF)));

		rawData[offSet++] = ((byte) ((this.ntpFrac & 0xFF000000) >> 24));
		rawData[offSet++] = ((byte) ((this.ntpFrac & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.ntpFrac & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.ntpFrac & 0x000000FF)));

		rawData[offSet++] = ((byte) ((this.rtpTs & 0xFF000000) >> 24));
		rawData[offSet++] = ((byte) ((this.rtpTs & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.rtpTs & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.rtpTs & 0x000000FF)));

		rawData[offSet++] = ((byte) ((this.psent & 0xFF000000) >> 24));
		rawData[offSet++] = ((byte) ((this.psent & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.psent & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.psent & 0x000000FF)));

		rawData[offSet++] = ((byte) ((this.osent & 0xFF000000) >> 24));
		rawData[offSet++] = ((byte) ((this.osent & 0x00FF0000) >> 16));
		rawData[offSet++] = ((byte) ((this.osent & 0x0000FF00) >> 8));
		rawData[offSet++] = ((byte) ((this.osent & 0x000000FF)));

		for (RtcpReportBlock report : this.reportBlocks) {
			if (report != null) {
				offSet = report.encode(rawData, offSet);
			} else {
				break;
			}
		}

		/* Reduce 4 octets of header and length is in terms 32bits word */
		this.length = (offSet - startPosition - 4) / 4;

		rawData[startPosition + 2] = ((byte) ((this.length & 0xFF00) >> 8));
		rawData[startPosition + 3] = ((byte) (this.length & 0x00FF));

		return offSet;
	}

	public long getNtpSec() {
		return ntpSec;
	}

	public long getNtpFrac() {
		return ntpFrac;
	}
	
	public long getNtpTs() {
		return ntpTs;
	}

	public long getRtpTs() {
		return rtpTs;
	}

	public long getPsent() {
		return psent;
	}

	public long getOsent() {
		return osent;
	}

	@Override
	public boolean isSender() {
		return IS_SENDER;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("SENDER REPORT:\n");
		builder.append("version=").append(this.version).append(", ");
		builder.append("padding=").append(this.padding).append(", ");
		builder.append("packet type=").append(this.packetType).append(", ");
		builder.append("length=").append(this.length).append(", ");
		builder.append("ssrc=").append(this.ssrc).append(", ");
		builder.append("ntp seconds=").append(this.ntpSec).append(", ");
		builder.append("ntp fraction=").append(this.ntpFrac).append(", ");
		builder.append("rtp timestamp=").append(this.rtpTs).append(", ");
		builder.append("packets sent=").append(this.psent).append(", ");
		builder.append("octets sent=").append(this.osent).append("\n");
		for (RtcpReportBlock rr : this.reportBlocks) {
			builder.append("\n").append(rr.toString());
		}
		return builder.toString();
	}
	
}
