package jazmin.server.ftp;

import java.util.concurrent.ThreadPoolExecutor;

import jazmin.core.JazminThreadFactory;

import org.apache.ftpserver.impl.DefaultFtpServerContext;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;

/**
 * 
 */
public class ExDefaultFtpServerContext extends DefaultFtpServerContext {
	private ThreadPoolExecutor executor;

	//
	@Override
	public void dispose() {
		super.dispose();
		if (executor != null) {
			executor.shutdown();

		}
	}

	//
	public synchronized ThreadPoolExecutor getThreadPoolExecutor() {
		if (executor == null) {
			int maxThreads = getConnectionConfig().getMaxThreads();
			if (maxThreads < 1) {
				int maxLogins = getConnectionConfig().getMaxLogins();
				if (maxLogins > 0) {
					maxThreads = maxLogins;
				} else {
					maxThreads = 16;
				}
			}
			executor = new OrderedThreadPoolExecutor(maxThreads);
			executor.setThreadFactory(new JazminThreadFactory("FTPWorker"));
		}
		return executor;
	}
}
