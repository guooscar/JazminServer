/**
 * 
 */
package jazmin.misc;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author yama
 * 28 Dec, 2014
 */
public class NetworkTrafficStat {
	public LongAdder inBoundBytes;
	public LongAdder outBoundBytes;
	public NetworkTrafficStat() {
		inBoundBytes=new LongAdder();
		outBoundBytes=new LongAdder();
	}
	//
	public void inBound(int length){
		inBoundBytes.add(length);
	}
	//
	public void outBound(int length){
		outBoundBytes.add(length);
	}
}
