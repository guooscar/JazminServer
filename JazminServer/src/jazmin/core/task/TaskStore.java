/**
 * 
 */
package jazmin.core.task;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.core.Jazmin;
import jazmin.core.Lifecycle;
import jazmin.misc.InfoBuilder;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class TaskStore extends Lifecycle{
	private Map<String,JazminTask>taskMap;
	public TaskStore() {
		taskMap=new ConcurrentHashMap<String, JazminTask>();
	}
	/**
	 * 
	 * @param instance
	 */
	public void registerTask(Object instance){
		if(isStarted()){
			throw new IllegalStateException("register before started.");
		}
		for(Method m:instance.getClass().getDeclaredMethods()){
			if(!m.isAnnotationPresent(TaskDefine.class)){
				continue;
			}
			if(!Modifier.isPublic(m.getModifiers())){
				throw new IllegalArgumentException("task method shoule be public");
			}
			if(m.getParameterCount()!=0){
				throw new IllegalArgumentException("task method parameter count must be 0");
			}
			TaskDefine td= m.getAnnotation(TaskDefine.class);
			JazminTask task=new JazminTask();
			task.id=m.getDeclaringClass().getSimpleName()+"."+m.getName();
			task.initialDelay=td.initialDelay();
			task.period=td.period();
			task.unit=td.unit();
			task.method=m;
			task.instance=instance;
			task.runInThreadPool=td.runInThreadPool();
			taskMap.put(task.id,task);
		}
	}
	//
	public void runTask(String id){
		if(id==null){
			throw new IllegalArgumentException("task id can not be null.");
		}
		JazminTask t=taskMap.get(id);
		if(t==null){
			throw new IllegalArgumentException("can not find task:"+id);
		}
		t.run();
	}
	//
	//
	public List<JazminTask>getTasks(){
		return new ArrayList<JazminTask>(taskMap.values());
	}
	//
	//--------------------------------------------------------------------------
	//lifecycle
	//
	@Override
	public void start() throws Exception {
		taskMap.forEach((id,task)->{
			Jazmin.scheduleAtFixedRate(task, task.initialDelay, task.period, task.unit);
		});
	}
	//
	@Override
	public String info() {
		if(taskMap.isEmpty()){
			return null;
		}
		String format="%-5s: %-50s %-10s %-10s %-10s %-10s\n";
		InfoBuilder ib=InfoBuilder.create().format(format);
		int index=1;
		ib.print("#","NAME","INITDELAY","PERIOD","TIMEUNIT","RUNTIMES");	
		for(JazminTask task:taskMap.values()){
			ib.print(index++,
					task.id,
					task.initialDelay,
					task.period,
					task.unit,
					task.runTimes);
		};
		return ib.toString();
	}
	
}
