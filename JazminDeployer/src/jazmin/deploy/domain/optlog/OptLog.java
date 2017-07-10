package jazmin.deploy.domain.optlog;

import java.util.Date;

/**
 * 
 * @author skydu
 *
 */
public class OptLog {
	//
	public static final String OPT_TYPE_START_INSTANCE="Start Instance";
	public static final String OPT_TYPE_STOP_INSTANCE="Stop Instance";
	public static final String OPT_TYPE_EXECUTE_COMMAND="Excute Command";
	public static final String OPT_TYPE_RUN_JOB="Run Job";
	//

	public String userId;
	
	public String optType;
	
	public String ip;
	
	public String remark;

	public Date createTime;
}
