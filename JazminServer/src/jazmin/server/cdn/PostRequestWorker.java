/**
 * 
 */
package jazmin.server.cdn;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.ReferenceCountUtil;

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
			DefaultHttpDataFactory.MINSIZE);
	HttpPostRequestDecoder decoder;

	//
	PostRequestWorker(CdnServer cdnServer, ChannelHandlerContext ctx,
			FullHttpRequest request) {
		super(cdnServer, ctx, request);
	}

	public void processRequest() {
		ReferenceCountUtil.retain(request);
		try {
			decoder = new HttpPostRequestDecoder(factory, request);
		} catch (ErrorDataDecoderException e1) {
			e1.printStackTrace();
			ctx.channel().close();
			return;
		}

		boolean readingChunks = HttpHeaderUtil
				.isTransferEncodingChunked(request);
		if (readingChunks) {
			readingChunks = true;
		}
		// check if the decoder was constructed before
		// if not it handles the form get
		if (decoder != null) {
			if (request instanceof HttpContent) {
				// New chunk is received
				HttpContent chunk = (HttpContent) request;
				try {
					decoder.offer(chunk);
				} catch (ErrorDataDecoderException e1) {
					e1.printStackTrace();
					ctx.channel().close();
					return;
				}
				// example of reading chunk by chunk (minimize memory usage due
				// to
				// Factory)
				readHttpDataChunkByChunk();
				// example of reading only if at the end
				if (chunk instanceof LastHttpContent) {
					readingChunks = false;
					reset();
				}
			}
		} else {
		}
	}

	private void reset() {
		request = null;

		// destroy the decoder to release all resources
		decoder.destroy();
		decoder = null;
	}

	//
	/**
	 * Example of reading request by chunk and getting values from chunk to
	 * chunk
	 */
	private void readHttpDataChunkByChunk() {
		try {
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
		} catch (EndOfDataDecoderException e1) {
			logger.catching(e1);
		}
	}

	private void writeHttpData(InterfaceHttpData data) {
		if (data.getHttpDataType() == HttpDataType.Attribute) {
			Attribute attribute = (Attribute) data;
			String value;
			try {
				value = attribute.getValue();
			} catch (IOException e1) {
				logger.catching(e1);
				return;
			}
			if (value.length() > 100) {

			} else {

			}
		} else {

			if (data.getHttpDataType() == HttpDataType.FileUpload) {
				FileUpload fileUpload = (FileUpload) data;
				if (fileUpload.isCompleted()) {
					try {
						fileUpload.renameTo(new File("d:/tmp"));
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (fileUpload.length() < 10000) {

					} else {

					}
					// fileUpload.isInMemory();// tells if the file is in Memory
					// or on File
					// fileUpload.renameTo(dest); // enable to move into another
					// File dest
					// decoder.removeFileUploadFromClean(fileUpload); //remove
					// the File of to delete file
				} else {

				}
			}
		}
	}
}
