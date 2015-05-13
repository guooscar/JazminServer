/**
 * 
 */
package jazmin.server.console.ascii;

import java.io.PrintWriter;

/**
 * @author yama
 *
 */
public class FormPrinter extends BasePrinter {
	private int keyWidth;
	public FormPrinter(PrintWriter out) {
		super(out);
	}
	//
	public static FormPrinter create(PrintWriter out,int keyWidth){
		FormPrinter fp=new FormPrinter(out);
		fp.format=TerminalWriter.BOLD+"%-"+keyWidth+"s "+TerminalWriter.RESET+": %-50s\n";
		fp.keyWidth=keyWidth;
		return fp;
	}
	//
	public FormPrinter print(String key,Object value){
		out.format(format,getValue(key,keyWidth),getValue(value,-1));	
		return this;
	}
}
