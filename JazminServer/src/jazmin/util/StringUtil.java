package jazmin.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author <a href="mailto:guooscar@gmail.com">yAma</a>
 */
public class StringUtil {
	
	/**
	 * format string 
	 */
	public static String format(String format,Object ...args){
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw);
		pw.format(format, args);
		pw.flush();
		sw.flush();
		return sw.toString();
	}
}
