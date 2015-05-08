/**
 * 
 */
package jazmin.server.cdn;

import static io.netty.handler.codec.http.HttpHeaderNames.CACHE_CONTROL;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.DATE;
import static io.netty.handler.codec.http.HttpHeaderNames.EXPIRES;
import static io.netty.handler.codec.http.HttpHeaderNames.IF_MODIFIED_SINCE;
import static io.netty.handler.codec.http.HttpHeaderNames.LAST_MODIFIED;
import static io.netty.handler.codec.http.HttpHeaderNames.LOCATION;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
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
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;


/**
 * @author yama
 *
 */
public class RequestWorker implements ChannelProgressiveFutureListener,FileRequest.ResultHandler{
	private static Logger logger=LoggerFactory.get(RequestWorker.class);
	//
	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	public static final int HTTP_CACHE_SECONDS = 60;
	FileRequest fileRequest;
	ChannelHandlerContext ctx; 
	FullHttpRequest request; 
	CdnServer cdnServer;
	//
	RequestWorker(
			CdnServer cdnServer,
			FileRequest fileRequest,
			ChannelHandlerContext ctx,
			FullHttpRequest request){
		this.cdnServer=cdnServer;
		this.fileRequest=fileRequest;
		this.ctx=ctx;
		this.request=request;
	}
	@Override
	public void handleInputStream(InputStream inputStream,long length) {
		try {
			sendInputStream(inputStream, length);
		} catch (Exception e) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			logger.catching(e);
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
	//
	private boolean filter() throws Exception {
		if (!request.decoderResult().isSuccess()) {
			if(logger.isDebugEnabled()){
				logger.debug("bad request");
			}
			sendError(ctx, BAD_REQUEST);
			return false;
		}
		if (request.method() != GET) {
			if(logger.isDebugEnabled()){
				logger.debug("method not allowed:"+request.method());
			}
			sendError(ctx, METHOD_NOT_ALLOWED);
			return false;
		}
		if(cdnServer.requestFilter!=null){
			FilterContext filterCtx=new FilterContext();
			filterCtx.fileRequest=fileRequest;
			filterCtx.request=request;
			cdnServer.requestFilter.filter(filterCtx);
			if(filterCtx.errorCode!=FilterContext.CODE_OK){
				if(logger.isDebugEnabled()){
					logger.debug("requestFilter {} reject ",cdnServer.requestFilter);
				}
				sendError(ctx,filterCtx.errorCode,filterCtx.responseMap);
				return false;
			}
		}
		return true;
	}
	public void processRequest(){
		try {
			processRequest0();
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
	private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
		response.headers().set(LOCATION, newUri);

		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
	
	private static void sendError(
			ChannelHandlerContext ctx,
			int statusCode,
			Map<String,String>rspHeaders) {
		HttpResponseStatus status=HttpResponseStatus.valueOf(statusCode);
		logger.warn("send status {}",ctx.channel(),statusCode);
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				status, Unpooled.copiedBuffer(
						status + "\r\n",CharsetUtil.UTF_8));
		rspHeaders.forEach((k,v)->{
			response.headers().set(k, v);
		});
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
	//
	private  static void sendError(ChannelHandlerContext ctx,
			HttpResponseStatus status) {
		logger.warn("send status {}",ctx.channel(),status);
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				status, Unpooled.copiedBuffer(
						status + "\r\n",CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * When file timestamp is the same as what the browser is sending up, send a
	 * "304 Not Modified"
	 *
	 * @param ctx
	 *            Context
	 */
	static void sendNotModified(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				NOT_MODIFIED);
		setDateHeader(response);

		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * Sets the Date header for the HTTP response
	 *
	 * @param response
	 *            HTTP response
	 */
	private static void setDateHeader(FullHttpResponse response) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT,
				Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		Calendar time = new GregorianCalendar();
		response.headers().set(DATE, dateFormatter.format(time.getTime()));
	}

	/**
	 * Sets the Date and Cache headers for the HTTP Response
	 *
	 * @param response
	 *            HTTP response
	 * @param fileToCache
	 *            file to extract content type
	 */
	private static void setDateAndCacheHeaders(HttpResponse response,
			File fileToCache) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT,
				Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		response.headers().set(DATE, dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
		response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
		response.headers().set(CACHE_CONTROL,
				"private, max-age=" + HTTP_CACHE_SECONDS);
		response.headers().set(LAST_MODIFIED,
				dateFormatter.format(new Date(fileToCache.lastModified())));
	}

	/**
	 * Sets the content type header for the HTTP Response
	 *
	 * @param response
	 *            HTTP response
	 * @param file
	 *            file to extract content type
	 */
	private static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(CONTENT_TYPE,
				mimeTypesMap.getContentType(file.getPath()));
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
