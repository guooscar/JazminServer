/**
 * 
 */
package jazmin.server.msg;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.codec.BinaryDecoder;
import jazmin.server.msg.codec.BinaryEncoder;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.server.msg.codec.ResponseMessage;
import jazmin.server.msg.kcp.KCP;
import jazmin.util.HexDumpUtil;

/**
 * @author yama
 *
 */
public class NettyKcpChannel extends KCP implements NetworkChannel{
	private static Logger logger=LoggerFactory.get(NettyKcpChannel.class);
	//
	Channel channel;
	InetSocketAddress localAddress;
	InetSocketAddress peerAddress;
	ByteBuf receiveBuffer;
	KcpSession session;
	NetworkTrafficStat networkTrafficStat;
	long createTime;
	long lastReceiveTime;
	long lastSentTime;
	long packetCount;
	//
	/*
	 * 
	 * int ikcp_nodelay(ikcpcb *kcp, int nodelay, int interval, int resend, int nc)
	 * nodelay ：是否启用 nodelay模式，0不启用；1启用。
	 * interval ：协议内部工作的 interval，单位毫秒，比如 10ms或者 20ms
	 * resend ：快速重传模式，默认0关闭，可以设置2（2次ACK跨越将会直接重传）
     * nc ：是否关闭流控，默认是0代表不关闭，1代表关闭。
	 * 普通模式：`ikcp_nodelay(kcp, 0, 40, 0, 0);
	 * 极速模式： ikcp_nodelay(kcp, 1, 10, 2, 1);
	 * 
	 * 
	 */
	//
	public NettyKcpChannel(
			int conv,
			Channel channel,
			InetSocketAddress localAddress,
			InetSocketAddress peerAddress) {
		super(conv);
		NoDelay(1, 10, 2, 1);
		this.channel=channel;
		this.localAddress=localAddress;
		this.peerAddress=peerAddress;
		receiveBuffer=Unpooled.buffer((int)(mtu+IKCP_OVERHEAD)*3);
		createTime=System.currentTimeMillis();
		lastReceiveTime=System.currentTimeMillis();
		lastSentTime=System.currentTimeMillis();
	}
	//
	@Override
	protected void output(byte[] buffer, int size) {
		System.err.println("send message----------------------------------------"+size+"\n"
					+HexDumpUtil.dumpHexString(buffer,0,size));
		//
		DatagramPacket dp=new DatagramPacket(Unpooled.copiedBuffer(buffer,0,size),peerAddress);
		channel.writeAndFlush(dp);
	}
	byte buffer[]=new byte[IKCP_MTU_DEF];
	//
	public RequestMessage receive(byte[]data){
		lastReceiveTime=System.currentTimeMillis();
		int ret=Input(data);
		if(ret<0){
			logger.warn("bad input data \n{}",HexDumpUtil.dumpHexString(data));
			return null;
		}
		int dataRead=0;
		while((dataRead=Recv(buffer))>0){
			receiveBuffer.writeBytes(buffer,0, dataRead);
		}
		//
		try {
			RequestMessage req=BinaryDecoder.decode0(receiveBuffer,networkTrafficStat);
			return req;
		} catch (Exception e) {
			logger.catching(e);
		}
		return null;
	}
	//
	@Override
	public InetSocketAddress getRemoteAddress() {
		return peerAddress;
	}
	//
	@Override
	public void close() {
		try {
			channel.close().sync();
		} catch (InterruptedException e) {}
	}
	//
	@Override
	public void writeAndFlush(Object obj) {
		lastSentTime=System.currentTimeMillis();
		ResponseMessage msg=(ResponseMessage) obj;
		ByteBuf out=Unpooled.buffer(128);
		try {
			BinaryEncoder.encode0(msg, out, networkTrafficStat);
			Send(out.array());
		} catch (Exception e) {
			logger.catching(e);
		}
	}
	//
	@Override
	public String toString() {
		return "KcpChannel-"+conv+"-"+peerAddress;
	}
}