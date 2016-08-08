/**
 * 
 */
package jazmin.test.server.file;

import java.io.File;

import jazmin.server.file.FileClient;

/**
 * @author yama
 *
 */
public class FileClientTest {
	//
	public static void main(String[] args)throws Exception {
		FileClient fc=new FileClient();
			String s=fc.upload("http://localhost:8080/upload/",new File("/Users/yama/Desktop/aaa.jpg"));
			System.err.println(s);
		
		//fc.download("http://skydu.local:6008/download/f35e3737-b518-4eb4-a1ed-53bc47119eaa.jpg",
		//		new File("/Users/yama/Desktop/test.txt"));
		
	}
}
