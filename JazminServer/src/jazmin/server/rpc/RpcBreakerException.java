/**
 * 
 */
package jazmin.server.rpc;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class RpcBreakerException extends RpcException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RpcBreakerException() {
		super();
	}
	public RpcBreakerException(String msg) {
		super(msg);
	}
}
