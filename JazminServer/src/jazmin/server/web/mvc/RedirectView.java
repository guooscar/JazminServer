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
	String title;
	public RedirectView(String url) {
		this.url=url;
	}
	public RedirectView(String url,String title) {
		this.url=url;
		this.title=title;
	}
	//
	public void setTitle(String title){
		this.title=title;
	}
	//
	@Override
	public void render(Context ctx) throws Exception {
		HttpServletResponse response=ctx.response.raw();
		if(title!=null){
			response.setHeader("title",title);
		}
		response.sendRedirect(url);
	}
}