/**
 * 
 */
package jazmin.server.file;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpRequest;

import java.io.RandomAccessFile;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class FileDownload extends FileOpt{
	private static Logger logger=LoggerFactory.get(FileDownload.class);
	//
	static interface ResultHandler{
		void handleRandomAccessFile(RandomAccessFile raf);
		void handleNotFound();
		void handleException(Throwable e);
	}
	ResultHandler resultHandler;
	//
	private RandomAccessFile randomAccessFile;
	//
	public FileDownload(FileServer cdnServer,
			String uri,
			Channel channel,
			DefaultHttpRequest httpRequest) {
		super(cdnServer,uri,channel,httpRequest);
	}
	//
	public void open()throws Exception{
		if(logger.isDebugEnabled()){
			logger.debug("open file {}",file);
		}
		if(file==null){
			if(logger.isDebugEnabled()){
				logger.debug("uri convert to path null."+uri);
			}
			resultHandler.handleNotFound();
			return;
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
			resultHandler.handleNotFound();
			close();
			return;
		}
	}
	//
	void close()throws Exception{
		cdnServer.removeFileRequest(this.id);
		if(randomAccessFile!=null){
			randomAccessFile.close();
		}
	}
}
