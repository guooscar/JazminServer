/**
 * 
 */
package jazmin.server.ftp;


/**
 * @author yama
 * 26 Mar, 2015
 */
public class FtpRequest {
	 org.apache.ftpserver.ftplet.FtpRequest request;

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpRequest#getArgument()
	 */
	public String getArgument() {
		return request.getArgument();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpRequest#getCommand()
	 */
	public String getCommand() {
		return request.getCommand();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpRequest#getRequestLine()
	 */
	public String getRequestLine() {
		return request.getRequestLine();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpRequest#hasArgument()
	 */
	public boolean hasArgument() {
		return request.hasArgument();
	}
	
}
