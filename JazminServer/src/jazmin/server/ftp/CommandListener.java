/**
 * 
 */
package jazmin.server.ftp;


/**
 * @author yama
 * 26 Mar, 2015
 */
public interface CommandListener {

	void onConnect(FTPSession session)throws Exception;
	void onDisconnect(FTPSession session)throws Exception;
	//
	void afterCommand(FTPSession session, FTPRequest req,FTPReply reply)throws Exception;
	void beforeCommand(FTPSession session, FTPRequest req)throws Exception;
}
