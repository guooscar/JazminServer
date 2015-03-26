/**
 * 
 */
package jazmin.server.ftp;

/**
 * @author yama
 * 26 Mar, 2015
 */
public abstract class CommandAdapter implements CommandListener{

	@Override
	public void onConnect(FTPSession session) throws Exception{
		
	}

	@Override
	public void onDisconnect(FTPSession session)throws Exception {
		
	}

	@Override
	public void afterCommand(FTPSession session, FTPRequest req, FTPReply reply)throws Exception{
		
	}

	@Override
	public void beforeCommand(FTPSession session, FTPRequest req) throws Exception{
		
	}
	
}
