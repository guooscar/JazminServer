/**
 * 
 */
package jazmin.server.web.mvc;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletResponse;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * 29 Dec, 2014
 */
public class ResourceView implements View{
	private static Logger logger=LoggerFactory.get(ResourceView.class);
	//
	private String file;
	public ResourceView(String file) {
		this.file=file;
		if(!file.startsWith("/")){
			throw new IllegalArgumentException("resource path should start with /");
		}
	}
	@Override
	public void render(Context ctx) throws Exception {
		ctx.contextMap.forEach((k,v)->{ctx.request.attribute(k,v);});
		RequestDispatcher rd=ctx.request.raw().getRequestDispatcher(file);
		if(rd==null){
			logger.warn("can not find resource file:"+file);
			ctx.response.raw().sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		rd.forward(ctx.request.raw(), ctx.response.raw());
	}
}
