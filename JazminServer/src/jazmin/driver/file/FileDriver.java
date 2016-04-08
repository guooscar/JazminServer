/**
 * 
 */
package jazmin.driver.file;

import jazmin.core.Driver;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.file.FileClient;

/**
 * @author yama
 *
 */
public class FileDriver extends Driver{
	private static Logger logger=LoggerFactory.get(FileDriver.class);
	FileClient client;
	public FileDriver() {
		
	}
	//
	
	//--------------------------------------------------------------------------
	@Override
	public void start() throws Exception {
		client=new FileClient();
	}
	//
	@Override
	public void stop() throws Exception {
		client.stop();
	}
	//
	public String info() {
		return null;
	}
}
