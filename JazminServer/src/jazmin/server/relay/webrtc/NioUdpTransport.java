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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import org.bouncycastle.crypto.tls.DatagramTransport;

/**
 * Datagram Transport implementation that uses NIO instead of bocking IO.
 * 
 * @author Henrique Rosa
 * 
 */
public class NioUdpTransport implements DatagramTransport {
	
	private static final Logger logger = LoggerFactory.getLogger(NioUdpTransport.class);
	
	public static final int DEFAULT_MTU = 1500;

	private final static int MIN_IP_OVERHEAD = 20;
	private final static int MAX_IP_OVERHEAD = MIN_IP_OVERHEAD + 64;
	private final static int UDP_OVERHEAD = 8;
	public final static int MAX_DELAY = 4000;

	private final DatagramChannel channel;
	private int mtu;
	private final int receiveLimit;
	private final int sendLimit;

	private long startTime;

	public NioUdpTransport(DatagramChannel channel) {
		if (!channel.isConnected()) {
			throw new IllegalArgumentException("The datagram channel must be connected");
		}
		
		this.channel = channel;
		try {
			NetworkInterface inet = NetworkInterface.getByInetAddress(channel.socket().getLocalAddress());
			this.mtu = inet == null ? 0 : inet.getMTU();
		} catch (SocketException e) {
			logger.warn("Could not discover Network Interface for current channel, setting MTU to " + DEFAULT_MTU + ". Reason: "+ e.getMessage(), e);
			this.mtu = DEFAULT_MTU;
		}
		this.receiveLimit = Math.max(0, mtu - MIN_IP_OVERHEAD - UDP_OVERHEAD);
		this.sendLimit = Math.max(0, mtu - MAX_IP_OVERHEAD - UDP_OVERHEAD);
		
		this.startTime = System.currentTimeMillis();
	}

	public void start() {
		this.startTime = System.currentTimeMillis();
	}

	@Override
	public int getReceiveLimit() throws IOException {
		return this.receiveLimit;
	}

	@Override
	public int getSendLimit() throws IOException {
		return this.sendLimit;
	}

	@Override
	public int receive(byte[] buf, int off, int len, int waitMillis) throws IOException {
		// MEDIA-48: DTLS handshake thread does not terminate
		// https://telestax.atlassian.net/browse/MEDIA-48
		if (this.hasTimeout()) {
			throw new IllegalStateException("Handshake is taking too long! (>" + MAX_DELAY + "ms");
		}
		ByteBuffer buffer = ByteBuffer.wrap(buf, off, len);
		return this.channel.read(buffer);
	}

	@Override
	public void send(byte[] buf, int off, int len) throws IOException {
		if (len > getSendLimit()) {
			/*
			 * RFC 4347 4.1.1. "If the application attempts to send a record
			 * larger than the MTU, the DTLS implementation SHOULD generate an
			 * error, thus avoiding sending a packet which will be fragmented."
			 */
			// TODO Exception
		}
		// MEDIA-48: DTLS handshake thread does not terminate
		// https://telestax.atlassian.net/browse/MEDIA-48
		if (this.hasTimeout()) {
			throw new IllegalStateException("Handshake is taking too long! (>" + MAX_DELAY + "ms");
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(buf, off, len);
		this.channel.send(buffer, this.channel.getRemoteAddress());
	}

	@Override
	public void close() throws IOException {
		if (this.channel.isOpen()) {
			this.channel.close();
		}
	}

	private boolean hasTimeout() {
		return (System.currentTimeMillis() - this.startTime) > MAX_DELAY;
	}

}
