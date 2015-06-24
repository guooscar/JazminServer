/**
 * 
 */
package jazmin.server.web.mvc;

import javax.servlet.http.HttpServletResponse;


/**
 * @author yama
 * 29 Dec, 2014
 */
public class RedirectView implements View{
	String url;
	public RedirectView(String url) {
		this.url=url;
	}
	//
	@Override
	public void render(Context ctx) throws Exception {
		HttpServletResponse response=ctx.response.raw();
		response.sendRedirect(url);
	}
}