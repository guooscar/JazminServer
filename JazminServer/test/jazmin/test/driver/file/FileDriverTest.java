package jazmin.test.driver.file;

import java.io.File;

import jazmin.driver.file.FileServerDriver;
import jazmin.driver.file.FileDriverException;

/**
 * 
 * @author yama
 *
 */
public class FileDriverTest {
	//
	public static void main(String[] args)throws Exception{
		FileServerDriver fsd=new FileServerDriver();
		fsd.setHomeDir("/Users/yama/Desktop/file-driver-test");
		//
		fsd.addServer("KdjyFileSystem","localhost",9561,10);
		fsd.start();
		//
		testDownload(fsd);
	}
	public static void testDownload(FileServerDriver fsd)throws Exception{
		fsd.downloadFile("KdjyFileSystem_56a38c77-af73-4ff4-b639-05bc4ce56ce6.jpg");
	}
	//
	public static void testSingleUpload(FileServerDriver fsd)throws Exception{
		String id = fsd.upload(new File("/Users/yama/Desktop/test.zip"));
		System.out.println(id);
	}
	//
	public static void testMultiThreadUpload(FileServerDriver fsd){
		for(int i=0;i<100;i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					String id;
					try {
						id = fsd.upload(new File("/Users/yama/Desktop/test.zip"));
						System.out.println(id);
					} catch (FileDriverException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
}
