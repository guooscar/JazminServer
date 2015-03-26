package jazmin.server.ftp;

import java.util.Date;

import org.apache.ftpserver.ftplet.FtpStatistics;

/**
 * 
 * @author yama
 * 26 Mar, 2015
 */
public class FTPStatistics {
	FtpStatistics statistics;

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getCurrentAnonymousLoginNumber()
	 */
	public int getCurrentAnonymousLoginNumber() {
		return statistics.getCurrentAnonymousLoginNumber();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getCurrentConnectionNumber()
	 */
	public int getCurrentConnectionNumber() {
		return statistics.getCurrentConnectionNumber();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getCurrentLoginNumber()
	 */
	public int getCurrentLoginNumber() {
		return statistics.getCurrentLoginNumber();
	}

	

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getStartTime()
	 */
	public Date getStartTime() {
		return statistics.getStartTime();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getTotalAnonymousLoginNumber()
	 */
	public int getTotalAnonymousLoginNumber() {
		return statistics.getTotalAnonymousLoginNumber();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getTotalConnectionNumber()
	 */
	public int getTotalConnectionNumber() {
		return statistics.getTotalConnectionNumber();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getTotalDeleteNumber()
	 */
	public int getTotalDeleteNumber() {
		return statistics.getTotalDeleteNumber();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getTotalDirectoryCreated()
	 */
	public int getTotalDirectoryCreated() {
		return statistics.getTotalDirectoryCreated();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getTotalDirectoryRemoved()
	 */
	public int getTotalDirectoryRemoved() {
		return statistics.getTotalDirectoryRemoved();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getTotalDownloadNumber()
	 */
	public int getTotalDownloadNumber() {
		return statistics.getTotalDownloadNumber();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getTotalDownloadSize()
	 */
	public long getTotalDownloadSize() {
		return statistics.getTotalDownloadSize();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getTotalFailedLoginNumber()
	 */
	public int getTotalFailedLoginNumber() {
		return statistics.getTotalFailedLoginNumber();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getTotalLoginNumber()
	 */
	public int getTotalLoginNumber() {
		return statistics.getTotalLoginNumber();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getTotalUploadNumber()
	 */
	public int getTotalUploadNumber() {
		return statistics.getTotalUploadNumber();
	}

	/**
	 * @return
	 * @see org.apache.ftpserver.ftplet.FtpStatistics#getTotalUploadSize()
	 */
	public long getTotalUploadSize() {
		return statistics.getTotalUploadSize();
	}
	
}
