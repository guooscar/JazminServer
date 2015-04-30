/**
 * 
 */
package jazmin.server.relay;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.HexDump;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author yama
 *
 */
public class HexDumpRelayChannel extends RelayChannel{
	private static Logger logger=LoggerFactory.get(HexDumpRelayChannel.class);
	//
	public HexDumpRelayChannel() {
		super();
	}
	//
	@Override
	void sendData(ByteBuf buffer) {
		packetSentCount++;
		byteSentCount+=buffer.capacity();
		if(logger.isDebugEnabled()){
			ByteBuf buf= Unpooled.copiedBuffer(buffer);
			logger.debug("send data:#{}\n{}",packetSentCount,HexDump.dumpHexString(buf.array()));
		}
	}
}
