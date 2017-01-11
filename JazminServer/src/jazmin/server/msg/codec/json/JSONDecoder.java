/**
 * 
 */
package jazmin.server.msg.codec.json;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.NetworkTrafficStat;
import jazmin.server.msg.codec.BinaryDecoder;
import jazmin.server.msg.codec.JSONRequestParser;
import jazmin.server.msg.codec.RequestMessage;
import jazmin.util.DumpUtil;

/**
 * @author yama
 * 31 Mar, 2015
 */
public class JSONDecoder extends BinaryDecoder{
	private static Logger logger= LoggerFactory.get(JSONDecoder.class);
	//
	public JSONDecoder(NetworkTrafficStat networkTrafficStat) {
		super(networkTrafficStat);
	}
	//
	@Override
	protected RequestMessage decode(byte[] payload) throws Exception {
		String s = new String(payload, "UTF-8");
		RequestMessage reqMessage=JSONRequestParser.createSimpleRequestMessage(s);
		if (logger.isDebugEnabled()) {
			logger.debug(
					"\ndecode message--------------------------------------\n{} "
					,DumpUtil.formatJSON(s));
		}
		return reqMessage;
	}
}
