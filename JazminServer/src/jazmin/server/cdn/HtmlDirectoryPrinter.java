/**
 * 
 */
package jazmin.server.cdn;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import jazmin.util.DumpUtil;

/**
 * @author yama
 *
 */
public class HtmlDirectoryPrinter implements DirectioryPrinter{
	private static final Pattern ALLOWED_FILE_NAME = Pattern
			.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
	@Override
	public String print(File dir) {
		 String dirPath = dir.getPath();
	        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        StringBuilder buf = new StringBuilder()
	            .append("<!DOCTYPE html>\r\n")
	            .append("<html><head><title>")
	            .append("Index of: ")
	            .append(dirPath)
	            .append("</title></head><body>\r\n")
	            .append("<h3>Index of: ")
	            .append(dirPath)
	            .append("</h3>\r\n<hr/>")
	            .append("<table>")
	            .append("<tr><td width='450px'>Name</td><td width='200px'>Last modified</td><td width='200px'>Size</td></tr>\r\n")
	            .append("<tr><td><a href=\"../\">..</a></td><td></td><td></td></tr>\r\n");

	        for (File f: dir.listFiles()) {
	            if (f.isHidden() || !f.canRead()) {
	                continue;
	            }
	            String name = f.getName();
	            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
	                continue;
	            }
	            buf.append("<tr><td><a href=\"")
	               .append(name)
	               .append("\">")
	               .append(name)
	               .append("</a></td>")
	               .append("<td>")
	               .append(sdf.format(new Date(f.lastModified())))
	               .append("</td>")
	               .append("<td>")
	               .append(f.isDirectory()?"-":DumpUtil.byteCountToString(f.length()))
	               .append("</td>")
	               .append("</tr>\r\n");
	        }
	        buf.append("</table></body></html>\r\n");
		return buf.toString();
	}
	//
	@Override
	public String contentType() {
		return "text/html; charset=UTF-8";
	}
}
