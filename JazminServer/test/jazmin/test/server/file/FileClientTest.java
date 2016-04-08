/**
 * 
 */
package jazmin.test.server.file;

import java.io.File;
import java.io.InputStream;

import jazmin.server.file.FileClient;
import jazmin.server.file.FileDownloadHandler;

/**
 * @author yama
 *
 */
public class FileClientTest {
	//
	public static void main(String[] args)throws Exception {
		FileClient fc=new FileClient();
		//String s=fc.upload("http://localhost:8080/upload/",new File("/Users/yama/Desktop/sync_jazmin.sh"));
		//System.err.println(s);
		//
		FileDownloadHandler handler=new FileDownloadHandler() {
			@Override
			public void handleNotFound() {
				System.err.println("not found");
			}
			
			@Override
			public void handleInputStream(InputStream inputStream, long fileLength) {
				System.err.println("handleInputStream found"+fileLength);
			}
			
			@Override
			public void handleException(Throwable e) {
				e.printStackTrace();
				
			}
		};
		fc.download("http://localhost:8080/download/e99a8f45-ce16-4f57-8df9-39c6a038ec67.sh",
				new File("/Users/yama/Desktop/test.txt"), handler);
	}
}
