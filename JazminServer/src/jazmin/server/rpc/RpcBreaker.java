package jazmin.server.rpc;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * RPC request breaker
 * @author yama
 * 29 Aug, 2016
 * @see https://yq.aliyun.com/articles/7443
 */
public class RpcBreaker {
	private AtomicInteger invokeCount;
	private AtomicInteger timeoutCount;
	private long statTime;
	private long breakTime;
	private int invokeThreshold;
	private int resumeSeconds;
	public RpcBreaker() {
		invokeCount=new AtomicInteger();
		timeoutCount=new AtomicInteger();
		invokeThreshold=120;
		resumeSeconds=120;
		reset();
	}
	//
	
	//
	private void reset(){
		statTime=System.currentTimeMillis();
		invokeCount.set(0);
		timeoutCount.set(0);
	}
	/**
	 * @return the invokeThreshold
	 */
	public int getInvokeThreshold() {
		return invokeThreshold;
	}

	/**
	 * @param invokeThreshold the invokeThreshold to set
	 */
	public void setInvokeThreshold(int invokeThreshold) {
		this.invokeThreshold = invokeThreshold;
	}

	/**
	 * @return the resumeSeconds
	 */
	public int getResumeSeconds() {
		return resumeSeconds;
	}

	/**
	 * @param resumeSeconds the resumeSeconds to set
	 */
	public void setResumeSeconds(int resumeSeconds) {
		this.resumeSeconds = resumeSeconds;
	}

	//
	public void stat(boolean timeout){
		long now=System.currentTimeMillis();
		if(now-statTime>1000L*60){
			reset();
		}
		invokeCount.incrementAndGet();
		if(timeout){
			timeoutCount.incrementAndGet();
		}
	}
	//
	public boolean isBreak(){
		//
		long now=System.currentTimeMillis();
		if(now-breakTime<resumeSeconds*1000L){
			return true;
		}else{
			if(breakTime>0){
				breakTime=0;
				reset();
			}
		}
		if(invokeCount.get()<invokeThreshold){//two request per seconds
			return false;
		}
		//timeout count more than half invoke count
		int timeoutValue=timeoutCount.get()*2;
		int invokeVaue=invokeCount.get();
		if(timeoutValue>=invokeVaue){
			breakTime=System.currentTimeMillis();
			return true;
		}
		//
		return false;
	}
}
