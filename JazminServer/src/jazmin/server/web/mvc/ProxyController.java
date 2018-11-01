/**
 * 
 */
package jazmin.server.web.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.core.app.AppException;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.JSONUtil;
import jazmin.util.XssShieldUtil;

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
	//
	private Class<?> findProxiedClass(Object proxiedObject) {
	    Class<?> proxiedClass = proxiedObject.getClass();
	    if (proxiedObject instanceof Proxy) {
	        Class<?>[] ifaces = proxiedClass.getInterfaces();
	        if (ifaces.length == 1) {
	            proxiedClass = ifaces[0];
	        } else {
	            // We need some selection strategy here
	            // or return all of them
	            proxiedClass = ifaces[ifaces.length - 1];
	        }
	    }
	    return proxiedClass;
	}
	/**
	 * register proxy target
	 */
	public void registerProxyTarget(Object target){
		
		String targetName=target.getClass().getSimpleName();
		Class<?>targetType=target.getClass();
		if(targetName.contains("$Proxy")){
			//proxy class
			targetType=findProxiedClass(target);
			targetName=targetType.getSimpleName();
		}
		Method methods[]=targetType.getMethods();
		for(Method m:methods){
			if(Modifier.isPublic(m.getModifiers())){
				String fullName=targetName+"."+m.getName();
				if(methodMap.containsKey(fullName)){
					logger.warn(fullName+" already registered");
					continue;
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
	public Object getParameterValue(Class<?> clazz,Type type,String json){
		try {
			 if ( type instanceof ParameterizedType ) {  
				 Type[] typeArguments = ((ParameterizedType)type).getActualTypeArguments();  
				 if(typeArguments.length==1) {
					 if(List.class.isAssignableFrom(clazz) && (typeArguments[0] instanceof Class)) {
						 return JSONUtil.fromJsonList(json, (Class<?>) typeArguments[0]);
					 }else  if(Set.class.isAssignableFrom(clazz) && (typeArguments[0] instanceof Class)) {
						 return JSONUtil.fromJsonSet(json, (Class<?>) typeArguments[0]);
					 }
				 }
			 }
			return JSONUtil.fromJson(json, clazz);
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
			throw new ParameterException(-1,e.getMessage());
		}	
	}
	//
	/**
	 * invoke/Class.Method?arg1=jsonstr&arg2=jsonstr&token=token
	 * invoke proxy function
	 * 
	 * @param ctx
	 */
	@Service(id="invoke",method=HttpMethod.ALL,queryCount=3)
	public void invoke(Context ctx){
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
			Class<?>[] pTypes=info.method.getParameterTypes();
			Type[] types = info.method.getGenericParameterTypes();
			Object args[]=new Object[pTypes.length];
			for(int i=0;i<pTypes.length;i++){
				String pstr=ctx.getString("arg"+i);
				if(pstr!=null){
					args[i]=getParameterValue(pTypes[i],types[i],pstr);
				}
			}
			ret=info.method.invoke(info.target, args);
		}catch (InvocationTargetException e) {
			exception=e.getTargetException();
		}catch (Throwable e) {
			exception=e;
		}finally {
			if(exception!=null){
				logger.error("invoke failed.classAndMethod:{} ",classAndMethod);
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
				sb.append(","+ae.getCode()+","+XssShieldUtil.stripXss(ae.getMessage()));
			}else if(e instanceof ParameterException){
				ParameterException ae=(ParameterException)e;
				sb.append(","+ae.getCode()+",");
			}else{
				sb.append(",,"+XssShieldUtil.stripXss(e.getMessage())+"\n");
			}
		}
		return new PlainTextView(sb.toString());
	}
	/**
	 * create proxy class 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T>proxyClass,String url){
		if(!proxyClass.isInterface()){
			throw new IllegalArgumentException("target class must be interface.");
		}
		SyncHttpProxyHandler handler=new SyncHttpProxyHandler(url);	
		Object proxyObject=Proxy.newProxyInstance(
				proxyClass.getClassLoader(),
				new Class<?>[]{proxyClass}, 
				handler);
		return (T) proxyObject;
	}
}
