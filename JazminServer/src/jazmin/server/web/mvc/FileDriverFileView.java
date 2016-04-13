/**
 * 
 */
package jazmin.server.web.mvc;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import jazmin.driver.file.FileServerDriver;
import jazmin.util.IOUtil;

/**
 * @author yama
 * 8 Jan, 2015
 */
public class FileDriverFileView implements View{
	//
	private int cacheSeconds=24*3600*30;
	private String fileId;
	private FileServerDriver fileDriver;
	private String fileName;
	private String contentDisposition;
	public FileDriverFileView(FileServerDriver fileDriver,String fileId) {
		this.fileDriver=fileDriver;
		this.fileId=fileId;
		contentDisposition="attachment";
	}
	//
	public FileDriverFileView(FileServerDriver fileDriver,String fileId,String fileName) {
		this(fileDriver,fileId);
		this.fileName=fileName;
	}
	//
	
	//
	@Override
	public void render(Context ctx) throws Exception {
		HttpServletResponse response=ctx.response.raw();
		File file=fileDriver.getFile(fileId);
		if(file==null){
			file=fileDriver.downloadFile(fileId);
		}
		String fn=file.getName();
		if(fileName!=null){
			fn=fileName;
		}
		writeCommonHeader(file.length(),fn,response);
		ServletOutputStream outStream = response.getOutputStream();
	    FileInputStream fis=new FileInputStream(file);
        IOUtil.copy(fis,outStream);
        IOUtil.closeQuietly(fis);
        IOUtil.closeQuietly(outStream);
    }
	/**
	 * @return the contentDisposition
	 */
	public String getContentDisposition() {
		return contentDisposition;
	}
	/**
	 * @param contentDisposition the contentDisposition to set
	 */
	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}
	/**
	 * @return the cacheSeconds
	 */
	public int getCacheSeconds() {
		return cacheSeconds;
	}
	/**
	 * @param cacheSeconds the cacheSeconds to set
	 */
	public void setCacheSeconds(int cacheSeconds) {
		this.cacheSeconds = cacheSeconds;
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
        		contentDisposition+"; filename=\"" + filename + "\"");
        if(cacheSeconds>0){
        	long expiry = new Date().getTime() + cacheSeconds*1000;
        	response.setDateHeader("Expires", expiry);
        	response.setHeader("Cache-Control", "max-age="+ cacheSeconds);
        }
        response.addHeader( "Cache-Control", "public, max-age=90000" );
	}
}
