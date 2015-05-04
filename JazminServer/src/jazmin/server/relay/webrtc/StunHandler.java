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



import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Random;

import jazmin.codec.stun.StunException;
import jazmin.codec.stun.TransportAddress;
import jazmin.codec.stun.TransportAddress.TransportProtocol;
import jazmin.codec.stun.messages.StunMessage;
import jazmin.codec.stun.messages.StunMessageFactory;
import jazmin.codec.stun.messages.StunRequest;
import jazmin.codec.stun.messages.StunResponse;
import jazmin.codec.stun.messages.attributes.StunAttribute;
import jazmin.codec.stun.messages.attributes.StunAttributeFactory;
import jazmin.codec.stun.messages.attributes.general.MessageIntegrityAttribute;
import jazmin.codec.stun.messages.attributes.general.UsernameAttribute;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * Handles STUN traffic.
 * 
 * @author Henrique Rosa
 * 
 */
public class StunHandler {
	private static Logger logger=LoggerFactory.get(StunHandler.class);
	//
	byte []localKey;
	protected String ufrag;
	protected String password;
	//
	public StunHandler() {
		this.ufrag = new BigInteger(24, new Random()).toString(32);
		password= new BigInteger(128,new Random()).toString(32);
		this.localKey =password.getBytes();
	}
	//
	private byte[] processRequest(StunRequest request, InetSocketAddress remotePeer) throws IOException {
		if(logger.isDebugEnabled()){
			logger.debug("handle stun rquest from:{}/{}",remotePeer,request);
		}
		/*
		 * The agent MUST use a short-term credential to authenticate the
		 * request and perform a message integrity check.
		 */
		UsernameAttribute remoteUnameAttribute = (UsernameAttribute) request.getAttribute(StunAttribute.USERNAME);
		String remoteUsername = new String(remoteUnameAttribute.getUsername());
		// Produce Binding Response
		TransportAddress transportAddress = new TransportAddress(
				remotePeer.getAddress(), 
				remotePeer.getPort(), 
				TransportProtocol.UDP);
		StunResponse response = StunMessageFactory.createBindingResponse(request, transportAddress);
		byte[] transactionID = request.getTransactionId();
		try {
			response.setTransactionID(transactionID);
		} catch (StunException e) {
			throw new IOException("Illegal STUN Transaction ID: " + new String(transactionID), e);
		}
		//
		int colon = remoteUsername.indexOf(":");
		String localUFrag = remoteUsername.substring(0, colon);
		String remoteUfrag = remoteUsername.substring(colon);
		String localUsername = remoteUfrag.concat(":").concat(localUFrag);
		StunAttribute unameAttribute = StunAttributeFactory.createUsernameAttribute(localUsername);
		response.addAttribute(unameAttribute);
		//
		//byte[] localKey = this.iceAuthenticator.getLocalKey(localUFrag);
		MessageIntegrityAttribute messageIntegrityAttribute = 
				StunAttributeFactory.createMessageIntegrityAttribute(remoteUsername, localKey);
		response.addAttribute(messageIntegrityAttribute);
		// Pass response to the server
		if(logger.isDebugEnabled()){
			logger.debug("return stun response to:{}/{}",remotePeer,response);
		}
		//
		return response.encode();
	}
	/**
	 * @return the ufrag
	 */
	public String getUfrag() {
		return ufrag;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	//
	private byte[] processResponse(StunResponse response) {
		throw new UnsupportedOperationException("Support to handle STUN responses is not implemented.");
	}
	//
	public boolean canHandle(byte[] packet) {
		return canHandle(packet, packet.length, 0);
	}

	/*
	 * All STUN messages MUST start with a 20-byte header followed by zero or more Attributes.
	 * The STUN header contains a STUN message type, magic cookie, transaction ID, and message length.
	 * 
     *  0                   1                   2                   3
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |0 0|     STUN Message Type     |         Message Length        |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                         Magic Cookie                          |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                                                               |
     * |                     Transaction ID (96 bits)                  |
     * |                                                               |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * 
	 * @param data
	 * @param length
	 * @return
	 * @see <a href="http://tools.ietf.org/html/rfc5389#page-10">RFC5389</a>
	 */

	public boolean canHandle(byte[] data, int length, int offset) {
		/*
		 * All STUN messages MUST start with a 20-byte header followed by zero
		 * or more Attributes.
		 */
		if(length >= 20) {
			// The most significant 2 bits of every STUN message MUST be zeroes.
			byte b0 = data[offset];
			boolean firstBitsValid = ((b0 & 0xC0) == 0);
			
			// The magic cookie field MUST contain the fixed value 0x2112A442 in network byte order.
			boolean hasMagicCookie = data[offset + 4] == StunMessage.MAGIC_COOKIE[0]
					&& data[offset + 5] == StunMessage.MAGIC_COOKIE[1]
					&& data[offset + 6] == StunMessage.MAGIC_COOKIE[2]
					&& data[offset + 7] == StunMessage.MAGIC_COOKIE[3];
			return firstBitsValid && hasMagicCookie;
		}
		return false;
	}
	//
	public byte[] handle(byte[] packet,InetSocketAddress remotePeer) throws Exception {
		return handle(packet, packet.length, 0,remotePeer);
	}
	//
	public byte[] handle(byte[] packet, int dataLength, int offset,
			InetSocketAddress remotePeer) throws Exception {
		// Decode and process the packet
		StunMessage message;
		message = StunMessage.decode(packet, (char) offset, (char) dataLength);
		if (message instanceof StunRequest) {
			return processRequest((StunRequest) message, remotePeer);
		} else if (message instanceof StunResponse) {
			return processResponse((StunResponse) message);
		} else {
			return null;
		}
	}
}
