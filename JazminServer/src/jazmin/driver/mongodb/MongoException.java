package jazmin.driver.mongodb;

/**
 * 
 * @author skydu
 *
 */
public class MongoException extends RuntimeException{

	private static final long serialVersionUID = 1L;	
	//
	protected int code;
	public MongoException() {
		
	}
	//
	public MongoException(String msg) {
		super(msg);
	}
	//
	public MongoException(Throwable e) {
		super(e);
	}
	/**
	 */
	public MongoException(int code) {
		super();
		this.code=code;
	}
	//
	/**
	 */
	public MongoException(int code,String msg){
		super(msg);
		this.code=code;
	}
	//
	/**
	 */
	public MongoException(int code,String msg,Throwable e){
		super(msg, e);
		this.code=code;
	}
	//
	/**
	 */
	public MongoException(int code,Throwable e){
		super(e);
		this.code=code;
	}

	/**
	 * @return the code */
	public int getCode() {
		return code;
	}
	
}
