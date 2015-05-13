/**
 * 
 */
package jazmin.server.console.ascii;

import java.io.PrintWriter;

/**
 * @author yama
 *
 */
public class TablePrinter extends BasePrinter{
	private int []headerLengths;
	private int idx;
	public TablePrinter(PrintWriter out) {
		super(out);
		idx=1;
	}
	//
	public static TablePrinter create(PrintWriter out){
		TablePrinter fp=new TablePrinter(out);
		return fp;
	}
	//
	public TablePrinter headers(String ...headers){
		Object oo[]=new Object[headerLengths.length+1];
		System.arraycopy(headers, 0, oo,1, headerLengths.length);
		oo[0]="#";
		
		out.print(TerminalWriter.BOLD);
		out.printf(format, oo);
		out.print(TerminalWriter.RESET);
		return this;
	}
	//
	public TablePrinter length(int ...headerLengths){
		this.headerLengths=headerLengths;
		StringBuilder f=new StringBuilder();
		f.append("%-5s : ");
		for(int c:headerLengths){
			f.append("%-").append(c).append("s ");
		}
		f.append("\n");
		this.format=f.toString();
		return this;
	}
	//
	public TablePrinter print(Object ...values){
		Object []result=new Object[headerLengths.length+1];
		result[0]=idx++;
		for(int i=0;i<values.length;i++){
			if(i!=values.length-1){
				result[i+1]=getValue(values[i],headerLengths[i]);		
			}else{
				result[i+1]=values[i];
			}
		}
		out.printf(format,result);
		return this;
	}
}
