/**
 * 
 */
package jazmin.server.console.ascii;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import jazmin.util.DumpUtil;

/**
 * @author yama
 *
 */
public class BasePrinter {
	protected PrintWriter out;
	protected String format;
	
	public BasePrinter(PrintWriter out) {
		this.out=out;
	}
	//
	protected String getValue(Object o,int width){
		String result="";
		if(o!=null){
			if(o instanceof Date){
				result=formatDate((Date) o);
			}else{
				result=o.toString();
			}
		}
		return DumpUtil.cut(result,width);
	}
	//
	protected String formatDate(Date date){
    	SimpleDateFormat sdf=new SimpleDateFormat("MM/dd HH:mm:ss");
    	return  sdf.format(date);
    }
}
