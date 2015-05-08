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
	public void onConnect(FtpSession session) throws Exception{
		
	}

	@Override
	public void onDisconnect(FtpSession session)throws Exception {
		
	}

	@Override
	public void afterCommand(FtpSession session, FtpRequest req, FtpReply reply)throws Exception{
		
	}

	@Override
	public void beforeCommand(FtpSession session, FtpRequest req) throws Exception{
		
	}
	
}
