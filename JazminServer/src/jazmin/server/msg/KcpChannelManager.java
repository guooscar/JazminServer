/**
 * 
 */
package jazmin.server.msg;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
import jazmin.util.RandomUtil;

/**
 * @author yama
 *
 */
public class KcpChannelManager implements Runnable{
	private static Logger logger=LoggerFactory.get(KcpChannelManager.class);
	//
	private AtomicInteger kcpChannelId;
	private Map<Integer,KcpChannel>kcpChannelMap;
	private Thread kcpUpdateThread;
	private MessageServer messageServer;
	private Channel udpChannel;
	public KcpChannelManager(MessageServer messageServer) {
		this.messageServer=messageServer;
		kcpChannelId=new AtomicInteger(RandomUtil.randomInt(1000000));
		kcpChannelMap=new ConcurrentHashMap<>();
		kcpUpdateThread=new Thread(this);
		kcpUpdateThread.start();
	}
	//-----------------------------------------------------------------------------
	public List<KcpChannel>getChannels(){
		return new ArrayList<>(kcpChannelMap.values());
	}
	//
	public int currentChannelId(){
		return kcpChannelId.get();
	}
	//-----------------------------------------------------------------------------
	//
	public static final short UDP_PKG_MAGIC=(short) 0xaabb;
	public static final short UDP_PKG_TYPE_PING=1;
	public static final short UDP_PKG_TYPE_PONG=2;
	//
	public static final int UDP_INFO_CHANNEL_NOT_EXIST=1;
	public static final int UDP_INFO_CHANNEL_SERVER_CLOSED=2;
	public static final int UDP_INFO_BAD_CONV=3;
	
	
	//
	private void sendPongPacket(
			InetSocketAddress peerAddress,
			int convId,
			int number,
			int info){
		ByteBuf bf=Unpooled.buffer(26);
		bf.writeShort(UDP_PKG_MAGIC);
		bf.writeShort(number);
		bf.writeShort(UDP_PKG_TYPE_PONG);
		bf.writeLong(System.currentTimeMillis());
		bf.writeInt(0);
		bf.writeInt(convId);
		bf.writeInt(info);
		DatagramPacket dp=new DatagramPacket(bf, peerAddress);
		udpChannel.writeAndFlush(dp);
	}
	//
	int nextChannelId(){
		int t=kcpChannelId.incrementAndGet();
		if(t<0){
			kcpChannelId.set(1);
		}
		return kcpChannelId.get();
	}
	//
	private boolean receivePingMessage(byte []data,Channel channel,DatagramPacket dp){
		ByteBuffer bf=ByteBuffer.wrap(data);
		short magic=bf.getShort();
		short number=bf.getShort();
		short type=bf.getShort();
		long timestamp=bf.getLong();
		int lag=bf.getInt();
		int convId=bf.getInt();
		int info=bf.getInt();
		//
		if(magic!=UDP_PKG_MAGIC){
			logger.warn("bad udp message:\n"+HexDumpUtil.dumpHexString(data));
			return false;
		}
		if(type==UDP_PKG_TYPE_PING){
			if(convId==0){
				//init packet,response next convId
				int newConvId=nextChannelId();
				KcpChannel newChannel=new KcpChannel(
						messageServer,
						newConvId,
						channel,
						dp.recipient(),
						dp.sender());
				newChannel.networkTrafficStat=messageServer.networkTrafficStat;
				kcpChannelMap.put(newConvId,newChannel);
				if(logger.isInfoEnabled()){
					logger.info("session create conv {} sender {}",newConvId,dp.sender());
				}
				//
				sendPongPacket(
						dp.sender(), 
						newConvId, 
						number,
						info);
				if(logger.isDebugEnabled()){
					logger.debug("allocate new conv {}",newConvId);
				}
			}else{
				//keep alive package
				KcpChannel kcpChannel=kcpChannelMap.get(convId);
				int rspInfo=info;
				if(kcpChannel==null){
					logger.warn("kcp channel not exists {}",convId);
					rspInfo=UDP_INFO_CHANNEL_NOT_EXIST;	
				}else{
					kcpChannel.peerAddress=dp.sender();
					kcpChannel.receivePing(timestamp,lag);
					if(kcpChannel.session!=null){
						if(logger.isDebugEnabled()){
							logger.debug("session keep alive {}",kcpChannel);
						}
						messageServer.sessionKeepAlive(kcpChannel.session);
					}
					kcpChannel.sentPacketCount++;
				}
				sendPongPacket(
						dp.sender(), 
						convId,
						number,
						rspInfo);
			}
			return true;
		}
		logger.warn("bad udp message type:\n"+HexDumpUtil.dumpHexString(data));
		return true;
	}
	//
	public void receiveDatagramPacket(Channel channel,DatagramPacket dp){
		udpChannel=channel;
		ByteBuf buf=dp.copy().content();
		byte data[]=new byte[buf.readableBytes()];
		buf.readBytes(data);
		buf.release();
		//ping message
		/*
		*ping message format
		*2 magic number=0xaabb
		*2 serial number
		*2 type 
		*8 timestamp
		*4 lag
		*4 convId
		*4 info
		*/
		//
		//
		if(data.length==26){
			if(receivePingMessage(data,channel, dp)){
				return;
			}
		}
		//
		int conv=KCP.getConversionId(data);
		if(conv<=0){
			logger.warn("bad conversion id {}",conv);
			sendPongPacket(
					dp.sender(), 
					conv,
					0,
					UDP_INFO_BAD_CONV);
			return;
		}
		//
		KcpChannel kcpChannel=kcpChannelMap.get(conv);
		if(kcpChannel==null){
			logger.warn("bad conv id: {} . maybe server restart",conv);
			sendPongPacket(
					dp.sender(), 
					conv,
					0,
					UDP_INFO_CHANNEL_SERVER_CLOSED);
			return;
		}
		if(kcpChannel.session==null){
			KcpSession session=new KcpSession(kcpChannel);
			kcpChannel.session=session;
			messageServer.sessionCreated(session);
		}
		//
		synchronized (kcpChannel) {
			kcpChannel.peerAddress=dp.sender();
			RequestMessage req=kcpChannel.receive(data);
			if(req!=null){
				messageServer.receiveMessage(kcpChannel.session, req);
			}
		}
	}
	//
	private void updateKcp0(List<Integer>removedChannel){
		long now=System.currentTimeMillis();
		//
		removedChannel.clear();
		kcpChannelMap.forEach((id,channel)->{
			if(channel.channel==null){
				sendPongPacket(
						channel.peerAddress,
						channel.getConvId(),
						0,
						KcpChannelManager.UDP_INFO_CHANNEL_SERVER_CLOSED);
				removedChannel.add(id);
				if(channel.session!=null){
					messageServer.sessionDisconnected(channel.session);
				}
				return;
			}
			//
			channel.updateChannel();
			//10 mins no data transfer
			if((now-channel.lastReceiveTime)>60*10*1000L){
				if(logger.isWarnEnabled()){
					logger.warn("channel:"+channel+" idle");
				}
				if(channel.session!=null){
					messageServer.sessionIdle(channel.session);
				}
				return;
			}
			//
			//2 mins no ping receive
			if((now-channel.lastPingTime)>60*2*1000L){
				if(logger.isWarnEnabled()){
					logger.warn("channel:"+channel+" 2 mins no ping receive");
				}
				channel.close();
				return;
			}
		});
		for(int id:removedChannel){
			kcpChannelMap.remove(id);
		}
	}
	//
	@Override
	public void run() {
		List<Integer>removedChannel=new LinkedList<>();
		while(true){
			try {
				updateKcp0(removedChannel);
				Thread.sleep(10);
			} catch (Exception e) {}
		}
	}
}
