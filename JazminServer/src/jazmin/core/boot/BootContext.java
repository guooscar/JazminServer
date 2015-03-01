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
	void log(String info);
	void logLevel(String level);
	void logFile(String logFile,boolean immdiateFlush);
	void halt(String msg);
	void disableConsoleLog();
	//
	void addServer(Server server);
	void addDriver(Driver driver);
	//
	void loadApplication(String appImage);
	void include(String rcFile)throws Exception;
	String serverName();
	String serverPath();
	void copyFile(String sourceURI, String destFilePath) throws Exception;
	//
	void env(String k,String v);
}
