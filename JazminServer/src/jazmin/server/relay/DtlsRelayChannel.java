/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;

import java.io.IOException;

import jazmin.core.Jazmin;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.HexDump;
import jazmin.server.relay.webrtc.ByteArrayBlockingQueue;
import jazmin.server.relay.webrtc.DtlsHandler;
import jazmin.server.relay.webrtc.DtlsListener;
import jazmin.server.relay.webrtc.StunHandler;

import org.bouncycastle.crypto.tls.DatagramTransport;

/**
 * @author yama
 *
 */
public class DtlsRelayChannel extends NetworkRelayChannel 
implements DatagramTransport,DtlsListener{
	private static Logger logger=LoggerFactory.get(DtlsRelayChannel.class);
	//
	private DtlsHandler dtlsHandler;
	private StunHandler stunHandler;
	private long startHandleShakeTime=0;
	//
	public DtlsRelayChannel(String localAddress, int localPort) {
		super(TransportType.DTLS, localAddress, localPort);
		queue=new ByteArrayBlockingQueue(3000);
		//
		final int MIN_IP_OVERHEAD = 20;
		final int MAX_IP_OVERHEAD = MIN_IP_OVERHEAD + 64;
		final int UDP_OVERHEAD = 8;
		int mtu=1400;
		this.receiveLimit = Math.max(0, mtu - MIN_IP_OVERHEAD - UDP_OVERHEAD);
		this.sendLimit = Math.max(0, mtu - MAX_IP_OVERHEAD - UDP_OVERHEAD);
		stunHandler=new StunHandler();
		dtlsHandler=new DtlsHandler(this);
        dtlsHandler.setRemoteFingerprint("sha-256", "");
		dtlsHandler.addListener(this);
	}
	//
	@Override
	public void write(ByteBuf buffer) {
		if(outboundChannel.isActive()){
			ByteBuf buf= Unpooled.copiedBuffer(buffer);
			DatagramPacket dp=new DatagramPacket(
					buf,
					remoteAddress);
			packetSentCount++;
			byteSentCount+=buffer.capacity();
			outboundChannel.writeAndFlush(dp);
		}
	}
	//
	@Override
	public void read(ByteBuf buffer) throws Exception{
		ByteBuf buf= Unpooled.copiedBuffer(buffer);
		byte reveivedBytes[]=buf.array();
    	if(stunHandler.canHandle(reveivedBytes)){
    		byte stunResponse[]=stunHandler.handle(reveivedBytes,remoteAddress);
    		DatagramPacket dp=new DatagramPacket(Unpooled.wrappedBuffer(stunResponse), remoteAddress);
    		outboundChannel.writeAndFlush(dp);
    	}else{
    		if(dtlsHandler.isHandshakeComplete()){
    			//handle shake complete 
    			//decode data
    			logger.debug("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\n{}",HexDump.dumpHexString(reveivedBytes));
    		}else{
    			queue.put(reveivedBytes);	
        		if(startHandleShakeTime==0){
        			startHandleShakeTime=System.currentTimeMillis();
        			Jazmin.execute(dtlsHandler::handshake);
        		}		
    		}
    	}
		//super.read(buffer);
	}
	//
	//--------------------------------------------------------------------------
	/**
	 * @return
	 * @see jazmin.server.relay.webrtc.StunHandler#getUfrag()
	 */
	public String getIceUfrag() {
		return stunHandler.getUfrag();
	}
	/**
	 * @return
	 * @see jazmin.server.relay.webrtc.StunHandler#getPassword()
	 */
	public String getIcePassword() {
		return stunHandler.getPassword();
	}
	/**
	 * @return
	 * @see jazmin.server.relay.webrtc.DtlsHandler#getLocalFingerprint()
	 */
	public String getLocalFingerprint() {
		return dtlsHandler.getLocalFingerprint();
	}
	//--------------------------------------------------------------------------
	public void onDtlsHandshakeComplete() {
		logger.info("handle shake complete");
	};
	
	@Override
	public void onDtlsHandshakeFailed(Throwable e) {
		logger.catching(e);
	}
	//--------------------------------------------------------------------------
	//dtls handle shake

	public final static int MAX_DELAY = 4000;
	//
	private final int receiveLimit;
	private final int sendLimit;
	private ByteArrayBlockingQueue queue;
	//
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
		int readSize=Math.min(len,queue.count);
		byte bb[]=new byte[readSize];
		try {
			Thread.sleep(1000);
			queue.take(bb);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		System.arraycopy(bb, 0,buf,off,bb.length);
		return readSize;
	}

	@Override
	public void send(byte[] buf, int off, int len) throws IOException {
		if (len > getSendLimit()) {
			logger.warn("send len > send limit,",len+"/"+getSendLimit());
		}
		if (this.hasTimeout()) {
			throw new IllegalStateException("Handshake is taking too long! (>" + MAX_DELAY + "ms");
		}
		DatagramPacket dp=new DatagramPacket(Unpooled.wrappedBuffer(buf,off,len),remoteAddress);
		outboundChannel.writeAndFlush(dp);
	}

	@Override
	public void close() throws IOException {
		logger.debug("close dtls handle shake");
	}

	private boolean hasTimeout() {
		return (System.currentTimeMillis() - this.startHandleShakeTime) > MAX_DELAY;
	}

}
