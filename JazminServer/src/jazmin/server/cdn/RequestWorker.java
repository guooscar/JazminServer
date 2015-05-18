/**
 * 
 */
package jazmin.server.cdn;

import static io.netty.handler.codec.http.HttpHeaderNames.CACHE_CONTROL;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.DATE;
import static io.netty.handler.codec.http.HttpHeaderNames.EXPIRES;
import static io.netty.handler.codec.http.HttpHeaderNames.LAST_MODIFIED;
import static io.netty.handler.codec.http.HttpHeaderNames.LOCATION;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.File;
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
public class RequestWorker{
	private static Logger logger=LoggerFactory.get(RequestWorker.class);
	//
	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	public static final int HTTP_CACHE_SECONDS = 60;
	protected ChannelHandlerContext ctx; 
	protected FullHttpRequest request; 
	protected CdnServer cdnServer;
	//
	RequestWorker(
			CdnServer cdnServer,
			ChannelHandlerContext ctx,
			FullHttpRequest request){
		this.cdnServer=cdnServer;
		this.ctx=ctx;
		this.request=request;
	}
	//
	protected boolean filter() throws Exception {
		if (!request.decoderResult().isSuccess()) {
			if(logger.isDebugEnabled()){
				logger.debug("bad request");
			}
			sendError(ctx, BAD_REQUEST);
			return false;
		}
		if(cdnServer.requestFilter!=null){
			FilterContext filterCtx=new FilterContext();
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
	//
	public void processRequest(){
		
	}
	
	//
	public static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
		response.headers().set(LOCATION, newUri);

		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
	
	public static void sendError(
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
	public  static void sendError(ChannelHandlerContext ctx,
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
	public static void sendNotModified(ChannelHandlerContext ctx) {
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
	public static void setDateAndCacheHeaders(HttpResponse response,
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
	public static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(CONTENT_TYPE,
				mimeTypesMap.getContentType(file.getPath()));
	}
}
