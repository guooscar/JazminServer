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
	public LongAdder minTime=new LongAdder();
	public LongAdder maxTime=new LongAdder();
	public LongAdder totalTime=new LongAdder();
	//
	public InvokeStat() {
		minTime.add(Integer.MAX_VALUE);
	}
	//
	public void invoke(boolean error,int time){
		invokeCount.increment();
		if(error){
			errorCount.increment();
		}
		if(minTime.intValue()>time){
			minTime.reset();
			minTime.add(time);
		}
		if(maxTime.intValue()<time){
			maxTime.reset();
			maxTime.add(time);
		}
		totalTime.add(time);
	}
	//
	public int avgTime(){
		if(invokeCount.intValue()==0){
			return 0;
		}
		return totalTime.intValue()/invokeCount.intValue();
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
