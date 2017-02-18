/**
 * 
 */
package jazmin.server.web.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.core.app.AppException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.JSONUtil;

/**
 * @author yama
 *
 */
public class ProxyController {
	private static Logger logger=LoggerFactory.get(ProxyController.class);
	static class InvokeInfo{
		Object target;
		Method method;
	}
	//
	protected Map<String,InvokeInfo>methodMap;
	public ProxyController() {
		methodMap=new ConcurrentHashMap<>();		
	}
	//
	static ThreadLocal<Context>contextThreadLocal=new ThreadLocal<>();
	//
	/**
	 * register proxy target
	 */
	public void registerProxyTarget(Object target){
		String targetName=target.getClass().getSimpleName();
		Method methods[]=target.getClass().getDeclaredMethods();
		for(Method m:methods){
			if(Modifier.isPublic(m.getModifiers())){
				String fullName=targetName+"."+m.getName();
				if(methodMap.containsKey(fullName)){
					throw new IllegalStateException(fullName+" already registered");
				}
				InvokeInfo info=new InvokeInfo();
				info.method=m;
				info.target=target;
				methodMap.put(fullName,info);
				if(logger.isInfoEnabled()){
					logger.info("register proxy target:"+fullName);
				}
			}
		}
	}
	//
	public Context getContext(){
		return contextThreadLocal.get();
	}
	//
	public Object getParameterValue(Class<?> type,String json){
		return JSONUtil.fromJson(json, type);
	}
	//
	/**
	 * invoke/Class.Method?arg1=jsonstr&arg2=jsonstr&token=token
	 * invoke proxy function
	 * 
	 * @param ctx
	 */
	@Service(id="invoke",method=HttpMethod.ALL,queryCount=3)
	public void Invoke(Context ctx){
		String classAndMethod=ctx.request().querys().get(2);
		InvokeInfo info=methodMap.get(classAndMethod);
		if(info==null){
			ctx.view(new ErrorView(404,"can not find "+classAndMethod));
			return;
		}
		Object ret=null;
		Throwable exception=null;
		try{
			contextThreadLocal.set(ctx);
			Class<?> pTypes[]=info.method.getParameterTypes();
			Object args[]=new Object[pTypes.length];
			for(int i=0;i<pTypes.length;i++){
				String pstr=ctx.getString("arg"+i);
				if(pstr!=null){
					args[i]=getParameterValue(pTypes[i],pstr);
				}
			}
			ret=info.method.invoke(info.target, args);
		}catch (InvocationTargetException e) {
			exception=e.getTargetException();
		}catch (Throwable e) {
			exception=e;
		}finally {
			if(exception!=null){
				logger.catching(exception);
			}
			contextThreadLocal.set(null);
			ctx.view(makeResultView(ret, exception));
		}
	}
	/*
	 * response format 2 line plain text
	 * 1.return value json string 
	 * 2.exception string if has
	 */
	private PlainTextView makeResultView(Object result,Throwable e){
		StringBuilder sb=new StringBuilder();
		if(result!=null){
			sb.append(JSONUtil.toJson(result)+"\n");
		}else{
			sb.append("\n");
		}
		if(e!=null){
			sb.append(e.getClass().getSimpleName());
			if(e instanceof AppException){
				AppException ae=(AppException)e;
				sb.append(","+ae.getCode()+","+ae.getMessage());
			}else{
				sb.append(",,"+e.getMessage()+"\n");
			}
		}
		return new PlainTextView(sb.toString());
	}
}
