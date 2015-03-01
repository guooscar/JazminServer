/**
 * 
 */
package jazmin.server.rpc;

/**
 * @author yama
 * 25 Dec, 2014
 */
@FunctionalInterface
public interface RPCMessageCallback {
	void callback(RPCSession session,RPCMessage message);
}
