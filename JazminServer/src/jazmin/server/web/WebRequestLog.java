package jazmin.server.web;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

/**
 * 
 * @author yama
 * 29 Dec, 2014
 */
public class WebRequestLog extends AbstractLifeCycle implements RequestLog {
	private static Logger logger = LoggerFactory.get(WebRequestLog.class);

	public WebRequestLog() {
		super();
	}

	@Override
	public void log(Request req, Response rsp) {
		if (!isStarted()) {
			return;
		}
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append('[').append(req.getRemoteHost()).append(':')
					.append(req.getRemotePort()).append("] ");
			sb.append(req.getProtocol() + " ");
			sb.append("[").append(req.getMethod()).append("] ");
			long rspLen = rsp.getContentCount();
			if (rspLen >= 0) {
				sb.append(" ");
				sb.append(rspLen);
				sb.append(" ");
			} else {
				sb.append(" - ");
			}
			logger.info(sb.toString());
		}
	}
}
