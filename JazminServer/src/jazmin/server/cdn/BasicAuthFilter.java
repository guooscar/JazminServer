/**
 * 
 */
package jazmin.server.cdn;

import java.util.Base64;

/**
 * http basic authenticate 
 * http://zh.wikipedia.org/zh-cn/HTTP%E5%9F%BA%E6%9C%AC%E8%AE%A4%E8%AF%81
 * @author yama
 *
 */
public class BasicAuthFilter implements RequestFilter{
	private String user;
	private String password;
	public BasicAuthFilter(String user,String password){
		this.user=user;
		this.password=password;
	}
	//
	@Override
	public void filter(FilterContext ctx) throws Exception {
		//WWW-Authenticate: Basic realm="Secure Area"
		String auth=ctx.getRequestHeader("Authorization");
		if(auth==null){
			sendError(ctx);
			return;
		}
		auth=auth.substring(auth.indexOf(' ')+1);
		String decoded=new String(Base64.getDecoder().decode(auth),"UTF-8");
		//
		if(!decoded.equals(user+":"+password)){
			sendError(ctx);
		}
	}
	//
	private void sendError(FilterContext ctx){
		ctx.setResponseHeader("WWW-Authenticate", "Basic realm=\"Secure Area\"");
		ctx.setErrorCode(401);
	}
}
