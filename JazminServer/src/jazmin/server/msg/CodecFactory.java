/**
 * 
 */
package jazmin.server.msg;

import io.netty.buffer.ByteBuf;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.server.msg.codec.ResponseMessage;

/**
 * @author yama
 *
 */
public interface CodecFactory {
	//
	void encode(
			ResponseMessage msg,
			ByteBuf out,
			NetworkTrafficStat networkTrafficStat)throws Exception;
	//
	RequestMessage decode(ByteBuf in,
			NetworkTrafficStat networkTrafficStat)throws Exception;
}
