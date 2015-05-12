/**
 * 
 */
package jazmin.misc;

/**
 * @author yama
 *
 */
public class RateLimiter {
	private int frequencyCounter;
	private long frequencyTime;
	private int maxRequestCountPerSecond;
	//
	public RateLimiter() {
		maxRequestCountPerSecond=10;
		reset();
	}
	//
	
	//
	public boolean accessAndTest(){
		if(maxRequestCountPerSecond<=0){
			return false;
		}
		int maxRequestCount=maxRequestCountPerSecond*10;
		if(maxRequestCount<=0){
			return false;
		}
		frequencyCounter++;
		final int TEN_SECONDS=10*1000;
		//sample every 10 second
		long now=System.currentTimeMillis();
		if((now-frequencyTime)>TEN_SECONDS){
			reset();
		}else{
			if(frequencyCounter>maxRequestCount){
				return true;
			}
		}
		return false;
	}
	/**
	 * @return the maxRequestCountPerSecond
	 */
	public int getMaxRequestCountPerSecond() {
		return maxRequestCountPerSecond;
	}

	/**
	 * @param maxRequestCountPerSecond the maxRequestCountPerSecond to set
	 */
	public void setMaxRequestCountPerSecond(int maxRequestCountPerSecond) {
		this.maxRequestCountPerSecond = maxRequestCountPerSecond;
	}

	//
	void reset(){
		frequencyTime=System.currentTimeMillis();
		frequencyCounter=0;
	}
}
