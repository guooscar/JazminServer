/**
 * 
 */
package jazmin.server.file;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpRequest;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Date;

/**
 * @author yama
 *
 */
public class FileOpt {
	static final String TYPE_LOCAL_FILE="local";
	static final String TYPE_UPLOAD_FILE="upload";
	
	String sourceType;
	String id;
	File file;
	String uri;
	Channel channel;
	Date createTime;
	long totalBytes;
	long transferedBytes;
	InetSocketAddress remoteAddress;
	FileServer cdnServer;
	DefaultHttpRequest httpRequest;
	
	//
	public FileOpt(FileServer cdnServer,
			String uri,
			Channel channel,
			DefaultHttpRequest httpRequest) {
		this.cdnServer=cdnServer;
		this.httpRequest=httpRequest;
		this.uri=uri;
		this.channel=channel;
		remoteAddress=(InetSocketAddress)channel.remoteAddress();
		createTime=new Date();
		file =  uri2FilePath(uri);
	}
	//
	protected File uri2FilePath(String uri) {
		// Decode the path.
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}
		if(!uri.startsWith("/download/")){
			return null;
		}
		int lastIdxOfSlash=uri.lastIndexOf('/');
		String file=uri.substring(lastIdxOfSlash+1);
		// Convert to absolute path.
		return new File(cdnServer.getHomeDir()+"/"+file.charAt(0)+"/"+file.charAt(1),file);
	}
	
}
