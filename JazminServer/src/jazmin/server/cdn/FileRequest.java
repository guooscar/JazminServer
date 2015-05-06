/**
 * 
 */
package jazmin.server.cdn;

import io.netty.channel.Channel;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author yama
 *
 */
public class FileRequest {
	//
	String id;
	File file;
	String uri;
	Channel channel;
	Date createTime;
	long totalBytes;
	long transferedBytes;
	InetSocketAddress remoteAddress;
	RandomAccessFile randomAccessFile;
	InputStream inputStream;
	CdnServer cdnServer;
	//
	public FileRequest(CdnServer cdnServer,String uri,Channel channel) {
		this.cdnServer=cdnServer;
		this.uri=uri;
		this.channel=channel;
		remoteAddress=(InetSocketAddress)channel.remoteAddress();
		createTime=new Date();
	}
	//
	public boolean open()throws Exception{
		final String path = sanitizeUri(uri);
		if (path == null) {
			return false;
		}
		file = new File(path);
		if (file.isHidden()) {
			return false;
		}
		// system device file
		if (!file.isDirectory() && !file.isFile()) {
			return false;
		}
		//
		if(file.exists()){
			randomAccessFile=new RandomAccessFile(file,"r");
			return true;
		}else{
			//check remote 
			return true;
		}
	}
	//
	void close()throws Exception{
		cdnServer.removeFileRequest(this.id);
		if(randomAccessFile!=null){
			randomAccessFile.close();
		}
	}
	//
	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
	private String sanitizeUri(String uri) {
		// Decode the path.
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}

		if (uri.isEmpty() || uri.charAt(0) != '/') {
			return null;
		}

		// Convert file separators.
		uri = uri.replace('/', File.separatorChar);

		// Simplistic dumb security check.
		// You will have to do something serious in the production environment.
		if (uri.contains(File.separator + '.')
				|| uri.contains('.' + File.separator) || uri.charAt(0) == '.'
				|| uri.charAt(uri.length() - 1) == '.'
				|| INSECURE_URI.matcher(uri).matches()) {
			return null;
		}

		// Convert to absolute path.
		return cdnServer.getHomeDir()+ File.separator + uri;
	}
}
