/**
 * 
 */
package jazmin.server.rpc;

import java.io.Serializable;

import jazmin.util.DumpUtil;

/**
 * @author yama
 * 23 Dec, 2014
 */
public class RPCMessage implements Serializable{
	//
	public static class AppExceptionMessage {
		public int code;
		public String message;
	}
	//
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//
	public static final int TYPE_RPC_CALL_REQ=1;
	public static final int TYPE_RPC_CALL_RSP=2;
	public static final int TYPE_SESSION_AUTH=3;
	public static final int TYPE_PUSH=4;
	
	//
	public int id;
	public int type;
	public Object []payloads;
	//
	@Override
	public String toString() {
		return DumpUtil.dump(this);
	}
}
