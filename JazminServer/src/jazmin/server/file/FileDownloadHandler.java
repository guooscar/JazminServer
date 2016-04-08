/**
 * 
 */
package jazmin.server.file;

import java.io.InputStream;

/**
 * @author yama
 *
 */
public interface FileDownloadHandler {
	void handleInputStream(InputStream inputStream,long fileLength);
	void handleNotFound();
	void handleException(Throwable e);
}
