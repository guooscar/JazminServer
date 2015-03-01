/**
 * 
 */
package jazmin.server.web.mvc;

import javax.servlet.http.HttpServletResponse;


/**
 * @author yama
 * 29 Dec, 2014
 */
public class PlainTextView implements View{
	String text;
	public PlainTextView(String text) {
		this.text=text;
	}
	//
	@Override
	public void render(Context ctx) throws Exception {
		HttpServletResponse response=ctx.response.raw();
		response.setCharacterEncoding(Response.DEFAULT_CHARSET_ENCODING);
		response.setContentType("text/plain;charset=UTF-8");
		byte outBytes[]=text.getBytes("UTF-8");
		response.getOutputStream().write(outBytes);
		response.setContentLength(outBytes.length);
	}
}