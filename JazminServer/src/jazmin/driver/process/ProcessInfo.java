/**
 * 
 */
package jazmin.driver.process;

import java.util.Date;
import java.util.Map;

/**
 * @author yama
 *
 */
public class ProcessInfo {
	String id;
	String []commands;
	Map<String,String>env;
	String homeDirectory;
	Process process;
	Date createTime;
	//
	ProcessInfo() {
	}
	
	//
	public String getId(){
		return id;
	}
	/**
	 * @return the commands
	 */
	public String[] getCommands() {
		return commands;
	}
	/**
	 * @return the env
	 */
	public Map<String, String> getEnv() {
		return env;
	}
	/**
	 * @return the homeDirectory
	 */
	public String getHomeDirectory() {
		return homeDirectory;
	}
	/**
	 * @return the process
	 */
	public Process getProcess() {
		return process;
	}
	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}
	
}
