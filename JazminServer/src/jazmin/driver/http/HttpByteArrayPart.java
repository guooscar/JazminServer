/**
 * 
 */
package jazmin.driver.http;

import java.nio.charset.Charset;

import com.ning.http.client.multipart.ByteArrayPart;

/**
 * @author yama
 *
 */
public class HttpByteArrayPart {
	ByteArrayPart part;
	public HttpByteArrayPart(String name,byte[] file){
		part=new ByteArrayPart(name, file);
	}
	public HttpByteArrayPart(String name,byte[] file,String contentType){
		part=new ByteArrayPart(name,file,contentType);
	}
	public HttpByteArrayPart(String name,byte[] file,String contentType,Charset charset){
		part=new ByteArrayPart(name, file,contentType,charset);
	}
	public HttpByteArrayPart(String name,byte[] file,String contentType,Charset charset,String fileName){
		part=new ByteArrayPart(name, file,contentType,charset,fileName);
	}
	public HttpByteArrayPart(String name,byte[] file,String contentType,Charset charset,String fileName,String contentId){
		part=new ByteArrayPart(name, file,contentType,charset,fileName,contentId);
	}
}
