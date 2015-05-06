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
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
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
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;


/**
 * @author yama
 *
 */
public class RequestWorker implements ChannelProgressiveFutureListener{
	private static Logger logger=LoggerFactory.get(RequestWorker.class);
	//
	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	public static final int HTTP_CACHE_SECONDS = 60;
	FileRequest fileRequest;
	//
	RequestWorker(FileRequest fileRequest){
		this.fileRequest=fileRequest;
	}
	//
	private boolean filter(
			ChannelHandlerContext ctx, 
			FullHttpRequest request) throws Exception {
		//
		if (!request.decoderResult().isSuccess()) {
			sendError(ctx, BAD_REQUEST);
			return false;
		}
		if (request.method() != GET) {
			sendError(ctx, METHOD_NOT_ALLOWED);
			return false;
		}
		if(!fileRequest.open()){
			sendError(ctx, NOT_FOUND);
			return false;
		}
		return true;
	}
	//
	private void sendFile(
			ChannelHandlerContext ctx, 
			FullHttpRequest request) throws Exception {
		// send stream to client
		if(fileRequest.randomAccessFile!=null){
			sendRandomAccessFile(ctx, request);
		}else{
			//send stream
		}
	}
	//
	private void sendRandomAccessFile(
			ChannelHandlerContext ctx, 
			FullHttpRequest request)throws Exception{
		RandomAccessFile raf = fileRequest.randomAccessFile;
		File file=fileRequest.file;
		long fileLength = raf.length();
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		HttpHeaderUtil.setContentLength(response, fileLength);
		setContentTypeHeader(response, file);
		setDateAndCacheHeaders(response, file);
		if (HttpHeaderUtil.isKeepAlive(request)) {
			response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		// Write the initial line and the header.
		ctx.write(response);
		// Write the content.
		ChannelFuture sendFileFuture;
		ChannelFuture lastContentFuture;
		sendFileFuture = ctx.write(new DefaultFileRegion(
				raf.getChannel(),0, fileLength), 
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
	@Override
	public void operationProgressed(
			ChannelProgressiveFuture future,
			long progress, long total) {
		fileRequest.totalBytes=total;
		fileRequest.transferedBytes=progress;
		if (total < 0) { // total unknown
			System.err.println(future.channel()
					+ " Transfer progress: " + progress);
		} else {
			System.err.println(future.channel()
					+ " Transfer progress: " + progress + " / " + total);
			try {
				fileRequest.close();
			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}
	@Override
	public void operationComplete(ChannelProgressiveFuture future) {
		System.err.println(future.channel() + " Transfer complete.");
	}
	//--------------------------------------------------------------------------
	public void processRequest(
			ChannelHandlerContext ctx,
			FullHttpRequest request) throws Exception {
		if (!filter(ctx, request)) {
			fileRequest.close();
			return;
		}
		String uri=fileRequest.uri;
		File file=fileRequest.file;
		if (file.isDirectory()) {
			if (uri.endsWith("/")) {
				sendListing(ctx, file);
			} else {
				sendRedirect(ctx, uri + '/');
			}
			fileRequest.close();
			return;
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
		sendFile(ctx, request);
	}

	//
	private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
		response.headers().set(LOCATION, newUri);

		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	//
	private static void sendError(ChannelHandlerContext ctx,
			HttpResponseStatus status) {
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
	private static void sendNotModified(ChannelHandlerContext ctx) {
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
	private static final Pattern ALLOWED_FILE_NAME = Pattern
			.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
	//
	private static void sendListing(ChannelHandlerContext ctx, File dir) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
		StringBuilder buf = new StringBuilder();
		for (File f : dir.listFiles()) {
			if (f.isHidden() || !f.canRead()) {
				continue;
			}
			String name = f.getName();
			if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
				continue;
			}
			buf.append(name).append("\r\n");
		}
		ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
		response.content().writeBytes(buffer);
		buffer.release();
		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

}
