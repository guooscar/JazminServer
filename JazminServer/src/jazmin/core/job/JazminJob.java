/**
 * 
 */
package jazmin.core.job;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;

import jazmin.core.Jazmin;


/**
 * @author yama
 * 25 Dec, 2014
 */
public class JazminJob {
	public int runTimes;
	public String id;
	public String cron;
	public Object instance;
	public Method method;
	private Date lastRunTime;
	public JazminJob() {
		runTimes=0;
	}
	//
	public Date lastRunTime(){
		return lastRunTime;
	}
	//
	public Date nextRunTime()throws Exception {
		if(lastRunTime==null){
			lastRunTime=new Date();
		}
		try {
			CronExpression ce=new CronExpression(cron);
			return ce.getNextValidTimeAfter(lastRunTime);
		} catch (ParseException e) {
			throw new Exception(e);
		}
	}

	/**
	 * @param cron the cron to set
	 */
	public void setCronExpression(String cron) {
		if(!CronExpression.isValidExpression(cron)){
			throw new IllegalArgumentException("bad cron expression:"
					+cron);
 		}
		this.cron = cron;
	}
	//
	public void run(){
		lastRunTime=new Date();
		runTimes++;
		Jazmin.dispatcher.invokeInPool("",instance, method);
	}
}
