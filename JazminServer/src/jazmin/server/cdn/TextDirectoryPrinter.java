/**
 * 
 */
package jazmin.server.cdn;

import java.io.File;
import java.util.regex.Pattern;

/**
 * @author yama
 *
 */
public class TextDirectoryPrinter implements DirectioryPrinter{
	private static final Pattern ALLOWED_FILE_NAME = Pattern
			.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
	//
	@Override
	public String print(File dir) {
		StringBuilder buf = new StringBuilder();
		for (File f : dir.listFiles()) {
			if (f.isHidden() || !f.canRead()) {
				continue;
			}
			String name = f.getName();
			if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
				continue;
			}
			buf.append(name).append("\r\n");
		}
		return buf.toString();
	}

	@Override
	public String contentType() {
		return "text/plain; charset=UTF-8";
	}

}
