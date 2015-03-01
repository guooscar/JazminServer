package jazmin.server.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
/**
 * 
 * @author yama
 * 27 Dec, 2014
 */
public class DefaultHandler extends AbstractHandler {
	private boolean showContexts = true;
	public DefaultHandler() {
	}
	
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (response.isCommitted() || baseRequest.isHandled()){
			return;
		}
	}

	public boolean getShowContexts() {
		return showContexts;
	}

	public void setShowContexts(boolean show) {
		showContexts = show;
	}
}
