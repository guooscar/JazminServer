/**
 * 
 */
package jazmin.server.web.mvc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.core.Jazmin;
import jazmin.core.aop.DispatcherCallbackAdapter;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * 29 Dec, 2014
 */
public class Dispatcher {
	private static Logger logger=LoggerFactory.get(Dispatcher.class);
	//
	private Map<String,ControllerStub>controllerMap;
	private ControllerStub indexController;
	private WebDispatchCallback webDispatchCallback;
	public Dispatcher() {
		controllerMap=new ConcurrentHashMap<String, ControllerStub>();
		webDispatchCallback=new WebDispatchCallback();
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
		logger.info("register controller:{}-{}",cc.id(),obj.getClass());
		ControllerStub cs=new ControllerStub(cc.id());
		cs.setInstance(obj);
		if(cc.index()){
			indexController=cs;
		}
		controllerMap.put(cc.id(),cs);
	}
	//
	public Context invokeService(Request request,Response response){
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
			logger.warn("can not find service:{}",request.queryURI());
			return ctx;		
		}
		//
		if(!methodStub.method.equals(request.requestMethod())){
			//404
			logger.warn("method not match url:{} stub:{} request:{}",
					request.queryURI(),
					methodStub.method,
					request.requestMethod());
			return ctx;				
		}
		//
		Jazmin.dispatcher.invokeInCaller("", 
				controllerStub.instance,
				methodStub.invokeMethod,
				webDispatchCallback,
				ctx);
		//
		return ctx;
	}
	//
	static class WebDispatchCallback extends DispatcherCallbackAdapter{
		@Override
		public void end(
				Object instance,
				Method method, 
				Object[] args,
				Object ret, Throwable e) {
			Context ctx=(Context) args[0];
			ctx.exception=e;
		}
	}
	//--------------------------------------------------------------------------
	public List<ControllerStub> controllerStubs(){
		return new ArrayList<ControllerStub>(controllerMap.values());
	}
}
