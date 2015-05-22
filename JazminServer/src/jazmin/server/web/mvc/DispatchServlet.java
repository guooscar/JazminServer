/**
 * 
 */
package jazmin.server.web.mvc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yama
 * 29 Dec, 2014
 */
public class DispatchServlet extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//
	public static Dispatcher dispatcher=new Dispatcher();
	//
	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		HttpServletRequest hsr=(HttpServletRequest) req;
		HttpServletResponse hsp=(HttpServletResponse) res;
		Request request=new Request(hsr);
		Response response=new Response(hsp);
		//
		Context ctx=dispatcher.invokeService(request, response);
		//
		if(ctx.errorCode!=0){
			//404
			hsp.sendError(ctx.errorCode);
			return;
		}
		if(ctx.exception!=null){
			hsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		//
		if(ctx.view==null){
			hsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;	
		}else{
			try {
				ctx.view.render(ctx);
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
