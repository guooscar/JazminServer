/**
 * 
 */
package jazmin.server.cdn;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.IF_MODIFIED_SINCE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;


/**
 * A simple handler that serves incoming HTTP requests to send their respective
 * HTTP responses.  It also implements {@code 'If-Modified-Since'} header to
 * take advantage of browser cache, as described in
 * <a href="http://tools.ietf.org/html/rfc2616#section-14.25">RFC 2616</a>.
 *
 * <h3>How Browser Caching Works</h3>
 *
 * Web browser caching works with HTTP headers as illustrated by the following
 * sample:
 * <ol>
 * <li>Request #1 returns the content of {@code /file1.txt}.</li>
 * <li>Contents of {@code /file1.txt} is cached by the browser.</li>
 * <li>Request #2 for {@code /file1.txt} does return the contents of the
 *     file again. Rather, a 304 Not Modified is returned. This tells the
 *     browser to use the contents stored in its cache.</li>
 * <li>The server knows the file has not been modified because the
 *     {@code If-Modified-Since} date is the same as the file's last
 *     modified date.</li>
 * </ol>
 *
 * <pre>
 * Request #1 Headers
 * ===================
 * GET /file1.txt HTTP/1.1
 *
 * Response #1 Headers
 * ===================
 * HTTP/1.1 200 OK
 * Date:               Tue, 01 Mar 2011 22:44:26 GMT
 * Last-Modified:      Wed, 30 Jun 2010 21:36:48 GMT
 * Expires:            Tue, 01 Mar 2012 22:44:26 GMT
 * Cache-Control:      private, max-age=31536000
 *
 * Request #2 Headers
 * ===================
 * GET /file1.txt HTTP/1.1
 * If-Modified-Since:  Wed, 30 Jun 2010 21:36:48 GMT
 *
 * Response #2 Headers
 * ===================
 * HTTP/1.1 304 Not Modified
 * Date:               Tue, 01 Mar 2011 22:44:28 GMT
 *
 * </pre>
 */
public class GetRequestWorker extends RequestWorker implements 
ChannelProgressiveFutureListener,FileDownload.ResultHandler{
	private static Logger logger=LoggerFactory.get(GetRequestWorker.class);
	//
	FileDownload fileRequest;
	GetRequestWorker(
			CdnServer cdnServer,
			FileDownload fileRequest,
			ChannelHandlerContext ctx,
			DefaultHttpRequest request){
		super(cdnServer, ctx, request);
		this.fileRequest=fileRequest;
	}
	@Override
	public void handleInputStream(InputStream inputStream,long length) {
		try {
			sendInputStream(inputStream, length);
		} catch (Exception e) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			logger.catching(e);
		}finally{
			
		}
	}
	@Override
	public void handleRandomAccessFile(RandomAccessFile raf){
		try {
			sendRaf(raf);
		} catch (Exception e) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			logger.catching(e);
		}
	}
	@Override
	public void handleException(Throwable e) {
		sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
	}
	//
	@Override
	public void handleNotFound() {
		if(logger.isDebugEnabled()){
			logger.debug("handle not found {} {}",fileRequest.uri);
		}
		sendError(ctx, HttpResponseStatus.NOT_FOUND);
	}
	
	//
	private void sendObject(Object obj,long length){
		long fileLength = length;
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		HttpHeaderUtil.setContentLength(response, fileLength);
		File file=fileRequest.file;
		setContentTypeHeader(response, file);
		setDateAndCacheHeaders(response, file);
		//
		if (HttpHeaderUtil.isKeepAlive(request)) {
			response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		// Write the initial line and the header.
		ctx.write(response);
		// Write the content.
		ChannelFuture sendFileFuture;
		ChannelFuture lastContentFuture;
		sendFileFuture = ctx.write(
				obj, 
				ctx.newProgressivePromise());
			// Write the end marker.
		lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		sendFileFuture.addListener(this);
		// Decide whether to close the connection or not.
		if (!HttpHeaderUtil.isKeepAlive(request)) {
			// Close the connection when the whole content is written out.
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}
	//
	private void sendInputStream(InputStream in,long length)throws Exception{
		if(logger.isDebugEnabled()){
			logger.debug("send input stream to client total bytes:{}",length);
		}
		sendObject(new ChunkedStream(in,8192), length);
	}
	//
	private void sendRaf(RandomAccessFile raf)throws Exception{
		long length=raf.length();
		if(logger.isDebugEnabled()){
			logger.debug("send file to client total bytes:{}",length);
		}
		sendObject(new DefaultFileRegion(
				raf.getChannel(),0, length),length);
	}
	//--------------------------------------------------------------------------
	//
	@Override
	public void operationProgressed(
			ChannelProgressiveFuture future,
			long progress, long total) {
		fileRequest.transferedBytes=progress;
	}
	//
	@Override
	public void operationComplete(ChannelProgressiveFuture future) {
		try {
			fileRequest.close();
		} catch (Exception e) {
			logger.catching(e);
		}
		logger.info("process request {} from {} complete time {} seconds",
				request.uri(),
				ctx.channel(),
				(System.currentTimeMillis()-fileRequest.createTime.getTime())/1000);
	}
	//--------------------------------------------------------------------------
	
	public void processRequest(){
		try {
			processRequest0();
			if(cdnServer.requestFilter!=null){
				cdnServer.requestFilter.endDownload(filterCtx);
			}
		} catch (Exception e) {
			logger.catching(e);
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}
	//
	private void processRequest0() throws Exception {
		logger.info("process request from {}",ctx.channel());
		if (!filter()) {
			fileRequest.close();
			return;
		}
		String uri=fileRequest.uri;
		File file=fileRequest.file;
		if(file!=null){
			if (file.isDirectory()) {
				if(logger.isDebugEnabled()){
					logger.debug("uri {} is directory",uri);
				}
				if(cdnServer.getDirectioryPrinter()!=null){
					if (uri.endsWith("/")) {
						sendListing(ctx,file,cdnServer.getDirectioryPrinter());
					} else {
						sendRedirect(ctx, uri + '/');
					}
				}else{
					sendError(ctx,HttpResponseStatus.FORBIDDEN);
				}
				fileRequest.close();
				return;
			}
		}
		// Cache Validation
		String ifModifiedSince = request.headers().getAndConvert(
				IF_MODIFIED_SINCE);
		if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat(
					HTTP_DATE_FORMAT, Locale.US);
			Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
			// Only compare up to the second because the datetime format we send
			// to the client
			// does not have milliseconds
			long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
			long fileLastModifiedSeconds = file.lastModified() / 1000;
			if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
				sendNotModified(ctx);
				return;
			}
		}
		fileRequest.resultHandler=this;
		fileRequest.open();
	}
	//
	private static void sendListing(
			ChannelHandlerContext ctx,
			File dir,
			DirectioryPrinter printer) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
		response.headers().set(CONTENT_TYPE, printer.contentType());
		ByteBuf buffer = Unpooled.copiedBuffer(printer.print(dir), CharsetUtil.UTF_8);
		response.content().writeBytes(buffer);
		buffer.release();
		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
}
