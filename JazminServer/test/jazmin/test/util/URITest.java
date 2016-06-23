/**
 * 
 */
package jazmin.test.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import jazmin.util.DumpUtil;
import jazmin.util.FileUtil;

/**
 * @author g2131
 *
 */
public class URITest {

	/**
	 * @param args
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException {
		URI uri=new URI("jazmin://user@localhost:3001/MainData/app1");
		System.out.println(uri.getScheme());
		System.out.println(uri.getHost());
		System.out.println(uri.getPort());
		System.out.println(uri.getUserInfo());
		System.out.println(uri.getPath());
		//
		long ss=FileUtil.sizeOfPath(new File("/Users/yama/Desktop").toPath());
		System.out.println(DumpUtil.byteCountToString(ss));
	}

}
