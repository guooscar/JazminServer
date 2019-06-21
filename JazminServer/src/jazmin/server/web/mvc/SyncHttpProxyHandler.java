/**
 * 
 */
package jazmin.server.web.mvc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import jazmin.core.app.AppException;
import jazmin.driver.http.HttpClientDriver;
import jazmin.driver.http.HttpRequest;
import jazmin.driver.http.HttpResponse;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.JSONUtil;

/**
 * @author yama
 *
 */
public class SyncHttpProxyHandler implements InvocationHandler {
	private Logger logger=LoggerFactory.get(SyncHttpProxyHandler.class);
	String url;
	HttpClientDriver httpDriver;
	public SyncHttpProxyHandler(String url){
		this.url=url;
		httpDriver=new HttpClientDriver();
		try {
			httpDriver.init();
		} catch (Exception e) {
			throw new AppException(e);
		}
	}
	//
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String invokeName=method.getDeclaringClass().getSimpleName()+"."+method.getName();
		logger.info(">>invoke "+invokeName);
		HttpRequest req=httpDriver.post(url+"/"+invokeName);
		if(args!=null) {
			for(int i=0;i<args.length;i++){
				if(args[i]!=null){
					req.addFormParam("arg"+i, JSONUtil.toJson(args[i]));		
				}
			}
		}
		Object ret=null;
		//
		try{
			HttpResponse resp=req.execute().get();
			if(resp.getStatusCode()!=200){
				throw new AppException("server return response code:"+resp.getStatusCode());
			}
			String respBody=resp.getResponseBody();
			String ss[]=respBody.split("\n");
			if(ss.length>0){
				if(!ss[0].isEmpty()){
					Class<?>retType=method.getReturnType();
					Type genericType =method.getGenericReturnType();
					if ( genericType instanceof ParameterizedType ) {  
						 Type[] typeArguments = ((ParameterizedType)genericType).getActualTypeArguments();  
						 if(typeArguments.length==1) {
							 if(List.class.isAssignableFrom(retType) && (typeArguments[0] instanceof Class)) {
								 ret=JSONUtil.fromJsonList(ss[0],(Class<?>) typeArguments[0]);
							 }
						 }
					 }else {
						 ret=JSONUtil.fromJson(ss[0],method.getReturnType());
					 }
				}	
			}
			if(ss.length>1){
				String ex=ss[1];
				String qq[]=ex.split(",");
				StringBuilder sb=new StringBuilder();
				for(int i=1;i<qq.length;i++){
					sb.append(qq[i]);
				}
				if(!qq[1].isEmpty()){
					throw new AppException(Integer.valueOf(qq[1]),sb.toString());
				}else{
					throw new AppException(sb.toString());
				}
			}
		}catch (Exception e) {
			logger.warn("invoke "+invokeName+":"+e.getMessage());
			if(e instanceof AppException){
				throw e;
			}else{
				throw new AppException(e);
			}	
		}finally {
			logger.info("<<invoke "+invokeName);
		}
		return ret;
	}

}
