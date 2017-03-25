/**
 * 
 */
package jazmin.deploy.domain;

import java.util.Date;

/**
 * @author yama
 *
 */
public class MachineJob {
	public String id;
	public String machine;
	public String robot;
	public String cron;
	public boolean root;
	public int runTimes;
	public Date lastRunTime;
	public Date nextRunTime;
	public String errorMessage;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the machine
	 */
	public String getMachine() {
		return machine;
	}
	/**
	 * @param machine the machine to set
	 */
	public void setMachine(String machine) {
		this.machine = machine;
	}
	/**
	 * @return the script
	 */
	public String getScript() {
		return robot;
	}
	/**
	 * @param script the script to set
	 */
	public void setScript(String script) {
		this.robot = script;
	}
	/**
	 * @return the cron
	 */
	public String getCron() {
		return cron;
	}
	/**
	 * @param cron the cron to set
	 */
	public void setCron(String cron) {
		this.cron = cron;
	}
	/**
	 * @return the runTimes
	 */
	public int getRunTimes() {
		return runTimes;
	}
	/**
	 * @param runTimes the runTimes to set
	 */
	public void setRunTimes(int runTimes) {
		this.runTimes = runTimes;
	}
	/**
	 * @return the lastRunTime
	 */
	public Date getLastRunTime() {
		return lastRunTime;
	}
	/**
	 * @param lastRunTime the lastRunTime to set
	 */
	public void setLastRunTime(Date lastRunTime) {
		this.lastRunTime = lastRunTime;
	}
	/**
	 * @return the root
	 */
	public boolean isRoot() {
		return root;
	}
	/**
	 * @param root the root to set
	 */
	public void setRoot(boolean root) {
		this.root = root;
	}
	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	/**
	 * @return the nextRunTime
	 */
	public Date getNextRunTime() {
		return nextRunTime;
	}
	/**
	 * @param nextRunTime the nextRunTime to set
	 */
	public void setNextRunTime(Date nextRunTime) {
		this.nextRunTime = nextRunTime;
	}
	
}
