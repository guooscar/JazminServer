/**
 * 
 */
package jazmin.server.web.mvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import jazmin.driver.file.FileServerDriver;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.file.FileDownloadHandler;
import jazmin.util.IOUtil;

/**
 * @author yama
 * 8 Jan, 2015
 */
public class FileDriverFileView implements View,FileDownloadHandler{
	private static Logger logger=LoggerFactory.get(FileDriverFileView.class);
	//
	private int cacheSeconds=24*3600*30;
	private String fileId;
	private FileServerDriver fileDriver;
	private Context ctx;
	public FileDriverFileView(FileServerDriver fileDriver,String fileId) {
		this.fileDriver=fileDriver;
		this.fileId=fileId;
	}
	//
	@Override
	public void render(Context ctx) throws Exception {
		this.ctx=ctx;
		HttpServletResponse response=ctx.response.raw();
		File file=fileDriver.getFile(fileId);
		if(!file.exists()){
			fileDriver.downloadFile(fileId, this);
			return;
		}
		writeCommonHeader(file.length(),file.getName(),response);
		ServletOutputStream outStream = response.getOutputStream();
	    FileInputStream fis=new FileInputStream(file);
        IOUtil.copy(fis,outStream);
        IOUtil.closeQuietly(fis);
        IOUtil.closeQuietly(outStream);
    }
	//
	private void writeCommonHeader(long length,String name,HttpServletResponse response)
			throws Exception{
		  // sets response content type
		String	mimetype = MimeMaps.getMimeTypeByFileName(fileId);
		if(mimetype==null){
			mimetype = "application/octet-stream";
		}
        response.setContentType(mimetype);
        response.setContentLengthLong(length);
        // sets HTTP header
        String filename = new String(name.getBytes("UTF-8"), "ISO8859-1");
        response.setHeader("Content-Disposition", 
        		"attachment; filename=\"" + filename + "\"");
        if(cacheSeconds>0){
        	long expiry = new Date().getTime() + cacheSeconds*1000;
        	response.setDateHeader("Expires", expiry);
        	response.setHeader("Cache-Control", "max-age="+ cacheSeconds);
        }
        response.addHeader( "Cache-Control", "public, max-age=90000" );
	}
	//
	@Override
	public void handleInputStream(InputStream inputStream, long fileLength) {
		try{
			HttpServletResponse response=ctx.response.raw();
			ServletOutputStream outStream = response.getOutputStream();
		    IOUtil.copy(inputStream,outStream);
	        IOUtil.closeQuietly(inputStream);
	        IOUtil.closeQuietly(outStream);
		}catch(Exception e){
			logger.catching(e);
		}
	}
	@Override
	public void handleNotFound() {
		HttpServletResponse response=ctx.response.raw();
		try {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			logger.catching(e);
		}
		
	}
	@Override
	public void handleException(Throwable e) {
		logger.catching(e);
		ctx.view(new ErrorView(500));
	}
}
