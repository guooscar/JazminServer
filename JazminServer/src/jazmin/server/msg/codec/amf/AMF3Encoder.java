/**
 * 
 */
package jazmin.server.msg.codec.amf;

import flex.messaging.io.amf.ASObject;
import io.netty.channel.ChannelHandler.Sharable;

import java.io.ByteArrayOutputStream;

import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.codec.BinaryEncoder;
import jazmin.server.msg.codec.ResponseMessage;

/**
 * @author yama
 * 31 Mar, 2015
 */
@Sharable
public class AMF3Encoder extends BinaryEncoder{
	//
	public AMF3Encoder(NetworkTrafficStat networkTrafficStat) {
		super(networkTrafficStat);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected byte[] encode(ResponseMessage msg) throws Exception{
		//amf3 format
		ASObject obj=new ASObject();
		obj.put("msg", msg.responseMessages);
		//
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		AMF3Serializer ser = new AMF3Serializer(stream);
		ser.writeObject(obj);
		ser.flush();
		ser.close();
		byte encodedByte[] = stream.toByteArray();
		stream.close();
		return encodedByte;
	}
}
