
package jazmin.core.app;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class AppException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	//
	protected int code;
	public AppException() {
		
	}
	//
	public AppException(String msg) {
		super(msg);
	}
	//
	public AppException(Throwable e) {
		super(e);
	}
	/**
	 */
	public AppException(int code) {
		super();
		this.code=code;
	}
	//
	/**
	 */
	public AppException(int code,String msg){
		super(msg);
		this.code=code;
	}
	//
	/**
	 */
	public AppException(int code,String msg,Throwable e){
		super(msg, e);
		this.code=code;
	}
	//
	/**
	 */
	public AppException(int code,Throwable e){
		super(e);
		this.code=code;
	}

	/**
	 * @return the code */
	public int getCode() {
		return code;
	}
	
}
