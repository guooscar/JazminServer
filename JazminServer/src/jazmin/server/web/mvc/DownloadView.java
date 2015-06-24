/**
 * 
 */
package jazmin.server.web.mvc;

import java.io.ByteArrayInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import jazmin.util.IOUtil;

/**
 * @author yama
 * 8 Jan, 2015
 */
public class DownloadView implements View{
	private byte [] content;
	private String filename;
	private String mimetype;
	public DownloadView(String filename,byte[]content,String mimetype) {
		this.content=content;
		this.mimetype=mimetype;
		this.filename=filename;
	}
	public DownloadView(String filename,byte [] content) {
		this(filename,content,null);
	}
	//
	@Override
	public void render(Context ctx) throws Exception {
		HttpServletResponse response=ctx.response.raw();
		ServletOutputStream outStream = response.getOutputStream();
        // sets response content type
		if(mimetype==null){
			mimetype = "application/octet-stream";
		}
        response.setContentType(mimetype);
        response.setContentLengthLong(content.length);
        // sets HTTP header
        String ff = new String(filename.getBytes("UTF-8"), "ISO8859-1");
        response.setHeader("Content-Disposition", 
        		"attachment; filename=\"" + ff + "\"");
        ByteArrayInputStream fis=new ByteArrayInputStream(content);
        IOUtil.copy(fis,outStream);
        IOUtil.closeQuietly(fis);
        IOUtil.closeQuietly(outStream);
	}
}
