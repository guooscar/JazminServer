/**
 * 
 */
package jazmin.server.web.mvc;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import jazmin.util.IOUtil;

/**
 * @author yama
 * 8 Jan, 2015
 */
public class FileView implements View{
	private File file;
	private String mimetype;
	private int cacheSeconds;
	private String contentDisposition;
	public FileView(String filePath,String mimetype) {
		this.file=new File(filePath);
		this.mimetype=mimetype;
		contentDisposition="attachment";
	}
	public FileView(String filePath,String mimetype,int cacheSeconds) {
		this.file=new File(filePath);
		this.mimetype=mimetype;
		this.cacheSeconds=cacheSeconds;
		contentDisposition="attachment";
	}
	public FileView(String filePath) {
		this(filePath,null);
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
	//
	@Override
	public void render(Context ctx) throws Exception {
		HttpServletResponse response=ctx.response.raw();
		if(!file.exists()){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		ServletOutputStream outStream = response.getOutputStream();
        // sets response content type
		if(mimetype==null){
			mimetype = MimeMaps.getMimeTypeByFile(file);
		}
		if(mimetype==null){
			mimetype = "application/octet-stream";
		}
        response.setContentType(mimetype);
        response.setContentLengthLong(file.length());
        // sets HTTP header
        String filename = new String(file.getName().getBytes("UTF-8"), "ISO8859-1");
        response.setHeader("Content-Disposition", 
        		contentDisposition+"; filename=\"" + java.net.URLEncoder.encode(filename,"UTF-8") + "\"");
        if(cacheSeconds>0){
        	long expiry = new Date().getTime() + cacheSeconds*1000;
        	response.setDateHeader("Expires", expiry);
        	response.setHeader("Cache-Control", "max-age="+ cacheSeconds);
        }
        response.addHeader( "Cache-Control", "public, max-age=90000" );
        FileInputStream fis=new FileInputStream(file);
        IOUtil.copy(fis,outStream);
        IOUtil.closeQuietly(fis);
        IOUtil.closeQuietly(outStream);
    }
}
