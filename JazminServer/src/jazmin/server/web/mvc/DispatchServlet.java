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

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * 29 Dec, 2014
 */
@SuppressWarnings("serial")
public class DispatchServlet extends HttpServlet{
	private static Logger logger=LoggerFactory.get(DispatchServlet.class);
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
			if(logger.isDebugEnabled()){
				logger.debug("send error code {}",ctx.errorCode);
			}
			hsp.sendError(ctx.errorCode);
			return;
		}
		if(ctx.exception!=null){
			if(logger.isDebugEnabled()){
				logger.catching(ctx.exception);
			}
			throw new ServletException(ctx.exception);
		}
		//
		if(ctx.view==null){
			logger.warn("can not find view class");
			hsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;	
		}else{
			try {
				if(logger.isDebugEnabled()){
					logger.debug("render view {}",ctx.view.getClass());
				}
				ctx.view.render(ctx);
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
