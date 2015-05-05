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

import java.io.Serializable;

import jazmin.codec.rtp.RtpPacket;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;


/**
 * 
 * @author Amit Bhayani
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 */
public class RtcpPacket implements Serializable {
	
	private static final long serialVersionUID = -7175947723683038337L;

	private static final Logger logger = LoggerFactory.getLogger(RtcpPacket.class);

	/**
	 * Maximum number of reporting sources
	 */
	public static final int MAX_SOURCES = 31;
	
	private RtcpSenderReport senderReport = null;
	private RtcpReceiverReport receiverReport = null;
	private RtcpSdes sdes = null;
	private RtcpBye bye = null;
	private RtcpAppDefined appDefined = null;
	
	private int packetCount = 0;
	private int size = 0;
	
	public RtcpPacket() {

	}

	public RtcpPacket(RtcpSenderReport senderReport, RtcpReceiverReport receiverReport, RtcpSdes sdes, RtcpBye bye, RtcpAppDefined appDefined) {
		this.senderReport = senderReport;
		this.receiverReport = receiverReport;
		this.sdes = sdes;
		this.bye = bye;
		this.appDefined = appDefined;
	}
	
	public RtcpPacket(RtcpReport report, RtcpSdes sdes, RtcpBye bye) {
		if(report.isSender()) {
			this.senderReport = (RtcpSenderReport) report;
		} else {
			this.receiverReport = (RtcpReceiverReport) report;
		}
		this.sdes = sdes;
		this.bye = bye;
	}

	public RtcpPacket(RtcpReport report, RtcpSdes sdes) {
		this(report, sdes, null);
	}

	public int decode(byte[] rawData, int offSet) {
//		this.size = rawData.length - offSet;
		this.size = 0;
		while (offSet < rawData.length) {
			int type = rawData[offSet + 1] & 0x000000FF;
			switch (type) {
			case RtcpHeader.RTCP_SR:
				packetCount++;
				this.senderReport = new RtcpSenderReport();
				offSet = this.senderReport.decode(rawData, offSet);
				this.size += this.senderReport.length;
				break;
			case RtcpHeader.RTCP_RR:
				packetCount++;
				this.receiverReport = new RtcpReceiverReport();
				offSet = this.receiverReport.decode(rawData, offSet);
				this.size += this.receiverReport.length;
				break;
			case RtcpHeader.RTCP_SDES:
				packetCount++;
				this.sdes = new RtcpSdes();
				offSet = this.sdes.decode(rawData, offSet);
				this.size += this.sdes.length;
				break;
			case RtcpHeader.RTCP_APP:
				packetCount++;
				this.appDefined = new RtcpAppDefined();
				offSet = this.appDefined.decode(rawData, offSet);
				this.size += this.appDefined.length;
				break;
			case RtcpHeader.RTCP_BYE:
				packetCount++;
				this.bye = new RtcpBye();
				offSet = this.bye.decode(rawData, offSet);
				this.size += this.bye.length;
				break;
			default:				
				logger.error("Received type = "+type+" RTCP Packet decoding falsed. offSet = "+offSet +". Packet count = "+ packetCount);
				offSet = rawData.length;
				break;
			}
		}
		return offSet;
	}

	public int encode(byte[] rawData, int offSet) {
		int initalOffSet = offSet;
		if (this.senderReport != null) {
			packetCount++;
			offSet = this.senderReport.encode(rawData, offSet);
		}
		if (this.receiverReport != null) {
			packetCount++;
			offSet = this.receiverReport.encode(rawData, offSet);
		}
		if (this.sdes != null) {
			packetCount++;
			offSet = this.sdes.encode(rawData, offSet);
		}
		if (this.appDefined != null) {
			packetCount++;
			offSet = this.appDefined.encode(rawData, offSet);
		}
		if (this.bye != null) {
			packetCount++;
			offSet = this.bye.encode(rawData, offSet);
		}
		this.size = offSet - initalOffSet;
		return offSet;
	}
	
	public boolean isSender() {
		return this.senderReport != null;
	}
	
	public RtcpPacketType getPacketType() {
		if(this.bye == null) {
			return RtcpPacketType.RTCP_REPORT;
		}
		return RtcpPacketType.RTCP_BYE;
	}
	
	public RtcpReport getReport() {
		if(isSender()) {
			return this.senderReport;
		}
		return this.receiverReport;
	}

	public RtcpSenderReport getSenderReport() {
		return senderReport;
	}

	public RtcpReceiverReport getReceiverReport() {
		return receiverReport;
	}

	public RtcpSdes getSdes() {
		return sdes;
	}

	public RtcpBye getBye() {
		return bye;
	}
	
	public boolean hasBye() {
		return this.bye != null;
	}

	public RtcpAppDefined getAppDefined() {
		return appDefined;
	}

	public int getPacketCount() {
		return packetCount;
	}

	public int getSize() {
		return size;
	}
	//
	public static boolean canHandle(byte[] packet) {
		return canHandle(packet, packet.length, 0);
	}

	public static boolean canHandle(byte[] packet, int dataLength, int offset) {
		// RTP version field must equal 2
		int version = (packet[offset] & 0xC0) >> 6;
		if (version == RtpPacket.VERSION) {
			// The payload type field of the first RTCP packet in a compound
			// packet must be equal to SR or RR.
			int type = packet[offset + 1] & 0x000000FF;
			if (type == RtcpHeader.RTCP_SR || type == RtcpHeader.RTCP_RR) {
				/*
				 * The padding bit (P) should be zero for the first packet of a
				 * compound RTCP packet because padding should only be applied,
				 * if it is needed, to the last packet.
				 */
				int padding = (packet[offset] & 0x20) >> 5;
				if(padding == 0) {
					/*
					 * TODO The length fields of the individual RTCP packets must add
					 * up to the overall length of the compound RTCP packet as
					 * received. This is a fairly strong check.
					 */
					return true;
				}
			}
		}
		return false;
	}
	//
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		// Print RR/SR
		RtcpReport report = getReport();
		if(report != null) {
			builder.append(report.toString());
		}
		// Print SDES if exists
		if(this.sdes != null) {
			builder.append(this.sdes.toString());
		}
		// Print BYE if exists
		if(this.bye != null) {
			builder.append(bye.toString());
		}
		
		return builder.toString();
	}
}
