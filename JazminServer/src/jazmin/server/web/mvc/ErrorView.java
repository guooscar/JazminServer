/**
 * 
 */
package jazmin.server.web.mvc;

import javax.servlet.http.HttpServletResponse;

/**
 * @author yama
 * 8 Jan, 2015
 */
public class ErrorView implements View{
	private int code;
	public ErrorView(int code) {
		this.code=code;
	}
	//
	@Override
	public void render(Context ctx) throws Exception {
		HttpServletResponse response=ctx.response.raw();
		response.sendError(code);
	}
}
