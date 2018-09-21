
package jazmin.server.web.mvc;
/**
 * 
 * @author skydu
 *
 */
public class ParameterException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	//
	protected int code;
	public ParameterException() {
		
	}
	//
	public ParameterException(String msg) {
		super(msg);
	}
	//
	public ParameterException(Throwable e) {
		super(e);
	}
	/**
	 */
	public ParameterException(int code) {
		super();
		this.code=code;
	}
	//
	/**
	 */
	public ParameterException(int code,String msg){
		super(msg);
		this.code=code;
	}
	//
	/**
	 */
	public ParameterException(int code,String msg,Throwable e){
		super(msg, e);
		this.code=code;
	}
	//
	/**
	 */
	public ParameterException(int code,Throwable e){
		super(e);
		this.code=code;
	}

	/**
	 * @return the code */
	public int getCode() {
		return code;
	}
	
}
