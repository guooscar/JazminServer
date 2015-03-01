/**
 * 
 */
package jazmin.test.server.console;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yama
 * 13 Jan, 2015
 */
public class TestPipe {

	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception{
		Pattern p=Pattern.compile("123");
		String line="123aaaacadcdc123";
		Matcher matcher=p.matcher(line);
			StringBuilder sb=new StringBuilder(line);
		int offset=0;
		while(matcher.find()){
			sb.insert(matcher.start()+offset,"$");
			offset+=1;
			sb.insert(matcher.end()+offset,"#");
			offset+=1;
		}
		System.out.println(matcher.matches());
	}
}
