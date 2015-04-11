/**
 * 
 */
package jazmin.test.util;

import java.net.URI;
import java.net.URISyntaxException;

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
	}

}
