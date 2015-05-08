/**
 * 
 */
package jazmin.server.ftp;


/**
 * @author yama
 * 26 Mar, 2015
 */
public interface CommandListener {

	void onConnect(FtpSession session)throws Exception;
	void onDisconnect(FtpSession session)throws Exception;
	//
	void afterCommand(FtpSession session, FtpRequest req,FtpReply reply)throws Exception;
	void beforeCommand(FtpSession session, FtpRequest req)throws Exception;
}
