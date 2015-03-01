/**
 * 
 */
package jazmin.misc;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class InfoBuilder {
	private StringWriter sw;
	private PrintWriter pw;
	private String format;
	public InfoBuilder() {
		sw=new StringWriter();
		pw=new PrintWriter(sw);
	}
	public static InfoBuilder create(){
		return new InfoBuilder();
	}
	//
	public InfoBuilder format(String format){
		this.format=format;
		return this;
	}
	//
	public InfoBuilder print(Object ...args){
		pw.printf(format, args);
		return this;
	}
	//
	public InfoBuilder section(String title){
		pw.append(title);
		for(int i=0;i<60-title.length();i++){
			pw.append("-");
		}
		pw.append("\n");
		return this;
	}
	//
	public InfoBuilder println(Object s){
		pw.println(s);
		return this;
	}
	//
	public String toString(){
		return sw.toString();
	}
}
