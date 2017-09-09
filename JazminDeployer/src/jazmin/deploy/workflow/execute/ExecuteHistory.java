/**
 * 
 */
package jazmin.deploy.workflow.execute;

import java.util.Date;
import java.util.TreeSet;

/**
 * @author yama
 *
 */
public class ExecuteHistory {
	public static final String STATUS_READY="ready";
	public static final String STATUS_ENTER="enter";
	public static final String STATUS_FINISHED="finished";
	//
	public String id;
	public String node;
	public TreeSet<String> fromNodes;
	public Date startTime;
	public Date endTime;
	public Throwable error;
	//
	public String status;
	//
	public ExecuteHistory() {
		fromNodes=new TreeSet<>();
	}
}
