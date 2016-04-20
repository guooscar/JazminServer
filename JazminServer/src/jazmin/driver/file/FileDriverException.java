package jazmin.driver.file;
/**
 * 
 * @author yama
 *
 */
@SuppressWarnings("serial")
public class FileDriverException extends Exception{
	public FileDriverException(Throwable e) {
		super(e);
	}
	public FileDriverException(String msg) {
		super(msg);
	}
}
