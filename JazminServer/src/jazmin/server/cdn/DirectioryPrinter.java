/**
 * 
 */
package jazmin.server.cdn;

import java.io.File;

/**
 * @author yama
 *
 */
public interface DirectioryPrinter {
	String print(File dir);
	String contentType();
}
