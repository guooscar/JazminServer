/**
 * 
 */
package jazmin.server.web;

import jazmin.log.LoggerFactory;

import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.Logger;

/**
 * @author yama 28 Dec, 2014
 */
public class JettyLogger extends AbstractLogger {
	private final static jazmin.log.Logger logger = LoggerFactory.get(JettyLogger.class);
	public static boolean enable=false;
	public JettyLogger() throws Exception {
	}
	public String getName() {
		return logger.getName();
	}

	public void warn(String msg, Object... args) {
		if(enable){
			logger.warn(msg, args);
		}
	}

	public void warn(Throwable thrown) {
		if(enable){
			warn("", thrown);
		}
	}

	public void warn(String msg, Throwable thrown) {
		if(enable){
			logger.warn(msg, thrown);
		}
	}

	public void info(String msg, Object... args) {
		if(enable){
			logger.info(msg, args);
		}
	}

	public void info(Throwable thrown) {
		if(enable){
			info("", thrown);
		}
	}

	public void info(String msg, Throwable thrown) {
		if(enable){
			logger.info(msg, thrown);
		}
	}

	public void debug(String msg, Object... args) {
		if(enable){
			logger.debug(msg, args);
		}
	}

	public void debug(String msg, long arg) {
		if(enable){
			if (isDebugEnabled()){
				logger.debug(msg, new Object[] { new Long(arg) });
			}
		}
			
	}

	public void debug(Throwable thrown) {
		if(enable){
			debug("", thrown);
		}
	}

	public void debug(String msg, Throwable thrown) {
		if(enable){
			logger.debug(msg, thrown);
		}
	}

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public void setDebugEnabled(boolean enabled) {
		if(enable){
			warn("setDebugEnabled not implemented", null, null);
		}
	}

	/**
	 * Create a Child Logger of this Logger.
	 */
	protected Logger newLogger(String fullname) {
		return this;
	}

	public void ignore(Throwable ignored) {
		if(enable){
			logger.warn(ignored);
		}
	}

	@Override
	public String toString() {
		return logger.toString();
	}
}
