/**
 * 
 */
package jazmin.core.boot;

import jazmin.core.Driver;
import jazmin.core.Server;



/**
 * @author yama
 * 27 Dec, 2014
 */
public interface BootContext{
	/**
	 * log message 
	 * @param message the message to log
	 */
	void log(String message);
	/**
	 * set log level,ALL/DEBUG/INFO/WARN/ERROR/FATAL
	 * @param level the level to set 
	 */
	void logLevel(String level);
	/**
	 * set log file
	 * @param logFile the log file 
	 * @param immdiateFlush if flush to file  immediately
	 */
	void logFile(String logFile,boolean immdiateFlush);
	/**
	 * stop server and print message 
	 * @param msg the message to print
	 */
	void halt(String msg);
	/**
	 * disable console output log
	 */
	void disableConsoleLog();
	/**
	 * add server to jazmin
	 * @param server the server will be added
	 */
	void addServer(Server server);
	/**
	 * add driver to jazmin
	 * @param driver the driver will be added
	 */
	void addDriver(Driver driver);
	/**
	 * load application image 
	 * @param appImage the application image path 
	 */
	void loadApplication(String appImage);
	/**
	 * include another boot file
	 * @param bootFile the boot file will be loaded
	 * @throws Exception
	 */
	void include(String bootFile)throws Exception;
	/**
	 * return server name 
	 * @return server name
	 */
	String serverName();
	/**
	 * return server home path
	 * @return server home path
	 */
	String serverPath();
	/**
	 * copy resource from remote URI
	 * @param sourceURI the resource URI
	 * @param destFilePath local file
	 * @throws Exception
	 */
	void copyFile(String sourceURI, String destFilePath) throws Exception;
	/**
	 * set environment variable
	 * @param k the variable key
	 * @param v the variable value
	 */
	void env(String k,String v);
}
