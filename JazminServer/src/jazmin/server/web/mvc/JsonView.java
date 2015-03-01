/**
 * 
 */
package jazmin.server.web.mvc;

import javax.servlet.http.HttpServletResponse;

import jazmin.util.JSONUtil;

/**
 * @author yama
 * 29 Dec, 2014
 */
public class JsonView implements View{
	
	@Override
	public void render(Context ctx) throws Exception {
		HttpServletResponse response=ctx.response.raw();
		response.setCharacterEncoding(Response.DEFAULT_CHARSET_ENCODING);
		response.setContentType("text/javascript;charset=UTF-8");
		String jsonStr=JSONUtil.toJson(ctx.contextMap);
		byte outBytes[]=jsonStr.getBytes("UTF-8");
		response.getOutputStream().write(outBytes);
		response.setContentLength(outBytes.length);
	}
}