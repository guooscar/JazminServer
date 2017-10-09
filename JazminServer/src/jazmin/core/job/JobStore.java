/**
 * 
 */
package jazmin.core.job;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.core.Lifecycle;
import jazmin.core.Registerable;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class JobStore extends Lifecycle implements Registerable{
	private static Logger logger=LoggerFactory.get(JobStore.class);
	//
	private Map<String, JazminJob>jobMap;
	public JobStore() {
		jobMap=new ConcurrentHashMap<String, JazminJob>();
	}
	//
	@Override
	public void register(Object object) {
		registerJob(object);
	}
	//
	/**
	 * register new job
	 */
	public void registerJob(Object instance){
		for(Method m:instance.getClass().getDeclaredMethods()){
			if(!m.isAnnotationPresent(JobDefine.class)){
				continue;
			}
			if(!Modifier.isPublic(m.getModifiers())){
				throw new IllegalArgumentException("job method shoule be public");
			}
			if(m.getParameterCount()!=0){
				throw new IllegalArgumentException("job method parameter count must be 0");
			}
			JobDefine td= m.getAnnotation(JobDefine.class);
			JazminJob job=new JazminJob();
			job.id=m.getDeclaringClass().getSimpleName()+"."+m.getName();
			job.cron=td.cron();
			job.method=m;
			job.instance=instance;
			try {
				job.nextRunTime();
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
			if(jobMap.containsKey(job.id)){
				throw new IllegalArgumentException("job : "+job.id+" already exists");
			}
			jobMap.put(job.id,job);
		}
	}
	//
	/**
	 */
	private void checkJob(){
		jobMap.values().forEach((job)->{
			Date nextRunTime;
			try {
				nextRunTime = job.nextRunTime();
				Date now=new Date();
				if(nextRunTime!=null&&nextRunTime.before(now)){
					job.run();
				}
			} catch (Exception e) {
				logger.error(e);
			}
			
		});
	}
	//
	public void runJob(String id){
		if(id==null){
			throw new IllegalArgumentException("job id can not be null.");
		}
		JazminJob j=jobMap.get(id);
		if(j==null){
			throw new IllegalArgumentException("can not find job:"+id);
		}
		j.run();
	}
	//
	public List<JazminJob>getJobs(){
		return new ArrayList<JazminJob>(jobMap.values());
	}
	//--------------------------------------------------------------------------
	@Override
	public void start() throws Exception {
		Jazmin.scheduleAtFixedRate(this::checkJob,30,30,TimeUnit.SECONDS);
	}
	//
	@Override
	public String info() {
		if(jobMap.isEmpty()){
			return null;
		}
		String format="%-5s: %-50s %-20s %-30s\n";
		InfoBuilder ib=InfoBuilder.create().format(format);
		int i=1;
		ib.print("#","NAME","CRON","NEXT RUN");	
		for(JazminJob job:jobMap.values()){
			try {
				ib.print(i++,
						job.id,
						job.cron,
						job.nextRunTime());
			} catch (Exception e) {
				logger.catching(e);
			}
		};
		return ib.toString();
	}
}
