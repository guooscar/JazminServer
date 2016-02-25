/**
 * 
 */
package jazmin.server.web.mvc;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import jazmin.util.IOUtil;

/**
 * @author yama
 * 8 Jan, 2015
 */
public class StreamView implements View{
	private Stream stream;
	public StreamView(Stream stream) {
		this.stream=stream;
	}
	//
	@Override
	public void render(Context ctx) throws Exception {
		HttpServletResponse response=ctx.response.raw();
		ServletOutputStream outStream = response.getOutputStream();
        response.setContentType(stream.getMimeType());
        // sets HTTP header
        String ff = new String(stream.getFileName().getBytes("UTF-8"), "ISO8859-1");
        response.setHeader("Content-Disposition", 
        		stream.getContentDisposition()+"; filename=\"" + ff + "\"");
        IOUtil.copy(stream.getInputStream(),outStream);
        IOUtil.closeQuietly(stream.getInputStream());
        IOUtil.closeQuietly(outStream);
	}
}
