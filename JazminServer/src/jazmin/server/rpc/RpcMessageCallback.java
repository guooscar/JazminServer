/**
 * 
 */
package jazmin.server.rpc;

/**
 * @author yama
 * 25 Dec, 2014
 */
@FunctionalInterface
public interface RpcMessageCallback {
	void callback(RpcSession session,RpcMessage message);
}
