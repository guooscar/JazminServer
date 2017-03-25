/**
 * 
 */
package jazmin.deploy.manager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.core.Jazmin;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class BenchmarkSession {
	private static Logger logger=LoggerFactory.get(BenchmarkSession.class);
	//
	public static class BenchmarkRequestStat{
		public String name;
		public int noOfSamples;
		public int total;
		public int average;
		public int deviation;
		public int throughtput;	
		public int errorCount;
		public Date startTime;
		public BenchmarkRequestStat() {
			startTime=new Date();
		}
		//
		public void sample(long time,boolean isError){
			noOfSamples++;
			if(isError){
				errorCount++;
			}
			total+=time;
			average=total/noOfSamples;
			int seconds=(int) ((System.currentTimeMillis()-startTime.getTime())/1000L);
			if(seconds>0){
				throughtput=noOfSamples/seconds;
			}
		}
	}
	//
	public Date startTime;
	public Date endTime;
	public int userCount;
	public int loopCount;
	public int rampUpPeriod;//seconds
	public boolean haltOnException;
	List<UserThread>userThreads;
	//
	BenchmarkRequestStat totalStat;
	BenchmarkRobot robot;
	AtomicInteger finishCount;
	//
	Map<String, BenchmarkRequestStat>statMap;
	List<BenchmarkRequestStat>allStats;
	public BenchmarkSession() {
		statMap=new ConcurrentHashMap<String,BenchmarkRequestStat>();
		allStats=new ArrayList<>();
	}
	//
	public void log(String log){
		logger.debug(log);
	}
	//
	public void start(
			BenchmarkRobot robot,
			int userCount,
			int loopCount,
			int rampUpPeriod){
		this.robot=robot;
		this.userCount=userCount;
		this.loopCount=loopCount;
		this.rampUpPeriod=rampUpPeriod;
		startTime=new Date();
		totalStat=new BenchmarkRequestStat();
		totalStat.name=robot.name()+"-Total";		
		userThreads= Collections.synchronizedList(new ArrayList<>());
		//
		int sleepMils=(rampUpPeriod*1000)/userCount;
		finishCount=new AtomicInteger(0);
		//
		for(int i=0;i<userCount;i++){
			UserThread ut=new UserThread();
			ut.setName(robot.name()+"-"+i);
			userThreads.add(ut);
			ut.start();
			try {
				Thread.sleep(sleepMils);
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		}
		//
		Jazmin.scheduleAtFixedRate(()->{
			BenchmarkRequestStat stat=new BenchmarkRequestStat();
			stat.startTime=new Date();
			stat.average=totalStat.average;
			stat.deviation=totalStat.deviation;
			stat.errorCount=totalStat.errorCount;
			stat.noOfSamples=totalStat.noOfSamples;
			stat.throughtput=totalStat.throughtput;
			stat.total=totalStat.total;
			allStats.add(stat);
		},1, 1, TimeUnit.SECONDS);
	}
	//
	private String dump(){
		StringBuilder sb=new StringBuilder();
		String format="%-5s %-15s %-15s %-15s %-15s %-15s %-15s %-15s\n";
		sb.append(String.format(format,
				"#",
				"noOfSamples",
				"average",
				"deviation",
				"errorCount",
				"throughtput",
				"startTime",
				"name"));
		int count=1;
		SimpleDateFormat sf=new SimpleDateFormat("MM-dd HH:mm:ss");
		for(BenchmarkRequestStat st:statMap.values()){
			sb.append(String.format(format,
					count++,
					st.noOfSamples,
					st.average,
					st.deviation,
					st.errorCount,
					st.throughtput,
					sf.format(st.startTime),
					st.name));
		}
		//
		sb.append("Total\n");
		format="%-30s %-30s\n";
		sb.append(String.format(format,"noOfSamples",totalStat.noOfSamples));
		sb.append(String.format(format,"average",totalStat.average));
		sb.append(String.format(format,"deviation",totalStat.deviation));
		sb.append(String.format(format,"errorCount",totalStat.errorCount));
		sb.append(String.format(format,"throughtput",totalStat.throughtput));
		sb.append(String.format(format,"startTime",sf.format(totalStat.startTime)));
		//
		return sb.toString();
	}
	//
	public void stop(){
		for(UserThread ut:userThreads){
			ut.abort();
		}
	}
	//
	private void finishBenchmark(){
		endTime=new Date();
		log("\n"+dump());
	}
	//
	public void sample(String name,long time,boolean isError){
		BenchmarkRequestStat stat=getStat(name);
		synchronized (stat) {
			stat.sample(time, isError);
		}
		synchronized (totalStat) {
			totalStat.sample(time, isError);
		}
	}
	//
	public BenchmarkRequestStat getStat(String name){
		BenchmarkRequestStat stat=statMap.get(name);
		if(stat==null){
			stat=new BenchmarkRequestStat();
			stat.name=name;
			statMap.put(name, stat);
		}
		return stat;
	}
	//
	class UserThread extends Thread{
		boolean abort;
		//
		public void abort(){
			abort=true;
		}
		//
		@Override
		public void run() {
			try{
				robot.start();	
			}catch (Exception e) {
				logger.catching(e);
				return;
			}
			//
			for(int i=0;i<loopCount&&!abort;i++){
				try{
					robot.loop();
				}catch (Exception e) {
					logger.catching(e);
					if(haltOnException){
						break;
					}
				}
			}
			//
			try{
				robot.end();
			}catch (Exception e) {
				logger.catching(e);
				return;
			}
			//
			userThreads.remove(this);
			if(finishCount.incrementAndGet()==userCount){
				finishBenchmark();
			}
		}
	}
}
