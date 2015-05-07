/**
 * 
 */
package jazmin.server.cdn;

import io.netty.channel.Channel;
import io.netty.handler.codec.Headers.NameVisitor;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;

/**
 * @author yama
 *
 */
public class FileRequest implements AsyncHandler<String>{
	static interface ResultHandler{
		void handleInputStream(InputStream inputStream,long fileLength);
		void handleRandomAccessFile(RandomAccessFile raf);
		void handleNotFound();
		void handleException(Throwable e);
	}
	//
	private static Logger logger=LoggerFactory.get(FileRequest.class);
	static final String TYPE_REMOTE_STREAM="remote";
	static final String TYPE_LOCAL_FILE="local";
	//
	String id;
	File file;
	String uri;
	Channel channel;
	Date createTime;
	long totalBytes;
	long transferedBytes;
	InetSocketAddress remoteAddress;
	CdnServer cdnServer;
	ResultHandler resultHandler;
	String sourceType;
	FullHttpRequest httpRequest;
	//
	private RandomAccessFile randomAccessFile;
	private InputStream inputStream;
	private PipedOutputStream outputStream;
	//
	public FileRequest(CdnServer cdnServer,
			String uri,
			Channel channel,
			FullHttpRequest httpRequest) {
		this.cdnServer=cdnServer;
		this.httpRequest=httpRequest;
		this.uri=uri;
		this.channel=channel;
		remoteAddress=(InetSocketAddress)channel.remoteAddress();
		createTime=new Date();
		final String path = uri2FilePath(uri);
		if (path != null) {
			file = new File(path);
		}
	}
	//
	public void open()throws Exception{
		if(file==null){
			if(logger.isDebugEnabled()){
				logger.debug("uri convert to path null."+uri);
			}
			resultHandler.handleNotFound();
		}
		//
		if(file.exists()){
			if (file.isHidden()) {
				if(logger.isDebugEnabled()){
					logger.debug("hidden file {}",file);
				}
				resultHandler.handleNotFound();
				return;
			}
			// system device file
			if (!file.isDirectory() && !file.isFile()) {
				if(logger.isDebugEnabled()){
					logger.debug("system file {}",file);
				}
				resultHandler.handleNotFound();
				return;
			}
			if(logger.isDebugEnabled()){
				logger.debug("open random access file {}",file);
			}
			randomAccessFile=new RandomAccessFile(file,"r");
			totalBytes=randomAccessFile.length();
			sourceType=TYPE_LOCAL_FILE;
			resultHandler.handleRandomAccessFile(randomAccessFile);
			return;
		}else{
			if(cdnServer.getOrginSiteURL()==null){
				resultHandler.handleNotFound();
				return;
			}
			sourceType=TYPE_REMOTE_STREAM;
			String targetURL=cdnServer.getOrginSiteURL()+uri;
			Map<String,Collection<String>>headerMap=new HashMap<String,Collection<String>>();
			httpRequest.headers().forEachName(new NameVisitor<CharSequence>() {
				@Override
				public boolean visit(CharSequence name) throws Exception {
					headerMap.put(name.toString(),
							httpRequest.headers().getAllAndConvert(name));
					return true;
				}
			});
			cdnServer.asyncHttpClient
			.prepareGet(targetURL)
			.setHeaders(headerMap)
			.execute(this);
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
	private String uri2FilePath(String uri) {
		// Decode the path.
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}

		if (uri.isEmpty() || uri.charAt(0) != '/') {
			return null;
		}
		//
		int indexOfParameter=uri.indexOf('?');
		if(indexOfParameter!=-1){
			uri=uri.substring(0,indexOfParameter);
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
	//
	//--------------------------------------------------------------------------
	@Override
	public STATE onBodyPartReceived(HttpResponseBodyPart part) throws Exception {
		byte partBytes[]=part.getBodyPartBytes();
		tempFileOutputStream.write(partBytes);
		outputStream.write(partBytes);
		return STATE.CONTINUE;
	}
	//
	@Override
	public String onCompleted() throws Exception {
		if(logger.isDebugEnabled()){
			logger.debug("complete fetch {}, move to {}",
					cdnServer.getOrginSiteURL()+uri,
					file);
		}
		try{
			outputStream.flush();
			outputStream.close();
		}catch(Exception e){
			logger.catching(e);
		}
		try{
			tempFileOutputStream.flush();
			tempFileOutputStream.close();
		}catch(Exception e){
			logger.catching(e);	
		}
		cdnServer.cachePolicy.moveTo(tempFile,file);
		return "";
	}
	//
	private File tempFile;
	private FileOutputStream tempFileOutputStream;
	//
	@Override
	public STATE onHeadersReceived(
			HttpResponseHeaders headers) throws Exception {
		//
		String len=headers.getHeaders().getFirstValue("Content-Length");
		if(len!=null){
			totalBytes=Long.valueOf(len);
		}
		if(logger.isDebugEnabled()){
			logger.debug("got length {} bytes from uri {}",totalBytes,uri);
		}
		tempFile=cdnServer.cachePolicy.createTempFile();
		tempFileOutputStream=new FileOutputStream(tempFile);
		outputStream=new PipedOutputStream();
		inputStream=new PipedInputStream(outputStream);
		resultHandler.handleInputStream(inputStream, totalBytes);
		//
		return STATE.CONTINUE;
	}
	//
	@Override
	public STATE onStatusReceived(
			HttpResponseStatus status) throws Exception {
		if(status.getStatusCode()!=200){
			resultHandler.handleNotFound();
			return STATE.ABORT;
		}
		return STATE.CONTINUE;
	}
	//
	@Override
	public void onThrowable(Throwable e) {
		resultHandler.handleException(e);
		if(tempFileOutputStream!=null){
			try {
				tempFileOutputStream.close();
			} catch (IOException e1) {
				logger.catching(e1);
			}
		}
		if(tempFile!=null){
			boolean success=tempFile.delete();
			logger.debug("delete temp file {} result {}",tempFile,success);
		}
		if(e instanceof IOException){
			logger.warn("uri {} catch exception {}",uri,e);
		}else{
			logger.catching(e);
		}
	}
}
