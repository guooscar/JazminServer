/**
 * 
 */
package jazmin.deploy.domain;

import java.util.Date;

/**
 * @author g2131
 *
 */
public class PackageDownloadInfo {
	public String instanceId;
	public String packageId;
	public int percent;
	public long packageSize;
	public long currentDownloadSize;
	public Date startTime;
	public String clientRemoteHost;
	public int clientRemotePort;
	
}
