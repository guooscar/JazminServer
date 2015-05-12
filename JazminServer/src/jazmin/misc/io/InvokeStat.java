/**
 * 
 */
package jazmin.misc.io;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author yama
 * 27 Dec, 2014
 */
public class InvokeStat implements Comparable<InvokeStat>{
	public String name;
	public LongAdder invokeCount=new LongAdder();
	public LongAdder errorCount=new LongAdder();
	
	public LongAdder minRunTime=new LongAdder();
	public LongAdder maxRunTime=new LongAdder();
	public LongAdder totalRunTime=new LongAdder();
	//
	public LongAdder minFullTime=new LongAdder();
	public LongAdder maxFullTime=new LongAdder();
	public LongAdder totalFullTime=new LongAdder();
	//
	
	//
	public InvokeStat() {
		minRunTime.add(Integer.MAX_VALUE);
		minFullTime.add(Integer.MAX_VALUE);
	}
	//
	public void invoke(boolean error,int runTime,int fullTime){
		invokeCount.increment();
		if(error){
			errorCount.increment();
		}
		if(minRunTime.intValue()>runTime){
			minRunTime.reset();
			minRunTime.add(runTime);
		}
		if(maxRunTime.intValue()<runTime){
			maxRunTime.reset();
			maxRunTime.add(runTime);
		}
		totalRunTime.add(runTime);
		if(totalRunTime.longValue()<0){
			totalRunTime.reset();
		}
		//
		if(minFullTime.intValue()>fullTime){
			minFullTime.reset();
			minFullTime.add(fullTime);
		}
		if(maxFullTime.intValue()<fullTime){
			maxFullTime.reset();
			maxFullTime.add(fullTime);
		}
		totalFullTime.add(fullTime);
		if(totalFullTime.longValue()<0){
			totalFullTime.reset();
		}
	}
	//
	public int avgRunTime(){
		if(invokeCount.intValue()==0){
			return 0;
		}
		return totalRunTime.intValue()/invokeCount.intValue();
	}
	//
	//
	public int avgFullTime(){
		if(invokeCount.intValue()==0){
			return 0;
		}
		return totalFullTime.intValue()/invokeCount.intValue();
	}
	//
	/**
	 * @param anotherString
	 * @return
	 * @see java.lang.String#compareTo(java.lang.String)
	 */
	public int compareTo(InvokeStat ms) {
		return name.compareTo(ms.name);
	}
	
}
