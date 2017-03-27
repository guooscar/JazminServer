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
		public int noOfUsers;
		public long total;
		public long max;
		public long min;
		public long average;
		public long deviation;
		public int throughtput;	//count pre minute
		public int errorCount;
		public Date startTime;
		public Date endTime;
		//
		public BenchmarkRequestStat() {
			startTime=new Date();
			endTime=new Date();
			max=0;
			min=Integer.MAX_VALUE;
		}
		//
		public void sample(long time,boolean isError){
			endTime=new Date();
			noOfSamples++;
			if(isError){
				errorCount++;
			}
			total+=time;

			if(max<time){
				max=time;
			}
			if(min>time){
				min=time;
			}
			average=total/noOfSamples;
			if(total>0){
				throughtput=(int) ((noOfSamples*60*1000L)/total);
			}
		}
	}
	//
	public interface RobotFactory{
		BenchmarkRobot create();
	}
	//
	public String id;
	public String name;
	public Date startTime;
	public Date endTime;
	public int userCount;
	public int loopCount;
	public int rampUpPeriod;//seconds
	public boolean haltOnException;
	List<UserThread>userThreads;
	//
	BenchmarkRequestStat totalStat;
	RobotFactory robotFactory;
	AtomicInteger finishCount;
	//
	Map<String, BenchmarkRequestStat>statMap;
	List<BenchmarkRequestStat>allStats;
	LogHandler logHandler;
	List<Runnable> completeHandlers;
	public boolean finished;
	
	//
	public BenchmarkRequestStat getTotalStat(){
		return totalStat;
	}
	//
	public List<BenchmarkRequestStat>getAllTotalStats(){
		return allStats;
	}
	//
	public static interface LogHandler{
		void log(String log);
	}
	//
	public BenchmarkSession() {
		completeHandlers=new ArrayList<>();
		statMap=new ConcurrentHashMap<String,BenchmarkRequestStat>();
		allStats=new ArrayList<>();
	}
	//
	public List<BenchmarkRequestStat>getAllStats(){
		return new ArrayList<>(statMap.values());
	}
	/**
	 * 
	 * @param handler
	 */
	public void setLogHandler(LogHandler handler){
		this.logHandler=handler;
	}
	//
	public void addCompleteHandler(Runnable r){
		completeHandlers.add(r);
	}
	//
	public void log(String log){
		logger.debug("["+Thread.currentThread().getName()+"]"+log);
		if(logHandler!=null){
			logHandler.log(log);
		}
	}
	//
	public void start(
			String name,
			RobotFactory robotFactory,
			int userCount,
			int loopCount,
			int rampUpPeriod){
		this.name=name;
		this.robotFactory=robotFactory;
		this.userCount=userCount;
		this.loopCount=loopCount;
		this.rampUpPeriod=rampUpPeriod;
		startTime=new Date();
		totalStat=new BenchmarkRequestStat();
		totalStat.name=name+"-Total";		
		userThreads= Collections.synchronizedList(new ArrayList<>());
		//
		int sleepMils=(rampUpPeriod*1000)/userCount;
		finishCount=new AtomicInteger(0);
		Jazmin.schedule(()->{
			for(int i=0;i<userCount;i++){
				UserThread ut=new UserThread();
				ut.robot=robotFactory.create();
				ut.setName(name+"-"+i);
				ut.idx=i;
				userThreads.add(ut);
				ut.start();
				try {
					if(sleepMils>0){
						Thread.sleep(sleepMils);
					}
				} catch (InterruptedException e) {
					logger.catching(e);
				}
			}
		}, 0, TimeUnit.SECONDS);
		Thread t=new Thread(this::addSampleLog);
		t.setName(name+"_addSampleData");
		t.start();
	}
	//
	private void addSampleLog(){
		while(finishCount.get()<userCount){
			addSampleLog0();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		}
	}
	//
	private void addSampleLog0(){
		BenchmarkRequestStat stat=new BenchmarkRequestStat();
		stat.startTime=new Date();
		stat.average=totalStat.average;
		stat.errorCount=totalStat.errorCount;
		stat.noOfSamples=totalStat.noOfSamples;
		stat.noOfUsers=totalStat.noOfUsers;
		stat.throughtput=totalStat.throughtput;
		stat.max=totalStat.max;
		stat.min=totalStat.min;
		stat.total=totalStat.total;
		allStats.add(stat);
	}
	//
	private String dump(){
		StringBuilder sb=new StringBuilder();
		String format="%-5s %-15s %-10s %-8s %-8s %-15s %-15s %-15s %-15s %-15s %-15s\n";
		sb.append(String.format(format,
				"#",
				"noOfSamples",
				"average",
				"max",
				"min",
				"deviation",
				"errorCount",
				"throughtput",
				"startTime",
				"endTime",
				"name"));
		int count=1;
		SimpleDateFormat sf=new SimpleDateFormat("MM-dd HH:mm:ss");
		for(BenchmarkRequestStat st:statMap.values()){
			sb.append(String.format(format,
					count++,
					st.noOfSamples,
					st.average,
					st.deviation,
					st.max,
					st.min,
					st.errorCount,
					st.throughtput,
					sf.format(st.startTime),
					sf.format(st.endTime),
					st.name));
		}
		sb.append("----------------------------------------------------------\n");
		//
		sb.append("Total\n");
		format="%-30s %-30s\n";
		sb.append(String.format(format,"noOfSamples",totalStat.noOfSamples));
		sb.append(String.format(format,"average",totalStat.average));
		sb.append(String.format(format,"deviation",totalStat.deviation));
		sb.append(String.format(format,"max",totalStat.max));
		sb.append(String.format(format,"min",totalStat.min));
		sb.append(String.format(format,"errorCount",totalStat.errorCount));
		sb.append(String.format(format,"throughtput",totalStat.throughtput));
		//
		sb.append("----------------------------------------------------------\n");
		//
		sb.append(String.format(format,"startTime",startTime));
		sb.append(String.format(format,"endTime",endTime));
		sb.append(String.format(format,"userCount",userCount));
		sb.append(String.format(format,"loopCount",loopCount));
		sb.append(String.format(format,"rampUpPeriod",rampUpPeriod));
		
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
		finished=true;
		addSampleLog0();
		endTime=new Date();
		log("\n"+dump());
		//
		for(Runnable r:completeHandlers){
			r.run();
		}
	}
	//
	public void sample(String name,long time,boolean isError){
		BenchmarkRequestStat stat=getStat(name);
		synchronized (stat) {
			stat.sample(time, isError);
			stat.noOfUsers=userThreads.size();
		}
		synchronized (totalStat) {
			totalStat.sample(time, isError);
			totalStat.noOfUsers=userThreads.size();
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
		BenchmarkRobot robot;
		int idx;
		//
		public void abort(){
			abort=true;
		}
		//
		private void runTest(){
			try{
				robot.start(idx);	
			}catch (Exception e) {
				log(robot.name()+"-"+e.getMessage());
				logger.catching(e);
				return;
			}
			//
			for(int i=0;i<loopCount&&!abort;i++){
				try{
					robot.loop(i);
				}catch (Exception e) {
					logger.catching(e);
					log(robot.name()+"-"+e.getMessage());
					if(haltOnException){
						break;
					}
				}
			}
			//
			try{
				robot.end();
			}catch (Exception e) {
				log(robot.name()+"-"+e.getMessage());
				logger.catching(e);
				return;
			}
		}
		//
		@Override
		public void run() {
			if(robot!=null){
				runTest();
			}else{
				log("Robot is null");
			}
			userThreads.remove(this);
			if(finishCount.incrementAndGet()>=userCount){
				finishBenchmark();
			}
		}
	}
}