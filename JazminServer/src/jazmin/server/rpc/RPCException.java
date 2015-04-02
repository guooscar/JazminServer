/**
 * 
 */
package jazmin.server.rpc;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class RPCException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RPCException() {
		super();
	}
	public RPCException(String msg) {
		super(msg);
	}
	//
	public RPCException(String msg,Throwable e){
		super(msg,e);
	}
	//
	public RPCException(Throwable e){
		super(e);
	}
}
