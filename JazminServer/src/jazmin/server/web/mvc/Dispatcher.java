/**
 * 
 */
package jazmin.server.web.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jazmin.core.Jazmin;
import jazmin.core.thread.DispatcherCallbackAdapter;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * 29 Dec, 2014
 */
public class Dispatcher {
	private static Logger logger=LoggerFactory.get(Dispatcher.class);
	//
	public static class SessionObject{
		public boolean invokeSyncSession;
		public String syncInvokingMethod;
	}
	//
	private Map<String,ControllerStub>controllerMap;
	private ControllerStub indexController;
	private Map<String,SessionObject>sessionObjectMap;
	public Dispatcher() {
		controllerMap=new ConcurrentHashMap<String, ControllerStub>();
		sessionObjectMap=new ConcurrentHashMap<>();
	}
	/**
	 * 
	 * @param obj
	 */
	public void registerController(Object obj){
		Class<?>controllerClass=obj.getClass();
		Controller cc=controllerClass.getAnnotation(Controller.class);
		if(cc==null||cc.id()==null){
			throw new IllegalArgumentException("can not find Controller annotation");
		}
		if(controllerMap.containsKey(cc.id())){
			throw new IllegalArgumentException("controller :"+cc.id()+" already exists");
		}
		//
		if(logger.isDebugEnabled()){
			logger.debug("register controller:{}-{}",cc.id(),obj.getClass());
		}
		ControllerStub cs=new ControllerStub(cc.id());
		cs.setInstance(obj);
		if(cc.index()){
			indexController=cs;
		}
		controllerMap.put(cc.id(),cs);
	}
	//
	Context invokeService(Request request,Response response){
		Context ctx=new Context();
		ctx.request=request;
		ctx.response=response;
		ControllerStub controllerStub=null;
		List<String>querys=request.querys();
		if(querys.size()==0){
			controllerStub=indexController;
		}
		//
		if(querys.size()>=1){
			controllerStub=controllerMap.get(querys.get(0));
		}
		//
		if(controllerStub==null){
			//404
			logger.warn("can not find controller:{}",request.queryURI());
			ctx.errorCode=HttpServletResponse.SC_NOT_FOUND;
			return ctx;
		}
		MethodStub methodStub=null;
		//
		if(querys.size()<=1){
			methodStub=controllerStub.indexMethod;
		}
		if(querys.size()>=2){
			methodStub=controllerStub.getMethod(querys.get(1));
		}
		//
		if(methodStub==null){
			//404
			ctx.errorCode=HttpServletResponse.SC_NOT_FOUND;
			logger.warn("can not find service:{}",request.queryURI());
			return ctx;		
		}
		
		//
		if(methodStub.queryCount>0){
			if(querys.size()<methodStub.queryCount){
				logger.warn("query count mismatch:{} {}",request.queryURI(),methodStub.queryCount);		
				ctx.errorCode=HttpServletResponse.SC_BAD_REQUEST;
				return ctx;		
			}
		}
		if(!methodStub.method.equals(HttpMethod.ALL.toString())){
			//
			if(!methodStub.method.equals(request.requestMethod())){
				//404
				logger.warn("method not match url:{} stub:{} request:{}",
						request.queryURI(),
						methodStub.method,
						request.requestMethod());
				ctx.errorCode=HttpServletResponse.SC_NOT_ACCEPTABLE;
				return ctx;				
			}
		}
		if(!callBeforeMethod(controllerStub, ctx)){
			return ctx;
		}
		//
		//sync on session
		if(methodStub.syncOnSession){
			if(request.session(false)!=null){
				HttpSession session=request.session();
				SessionObject so=sessionObjectMap.get(session.getId());
				if(so==null){
					so=new SessionObject();
					so.invokeSyncSession=true;
					so.syncInvokingMethod=methodStub.toString();
					sessionObjectMap.put(session.getId(),so);
				}else{
					if(so.invokeSyncSession){
						ctx.errorCode=HttpServletResponse.SC_NOT_ACCEPTABLE;
						logger.warn("invoke sync method:{} uri:{}",
								so.syncInvokingMethod,
								request.queryURI());
						return ctx;
					}else{
						so.invokeSyncSession=true;
						so.syncInvokingMethod=methodStub.toString();
					}
				}
			}	
		}
		//
		WebDispatchCallback callback=new WebDispatchCallback();
		callback.controllerStub=controllerStub;
		Jazmin.dispatcher.invokeInCaller("", 
				controllerStub.instance,
				methodStub.invokeMethod,
				callback,
				ctx);
		return ctx;
	}
	
	//
	private static boolean callBeforeMethod(ControllerStub controllerStub,Context ctx){
		if(controllerStub.beforeMethod!=null){
			
			Throwable realException=null;
			boolean ret=false;
			try {
				ret=(boolean) controllerStub.beforeMethod.invoke(
						controllerStub.instance,ctx);
			} catch (IllegalAccessException e) {
				realException=e;
			} catch (IllegalArgumentException e) {
				realException=e;
			} catch (InvocationTargetException e) {
				realException=e.getTargetException();
			}finally{
				if(logger.isDebugEnabled()){
					logger.debug("call before method:{}#{} ret:{}",
							controllerStub.id,
							controllerStub.beforeMethod.getName(),
							ret);
				}
			}
			ctx.exception=realException;
			return ret;
		}
		return true;
	}
	//
	public void resetSessionObject(HttpSession session){
		if(session!=null){
			sessionObjectMap.remove(session.getId());
		}
	}
	//
	private  void callAfterMethod(ControllerStub controllerStub,Context ctx,Throwable e){
		resetSessionObject(ctx.request.session(false));
		//
		if(controllerStub.afterMethod!=null){
			if(logger.isDebugEnabled()){
				logger.debug("call after method:{}#{}",
						controllerStub.id,
						controllerStub.afterMethod.getName());
			}
			try {
				controllerStub.afterMethod.invoke(controllerStub.instance,ctx,e);
			} catch (InvocationTargetException ee) {
				logger.catching(ee.getTargetException());
			} catch (Exception e1) {
				logger.catching(e1);
			} 
		}
	}
	//
	class WebDispatchCallback extends DispatcherCallbackAdapter{
		ControllerStub controllerStub;
		@Override
		public void end(
				Object instance,
				Method method, 
				Object[] args,
				Object ret, Throwable e) {
			Context ctx=(Context) args[0];
			ctx.exception=e;
			callAfterMethod(controllerStub, ctx, e);
		}
	}
	//--------------------------------------------------------------------------
	public List<ControllerStub> controllerStubs(){
		return new ArrayList<ControllerStub>(controllerMap.values());
	}
}
