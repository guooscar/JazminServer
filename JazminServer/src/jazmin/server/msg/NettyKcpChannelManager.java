/**
 * 
 */
package jazmin.server.msg;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.server.msg.kcp.KCP;
import jazmin.util.HexDumpUtil;

/**
 * @author yama
 *
 */
public class NettyKcpChannelManager implements Runnable{
	private static Logger logger=LoggerFactory.get(NettyKcpChannelManager.class);
	//
	private AtomicInteger kcpChannelId;
	private Map<Integer,NettyKcpChannel>kcpChannelMap;
	private Thread kcpUpdateThread;
	private MessageServer messageServer;
	public NettyKcpChannelManager(MessageServer messageServer) {
		this.messageServer=messageServer;
		kcpChannelId=new AtomicInteger(0);
		kcpChannelMap=new ConcurrentHashMap<>();
		kcpUpdateThread=new Thread(this);
		kcpUpdateThread.start();
	}
	//
	public static final int  UDP_PKG_MAGIC=0xaabbccdd;
	public static final short UDP_PKG_TYPE_PING=1;
	public static final short UDP_PKG_TYPE_PONG=2;
	//
	public static final int UDP_INFO_CHANNEL_NOT_EXIST=1;
	
	//
	private static void sendPongPacket(
			InetSocketAddress peerAddress,
			Channel channel,
			int convId,
			int info){
		ByteBuf bf=Unpooled.buffer(22);
		bf.writeInt(UDP_PKG_MAGIC);
		bf.writeShort(UDP_PKG_TYPE_PONG);
		bf.writeLong(System.currentTimeMillis());
		bf.writeInt(convId);
		bf.writeInt(info);
		DatagramPacket dp=new DatagramPacket(bf, peerAddress);
		channel.writeAndFlush(dp);
		if(logger.isDebugEnabled()){
			logger.debug("send pong message {} {} {} {}",channel,peerAddress,convId,info);
		}
	}
	
	//
	public void receiveDatagramPacket(Channel channel,DatagramPacket dp){
		ByteBuf buf=dp.copy().content();
		byte data[]=new byte[buf.readableBytes()];
		buf.readBytes(data);
		//ping message
		/*
		*ping message format
		*4 magic number=0xaabbccdd
		*2 type 
		*8 timestamp
		*8 convId
		*4 info
		*/
		//
		System.err.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx:"+data.length+"\n"
				+HexDumpUtil.dumpHexString(data));
		//
		if(data.length==22){
			ByteBuffer bf=ByteBuffer.wrap(data);
			long magic=bf.getInt();
			short type=bf.getShort();
			long timestamp=bf.getLong();
			int convId=bf.getInt();
			int info=bf.getInt();
			//
			if(magic!=UDP_PKG_MAGIC){
				logger.warn("bad udp message:\n"+HexDumpUtil.dumpHexString(data));
				return;
			}
			if(type==UDP_PKG_TYPE_PING){
				if(logger.isDebugEnabled()){
					logger.debug("reveice ping packet convId {} {}",convId,new Date(timestamp));
				}
				if(convId==0){
					//init packet,response next convId
					sendPongPacket(
							dp.sender(), 
							channel,
							kcpChannelId.incrementAndGet(), info);
				}else{
					//keep alive package
					NettyKcpChannel kcpChannel=kcpChannelMap.get(convId);
					int rspInfo=info;
					if(kcpChannel==null){
						logger.warn("kcp channel not exists {}",convId);
						rspInfo=UDP_INFO_CHANNEL_NOT_EXIST;	
					}else{
						if(kcpChannel.session!=null){
							if(logger.isDebugEnabled()){
								logger.debug("session keep alive {}",kcpChannel);
							}
							messageServer.sessionKeepAlive(kcpChannel.session);
						}
					}
					sendPongPacket(
							dp.sender(), 
							channel,
							convId,
							rspInfo);
				}
				return;
			}else{
				logger.warn("bad udp message type:\n"+HexDumpUtil.dumpHexString(data));
			}
		}
		//
		int conv=KCP.getConversionId(data);
		if(conv<0){
			logger.warn("bad conversion id : {}"+conv);
			return;
		}
		//
		NettyKcpChannel kcpChannel=kcpChannelMap.get(conv);
		if(kcpChannel==null){
			kcpChannel=new NettyKcpChannel(
					conv,channel,dp.recipient(),dp.sender());
			kcpChannel.networkTrafficStat=messageServer.networkTrafficStat;
			kcpChannelMap.put(conv,kcpChannel);
			if(logger.isInfoEnabled()){
				logger.info("session create conv {} sender {}",conv,dp.sender());
			}
			KcpSession session=new KcpSession(kcpChannel);
			kcpChannel.session=session;
			messageServer.sessionCreated(session);
		}
		//
		RequestMessage req=kcpChannel.receive(data);
		if(req!=null){
			messageServer.receiveMessage(kcpChannel.session, req);
		}
	}
	//
	@Override
	public void run() {
		List<Integer>removedChannel=new LinkedList<>();
		while(true){
			//
			long now=System.currentTimeMillis();
			//
			removedChannel.clear();
			kcpChannelMap.forEach((id,channel)->{
				channel.Update(System.currentTimeMillis());
				//
				if((now-channel.lastReceiveTime)>120*1000L){
					if(logger.isWarnEnabled()){
						logger.warn("channel:"+channel+" idle");
					}
					messageServer.sessionIdle(channel.session);
					removedChannel.add(id);
				}
			});
			for(int id:removedChannel){
				kcpChannelMap.remove(id);
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}
	}
}
