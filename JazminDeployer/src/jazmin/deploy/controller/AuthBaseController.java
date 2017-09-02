/**
 * 
 */
package jazmin.deploy.controller;

import jazmin.server.web.mvc.AfterService;
import jazmin.server.web.mvc.BeforeService;
import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.JsonView;

/**
 * @author yama
 *
 */
public class AuthBaseController {
	
	@BeforeService
	public boolean beforeInvoke(Context ctx){
		if(ctx.request().session(true).getAttribute("user")!=null){
			return true;
		}
		return false;
	}
	
    @AfterService
    public void afterInvoke(Context ctx, Throwable e) {
        ctx.put("errorCode", 0);
        if (e == null) {
            return;
        }
        ctx.put("errorCode", -1);
        ctx.put("errorMessage", e.getMessage());
        ctx.clearException();
        if (ctx.view() == null) {
            ctx.view(new JsonView());
        }
    }
	
}
