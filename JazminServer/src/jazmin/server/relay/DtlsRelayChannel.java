/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import jazmin.codec.rtcp.RtcpPacket;
import jazmin.codec.rtp.RtpPacket;
import jazmin.core.Jazmin;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.server.relay.webrtc.ByteArrayBlockingQueue;
import jazmin.server.relay.webrtc.DtlsHandler;
import jazmin.server.relay.webrtc.StunHandler;

import org.bouncycastle.crypto.tls.DatagramTransport;

/**
 * @author yama
 *
 */
public class DtlsRelayChannel extends NetworkRelayChannel 
implements DatagramTransport{
	private static Logger logger=LoggerFactory.get(DtlsRelayChannel.class);
	//
	private DtlsHandler dtlsHandler;
	private static StunHandler stunHandler=new StunHandler();
	private long startHandleShakeTime=0;
	//
	private TreeSet<String>rtpRelayChannels;
	private TreeSet<String>rtcpRelayChannels;
	
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
		dtlsHandler=new DtlsHandler(this);
        dtlsHandler.setRemoteFingerprint("sha-256", "");
        //
        rtpRelayChannels=new TreeSet<String>();
        rtcpRelayChannels=new TreeSet<String>(); 
	}
	//
	public void writeToPeer(byte [] buffer,int off,int len) {
		lastAccessTime=System.currentTimeMillis();
		ByteBuf buf= Unpooled.copiedBuffer(buffer,off,len);
		DatagramPacket dp=new DatagramPacket(
				buf,
				remoteAddress);
		outboundChannel.writeAndFlush(dp);
	}
	/**
	 * relay rtp packet to channel
	 * @param rc
	 */
	public void relayRtpTo(RelayChannel rc){
		rtpRelayChannels.add(rc.id);
	}
	//
	public void relayRtpAndRtcpTo(RelayChannel rc){
		relayRtcpTo(rc);
		relayRtpTo(rc);
	}
	//
	/**
	 * relay rtcp packet to channel
	 * @param rc
	 */
	public void relayRtcpTo(RelayChannel rc){
		rtcpRelayChannels.add(rc.id);
	}
	//
	private void writeToPeer(byte [] buffer) {
		writeToPeer(buffer,0,buffer.length);
	}
	//
	@Override
	public void dataFromRelay(RelayChannel channel,byte [] buffer) throws Exception {
		super.dataFromRelay(channel, buffer);
		if(!dtlsHandler.isHandshakeComplete()){
			return;
		}
		//encryption incoming data and send to peer
		if(RtpPacket.canHandle(buffer)){
			byte encoded[]=dtlsHandler.encodeRTP(buffer, 0, buffer.length);	
			writeToPeer(encoded);
			return;
		}
		if(RtcpPacket.canHandle(buffer)){
			byte encoded[]=dtlsHandler.encodeRTCP(buffer, 0, buffer.length);	
			writeToPeer(encoded);
			return;
		}
	}
	//
	public void dataFromPeer(byte []buffer) throws Exception{
		bytePeerCount+=buffer.length;
		packetPeerCount++;
		lastAccessTime=System.currentTimeMillis();
    	if(stunHandler.canHandle(buffer)){
    		if(logger.isDebugEnabled()){
    			logger.debug("handle stun message");
    		}
    		byte stunResponse[]=stunHandler.handle(buffer,remoteAddress);
    		writeToPeer(stunResponse);
    	}else{
    		if(dtlsHandler.isHandshakeComplete()){
    			//handshake complete 
    			//decode data
    			processRtpPackage(buffer);
    		}else{
    			queue.put(buffer);
    			if(startHandleShakeTime==0){
        			startHandleShakeTime=System.currentTimeMillis();
        			if(logger.isDebugEnabled()){
        				logger.debug("start handshake "+getInfo());
        			}
        			Jazmin.execute(dtlsHandler::handshake);
        		}		
    		}
    	}
	}
	//
	private void processRtpPackage(byte[]buf) throws Exception{
		//rtp
		byte decodedRtp[]=dtlsHandler.decodeRTP(buf, 0, buf.length);
		if(decodedRtp!=null){
			relayToNextStream(rtpRelayChannels,decodedRtp);
			return;
		}
		//rtcp
		byte decodedRtcp[]=dtlsHandler.decodeRTCP(buf, 0, buf.length);
		if(decodedRtcp!=null){
			relayToNextStream(rtcpRelayChannels,decodedRtcp);
			return;
		}
	}
	//
	private void relayToNextStream(Set<String>channels,byte []bytes){
		synchronized (linkedChannels) {
			for(RelayChannel rc:linkedChannels){
				rc.lastAccessTime=System.currentTimeMillis();
				try {
					if(channels.contains(rc.id)){
						rc.dataFromRelay(this,bytes);
					}
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}
	}
	//
	//
	@Override
	public String getInfo() {
		InfoBuilder ib=new InfoBuilder();
		ib.println(super.getInfo());
		ib.format("%-30s:%-30s\n");
		if(dtlsHandler.isHandshakeComplete()){
			ib.print("HandshakeStatus","HandshakeComplete");	
		}else if(dtlsHandler.isHandshaking()){
			ib.print("HandshakeStatus","Handshaking");	
		}else if(dtlsHandler.isHandshakeFailed()){
			ib.print("HandshakeStatus","HandshakeFailed");	
		}else{
			ib.print("HandshakeStatus","N/A");		
		}
		ib.print("LocalFingerprint",dtlsHandler.getHashFunction()+" "+dtlsHandler.getLocalFingerprint());
		ib.print("RemoteFingerprint",dtlsHandler.getRemoteFingerprint());
		ib.print("IceUfrag/Password",getIceUfrag()+"  "+getIcePassword());
		ib.print("Relays:","rtp->"+rtpRelayChannels+" rtcp->"+rtcpRelayChannels);
		return ib.toString();
	}
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
		logger.info(toString()+"/ handshake complete");
		queue.close();
	}
	//
	public void onDtlsHandshakeFailed(Throwable e) {
		logger.catching(e);
		queue.close();
		try {
			closeChannel();
		} catch (Exception e1) {
			logger.catching(e1);
		}
	}
	//--------------------------------------------------------------------------
	//dtls handle shake

	public final static int MAX_DELAY = 15000;
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
		writeToPeer(buf,off,len);
	}

	@Override
	public void close(){
		logger.debug("close dtls handshake");
		queue.close();
	}

	private boolean hasTimeout() {
		return (System.currentTimeMillis() - this.startHandleShakeTime) > MAX_DELAY;
	}
}
