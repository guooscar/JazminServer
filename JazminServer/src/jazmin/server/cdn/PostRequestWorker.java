/**
 * 
 */
package jazmin.server.cdn;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class PostRequestWorker extends RequestWorker {
	private static Logger logger = LoggerFactory.get(PostRequestWorker.class);
	//
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(
			DefaultHttpDataFactory.MAXSIZE);
	HttpPostRequestDecoder decoder;
	static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
                                                         // on exit (in normal
                                                         // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
                                                        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }
	//
	boolean saveComplete;
	jazmin.server.cdn.FileUpload fileUpload;
	//
	PostRequestWorker(CdnServer cdnServer, ChannelHandlerContext ctx,
			DefaultHttpRequest request,jazmin.server.cdn.FileUpload fileUpload) {
		super(cdnServer, ctx, request);
		this.fileUpload=fileUpload;
	}
	//
	public void processRequest(){
		try {
			processRequest0();
		} catch (Exception e) {
			logger.catching(e);
			clean();
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@Override
	protected boolean filter() throws Exception {
		boolean f=super.filter();
		if(f){
			if(!requestFile.exists()){
				//
				File parentDir=requestFile.getParentFile();
				if(!parentDir.exists()){
					parentDir.mkdirs();
				}
				//
				if(!requestFile.createNewFile()){
					sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE);
					return false;
				}
			}else{
				sendError(ctx, HttpResponseStatus.NOT_ACCEPTABLE);
				return false;
			}
		}
		return f;
	}
	//
	private void processRequest0()throws Exception{
		logger.info("process request from {}",ctx.channel());
		if (!filter()) {
			fileUpload.close();
			return;
		}
		decoder = new HttpPostRequestDecoder(factory, request);
		fileUpload.totalBytes=request.headers().getLong("Content-Length",0);
	}
	//
	private void handleContent(DefaultHttpContent content) throws IOException{
		
		// if not it handles the form get
		if (decoder != null) {
			if (content instanceof HttpContent) {
				// New chunk is received
				HttpContent chunk = (HttpContent) content;
				decoder.offer(chunk);
				fileUpload.transferedBytes+=content.content().capacity();
				readHttpDataChunkByChunk();
			}
		} 
		//
		if(content instanceof LastHttpContent){
			if(!saveComplete){
				sendError(ctx, HttpResponseStatus.BAD_REQUEST);
			}else{
				sendResult(ctx, requestFile);
			}	
			clean();
		}
	}
	//
	private void clean(){
		try {
			fileUpload.close();
		} catch (Exception e) {
			logger.catching(e);
		}
		if(requestFile!=null){
			requestFile.delete();
		}
	}
	//
	@Override
	public void handleHttpContent(DefaultHttpContent content) {
		try {
			handleContent(content);
		} catch (Exception e) {
			logger.catching(e);
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}		
	}
	//
	/**
	 * Example of reading request by chunk and getting values from chunk to
	 * chunk
	 * @throws IOException 
	 */
	private void readHttpDataChunkByChunk() throws IOException {
		try{
			while (decoder.hasNext()) {
				InterfaceHttpData data = decoder.next();
				if (data != null) {
					try {
						// new value
						writeHttpData(data);
					} finally {
						data.release();
					}
				}
			}
		}catch (EndOfDataDecoderException e1) {}
	}
	//
	private void writeHttpData(InterfaceHttpData data) throws IOException {
		if (data.getHttpDataType() == HttpDataType.FileUpload) {
			FileUpload fu = (FileUpload) data;
			if (fu.isCompleted()) {
				saveComplete=true;
				fu.renameTo(requestFile);
			}
		}	
	}
	//
	private static void sendResult(
			ChannelHandlerContext ctx,
			File resultFile) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
		response.headers().set(CONTENT_TYPE, "text/plain");
		ByteBuf buffer = Unpooled.copiedBuffer(resultFile.getAbsolutePath(), CharsetUtil.UTF_8);
		response.content().writeBytes(buffer);
		buffer.release();
		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
}
