package jazmin.server.relay.webrtc;
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



import java.security.SecureRandom;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.relay.DtlsRelayChannel;

import org.bouncycastle.crypto.tls.DTLSServerProtocol;

/**
 * Handler to process DTLS-SRTP packets
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class DtlsHandler {

	private static final Logger logger = LoggerFactory.getLogger(DtlsHandler.class);
	//
	private DtlsSrtpServer server;
	private volatile boolean handshakeComplete;
	private volatile boolean handshakeFailed;
	private volatile boolean handshaking;
	private String hashFunction;
	private String remoteFingerprint;
	private String localFingerprint;
	/**
	 * Handles encryption of outbound RTP packets for a given RTP stream
	 * identified by its SSRC
	 * 
	 * @see http://tools.ietf.org/html/rfc5764#section-4.2
	 */
	private PacketTransformer srtpEncoder;

	/**
	 * Handles decryption of inbound RTP packets for a given RTP stream
	 * identified by its SSRC
	 * 
	 * @see http://tools.ietf.org/html/rfc5764#section-4.2
	 */
	private PacketTransformer srtpDecoder;

	/**
	 * Handles encryption of outbound RTCP packets for a given RTP stream
	 * identified by its SSRC
	 * 
	 * @see http://tools.ietf.org/html/rfc5764#section-4.2
	 */
	private PacketTransformer srtcpEncoder;

	/**
	 * Handles decryption of inbound RTCP packets for a given RTP stream
	 * identified by its SSRC
	 * 
	 * @see http://tools.ietf.org/html/rfc5764#section-4.2
	 */
	private PacketTransformer srtcpDecoder;
	private DtlsRelayChannel dtlsRelayChannel;

	public DtlsHandler(DtlsRelayChannel dtlsRelayChannel) {
		this.dtlsRelayChannel=dtlsRelayChannel;
		this.server = new DtlsSrtpServer();
		this.handshakeComplete = false;
		this.handshakeFailed = false;
		this.handshaking = false;
		this.hashFunction = "";
		this.remoteFingerprint = "";
		this.localFingerprint = "";
		logger.debug("local fingure print:"+getLocalFingerprint());
	}
	//
	public boolean isHandshakeComplete() {
		return handshakeComplete;
	}
	//
	public boolean isHandshakeFailed() {
		return handshakeFailed;
	}
	//
	public boolean isHandshaking() {
		return handshaking;
	}
	//
	public String getLocalFingerprint() {
		if(this.localFingerprint == null || this.localFingerprint.isEmpty()) {
			this.localFingerprint = this.server.generateFingerprint(this.hashFunction);
		}
		return this.localFingerprint;
	}
	//
	public void resetLocalFingerprint() {
		this.localFingerprint = "";
	}
	//
	public String getHashFunction() {
		return hashFunction;
	}
	//
	public String getRemoteFingerprintValue() {
		return remoteFingerprint;
	}
	//
	public String getRemoteFingerprint() {
		return hashFunction + " " + remoteFingerprint;
	}
	//
	public void setRemoteFingerprint(String hashFunction, String fingerprint) {
		this.hashFunction = hashFunction;
		this.remoteFingerprint = fingerprint;
	}
	//
	private byte[] getMasterServerKey() {
		return server.getSrtpMasterServerKey();
	}
	//
	private byte[] getMasterServerSalt() {
		return server.getSrtpMasterServerSalt();
	}
	//
	private byte[] getMasterClientKey() {
		return server.getSrtpMasterClientKey();
	}
	//
	private byte[] getMasterClientSalt() {
		return server.getSrtpMasterClientSalt();
	}
	//
	private SRTPPolicy getSrtpPolicy() {
		return server.getSrtpPolicy();
	}
	//
	private SRTPPolicy getSrtcpPolicy() {
		return server.getSrtcpPolicy();
	}
	//
	public PacketTransformer getSrtpDecoder() {
		return srtpDecoder;
	}
	//
	public PacketTransformer getSrtpEncoder() {
		return srtpEncoder;
	}
	//
	public PacketTransformer getSrtcpDecoder() {
		return srtcpDecoder;
	}
	//
	public PacketTransformer getSrtcpEncoder() {
		return srtcpEncoder;
	}

	/**
	 * Generates an SRTP encoder for outgoing RTP packets using keying material
	 * from the DTLS handshake.
	 */
	private PacketTransformer generateRtpEncoder() {
		return new SRTPTransformEngine(getMasterServerKey(),
				getMasterServerSalt(), getSrtpPolicy(), getSrtcpPolicy())
				.getRTPTransformer();
	}

	/**
	 * Generates an SRTP decoder for incoming RTP packets using keying material
	 * from the DTLS handshake.
	 */
	private PacketTransformer generateRtpDecoder() {
		return new SRTPTransformEngine(getMasterClientKey(),
				getMasterClientSalt(), getSrtpPolicy(), getSrtcpPolicy())
				.getRTPTransformer();
	}

	/**
	 * Generates an SRTCP encoder for outgoing RTCP packets using keying
	 * material from the DTLS handshake.
	 */
	private PacketTransformer generateRtcpEncoder() {
		return new SRTPTransformEngine(getMasterServerKey(),
				getMasterServerSalt(), getSrtpPolicy(), getSrtcpPolicy())
				.getRTCPTransformer();
	}

	/**
	 * Generates an SRTCP decoder for incoming RTCP packets using keying
	 * material from the DTLS handshake.
	 */
	private PacketTransformer generateRtcpDecoder() {
		return new SRTPTransformEngine(getMasterClientKey(),
				getMasterClientSalt(), getSrtpPolicy(), getSrtcpPolicy())
				.getRTCPTransformer();
	}

	/**
	 * Decodes an RTP Packet
	 * 
	 * @param packet
	 *            The encoded RTP packet
	 * @return The decoded RTP packet. Returns null is packet is not valid.
	 */
	public byte[] decodeRTP(byte[] packet, int offset, int length) {
		return this.srtpDecoder.reverseTransform(packet, offset, length);
	}

	/**
	 * Encodes an RTP packet
	 * 
	 * @param packet
	 *            The decoded RTP packet
	 * @return The encoded RTP packet
	 */
	public byte[] encodeRTP(byte[] packet, int offset, int length) {
		return this.srtpEncoder.transform(packet, offset, length);
	}

	/**
	 * Decodes an RTCP Packet
	 * 
	 * @param packet
	 *            The encoded RTP packet
	 * @return The decoded RTP packet. Returns null is packet is not valid.
	 */
	public byte[] decodeRTCP(byte[] packet, int offset, int length) {
		return this.srtcpDecoder.reverseTransform(packet, offset, length);
	}

	/**
	 * Encodes an RTCP packet
	 * 
	 * @param packet
	 *            The decoded RTP packet
	 * @return The encoded RTP packet
	 */
	public byte[] encodeRTCP(byte[] packet, int offset, int length) {
		return this.srtcpEncoder.transform(packet, offset, length);
	}

	public void handshake() {
		if (!handshaking && !handshakeComplete) {
			this.handshaking = true;
			SecureRandom secureRandom = new SecureRandom();
			DTLSServerProtocol serverProtocol =
					new DTLSServerProtocol(secureRandom);
			try {
				// Perform the handshake in a non-blocking fashion
				serverProtocol.accept(server, dtlsRelayChannel);
				// Prepare the shared key to be used in RTP streaming
				server.prepareSrtpSharedSecret();
				// Generate encoders for DTLS traffic
				if(server.getSrtpPolicy()!=null){
					srtpDecoder = generateRtpDecoder();
					srtpEncoder = generateRtpEncoder();
					srtcpDecoder = generateRtcpDecoder();
					srtcpEncoder = generateRtcpEncoder();
				}
				// Declare handshake as complete
				handshakeComplete = true;
				handshakeFailed = false;
				handshaking = false;
				// Warn listeners handshake completed
				dtlsRelayChannel.onDtlsHandshakeComplete();
			} catch (Exception e) {
				// Declare handshake as failed
				handshakeComplete = false;
				handshakeFailed = true;
				handshaking = false;
				// Warn listeners handshake completed
				dtlsRelayChannel.onDtlsHandshakeFailed(e);
			}
		}
	}

	public void reset() {
		this.server = new DtlsSrtpServer();
		this.srtcpDecoder = null;
		this.srtcpEncoder = null;
		this.srtpDecoder = null;
		this.srtpEncoder = null;
		this.hashFunction = "";
		this.remoteFingerprint = "";
		this.localFingerprint = "";
		this.handshakeComplete = false;
		this.handshakeFailed = false;
		this.handshaking = false;
	}

}
