/**
 * 
 */
package jazmin.server.rpc.codec;

import jazmin.server.rpc.RpcException;
import jazmin.server.rpc.RpcMessage;

/**
 * @author yama
 * 6 Jan, 2016
 */
public class CodecUtil {
	public static RpcMessage createExceptionMessage(int messageId,String message){
		RpcMessage rspMessage=new RpcMessage();
		rspMessage.id=messageId;
		rspMessage.type=RpcMessage.TYPE_RPC_CALL_RSP;
		RpcException rpcException=new RpcException(message);
		rspMessage.payloads=new Object[]{null, rpcException};	
		return rspMessage;
	}
}
