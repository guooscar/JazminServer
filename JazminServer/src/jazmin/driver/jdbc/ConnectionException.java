/**
 * 
 */
package jazmin.driver.jdbc;

/**
 * @author yama
 * 27 Dec, 2014
 */
public class ConnectionException extends RuntimeException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ConnectionException(){
		super();
	}
	public ConnectionException(Throwable e){
		super(e);
	}
	//
	public ConnectionException(String s){
		super(s);
	}
}
