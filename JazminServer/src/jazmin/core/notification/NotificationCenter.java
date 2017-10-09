/**
 * 
 */
package jazmin.core.notification;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jazmin.core.Jazmin;
import jazmin.core.Lifecycle;
import jazmin.core.Registerable;
import jazmin.core.thread.Dispatcher;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;

/**
 * @author yama
 *
 */
public class NotificationCenter extends Lifecycle implements Registerable{
	private static Logger logger=LoggerFactory.get(NotificationCenter.class);
	//
	List<NotificationListener> listeners;
	public NotificationCenter() {
		listeners=Collections.synchronizedList(new LinkedList<NotificationListener>());
	}
	/**
	 * register notification listener
	 * @param name
	 * @param listener
	 */
	public void register(Object object){
		
		if(isStarted()){
			throw new IllegalStateException("register before started.");
		}
		for(Method m:object.getClass().getDeclaredMethods()){
			if(!m.isAnnotationPresent(NotificationDefine.class)){
				continue;
			}
			if(!Modifier.isPublic(m.getModifiers())){
				throw new IllegalArgumentException("notification method shoule be public");
			}
			if(m.getParameterCount()!=1){
				throw new IllegalArgumentException("notification method parameter must be Notification");
			}
			if(!m.getParameters()[0].getType().equals(Notification.class)){
				throw new IllegalArgumentException("notification method parameter must be Notification");
			}
			
			NotificationDefine td= m.getAnnotation(NotificationDefine.class);
			NotificationListener l=new NotificationListener();
			l.id=m.getDeclaringClass().getSimpleName()+"."+m.getName();
			//
			for(NotificationListener ll :listeners){
				if(ll.id.equals(l.id)){
					throw new IllegalArgumentException("notification listener already registered."+ll.id);
				}
			}
			//
			l.method=m;
			l.async=td.async();
			l.event=td.event();
			l.instance=object;
			listeners.add(l);
			logger.info("register notification listener {} on event {}",l.id,l.event);
		}
	}
	/**
	 * remove notification listener
	 * @param listener
	 */
	public void remove(String id){
		logger.info("remove notification listener {} ",id);
		Iterator<NotificationListener> it=listeners.iterator();
		while(it.hasNext()){
			if(it.next().id.equals(id)){
				it.remove();
			}
		}
	}
	/**
	 * post notification event
	 * @param name
	 */
	public void post(String name){
		post(name,new HashMap<String, Object>());
	}
	/**
	 * post notification event with args
	 * @param name
	 */
	public void post(String event,Map<String,Object>args){
		//post async
		for(NotificationListener l : listeners){
			if(l.async&&l.event.equals(event)){
				fireNotification(event,l,args);
			}
		}
		//
		for(NotificationListener l : listeners){
			if(!l.async&&l.event.equals(event)){
				fireNotification(event,l,args);
			}
		}
	}
	//
	private void fireNotification(String event,NotificationListener l,Map<String,Object>args){
		Notification n=new Notification();
		n.args=args;
		n.event=event;
		if(l.async){
			Jazmin.dispatcher.invokeInPool(
					"NotificationCenter",
					l.instance, l.method, Dispatcher.EMPTY_CALLBACK,
					n);
		}else{
			Jazmin.dispatcher.invokeInCaller(
					"NotificationCenter",
					l.instance, l.method, Dispatcher.EMPTY_CALLBACK,
					n);
		}
	}
	
	//
	@Override
	public String info() {
		if(listeners.isEmpty()){
			return null;
		}
		String format="%-5s: %-50s %-20s %-10s\n";
		InfoBuilder ib=InfoBuilder.create().format(format);
		int index=1;
		ib.print("#","ID","EVENT","ASYNC");	
		for(NotificationListener l:listeners){
			ib.print(index++,
					l.id,
					l.event,
					l.async);
		};
		return ib.toString();
	}
	
}
