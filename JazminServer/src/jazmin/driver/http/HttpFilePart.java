/**
 * 
 */
package jazmin.driver.http;

import java.io.File;
import java.nio.charset.Charset;

import com.ning.http.client.multipart.FilePart;

/**
 * @author yama
 *
 */
public class HttpFilePart {
	FilePart part;
	public HttpFilePart(String name,File file){
		part=new FilePart(name, file);
	}
	public HttpFilePart(String name,File file,String contentType){
		part=new FilePart(name,file,contentType);
	}
	public HttpFilePart(String name,File file,String contentType,Charset charset){
		part=new FilePart(name, file,contentType,charset);
	}
	public HttpFilePart(String name,File file,String contentType,Charset charset,String fileName){
		part=new FilePart(name, file,contentType,charset,fileName);
	}
	public HttpFilePart(String name,File file,String contentType,Charset charset,String fileName,String contentId){
		part=new FilePart(name, file,contentType,charset,fileName,contentId);
	}
}
