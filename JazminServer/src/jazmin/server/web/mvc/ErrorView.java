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
	private String message;
	public ErrorView(int code) {
		this.code=code;
	}
	public ErrorView(int code,String message) {
		this.code=code;
		this.message=message;
	}
	//
	@Override
	public void render(Context ctx) throws Exception {
		HttpServletResponse response=ctx.response.raw();
		if(message==null){
			response.sendError(code);
		}else{
			response.sendError(code, message);
		}
	}
}
