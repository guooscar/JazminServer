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



import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.bouncycastle.crypto.tls.DatagramTransport;

/**
 * Datagram Transport implementation that uses NIO instead of bocking IO.
 * 
 * @author Henrique Rosa
 * 
 */
public class NettyUdpTransport implements DatagramTransport {
	
	public static final int DEFAULT_MTU = 1500;
	private final static int MIN_IP_OVERHEAD = 20;
	private final static int MAX_IP_OVERHEAD = MIN_IP_OVERHEAD + 64;
	private final static int UDP_OVERHEAD = 8;
	public final static int MAX_DELAY = 4000;

	private int mtu;
	private final int receiveLimit;
	private final int sendLimit;
	private long startTime;
	ByteArrayBlockingQueue queue;
	Channel udpChannel;
	private InetSocketAddress remoteAddress;
	//
	public void write(byte []data)throws Exception{
		queue.put(data);
	}
	public NettyUdpTransport() {
		mtu=1400;
		queue=new ByteArrayBlockingQueue(3000);
		this.receiveLimit = Math.max(0, mtu - MIN_IP_OVERHEAD - UDP_OVERHEAD);
		this.sendLimit = Math.max(0, mtu - MAX_IP_OVERHEAD - UDP_OVERHEAD);
		this.startTime = System.currentTimeMillis();
	}
	//
	public void startHandshake(){
		startTime=System.currentTimeMillis();
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
		if (this.hasTimeout()) {
			throw new IllegalStateException("Handshake is taking too long! (>" + MAX_DELAY + "ms");
		}
		System.err.println("receive:"+off+"/"+len+"/"+waitMillis+"/"+queue.count);
		int readSize=Math.min(len,queue.count);
		byte bb[]=new byte[readSize];
		try {
			Thread.sleep(1000);
			queue.take(bb);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		System.arraycopy(bb, 0,buf,off,bb.length);
		System.err.println("receive:"+off+"/"+len+"/"+waitMillis+"/"+queue.count+"/>>>>>>>"+readSize);
		return readSize;
	}

	@Override
	public void send(byte[] buf, int off, int len) throws IOException {
		System.err.println("send:"+off+"/"+len);
		if (len > getSendLimit()) {
		}
		if (this.hasTimeout()) {
			throw new IllegalStateException("Handshake is taking too long! (>" + MAX_DELAY + "ms");
		}
		DatagramPacket dp=new DatagramPacket(Unpooled.wrappedBuffer(buf,off,len),remoteAddress);
		udpChannel.writeAndFlush(dp);
	}

	@Override
	public void close() throws IOException {
		System.err.println("close");
	}

	private boolean hasTimeout() {
		return (System.currentTimeMillis() - this.startTime) > MAX_DELAY;
	}

}
