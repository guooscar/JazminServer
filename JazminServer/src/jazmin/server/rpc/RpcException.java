/**
 * 
 */
package jazmin.server.rpc;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class RpcException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RpcException() {
		super();
	}
	public RpcException(String msg) {
		super(msg);
	}
	//
	public RpcException(String msg,Throwable e){
		super(msg,e);
	}
	//
	public RpcException(Throwable e){
		super(e);
	}
}
