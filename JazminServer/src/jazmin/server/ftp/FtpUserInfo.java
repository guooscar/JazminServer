package jazmin.server.ftp;

/**
 * 
 * @author yama 26 Mar, 2015
 */
public class FtpUserInfo {
	public String userName;
	public String homeDirectory=".";
	public String userPassword;
	public boolean enableFlag=true;
	public boolean writePermission=true;
	public int idleTime;
	public int maxLoginNumber;
	public int maxLoginperip;
	public int uploadRate;
	public int downloadRate;
	//
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FTPUserInfo [userName=" + userName + ", homeDirectory="
				+ homeDirectory + "]";
	}
	
}
