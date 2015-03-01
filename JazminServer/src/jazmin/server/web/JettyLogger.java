/**
 * 
 */
package jazmin.server.web;

import jazmin.log.LoggerFactory;

import org.apache.logging.log4j.Level;
import org.eclipse.jetty.util.log.Logger;

/**
 * @author yama
 * 28 Dec, 2014
 */
public class JettyLogger implements Logger{
	/**
	 * @return
	 * @see jazmin.log.Logger#getName()
	 */
	public String getName() {
		return logger.getName();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#debug(java.lang.Object, java.lang.Throwable)
	 */
	public void debug(Object arg0, Throwable arg1) {
		logger.debug(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#debug(java.lang.Object)
	 */
	public void debug(Object arg0) {
		logger.debug(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#debug(java.lang.String, java.lang.Object[])
	 */
	public void debug(String arg0, Object... arg1) {
		logger.debug(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#debug(java.lang.String, java.lang.Throwable)
	 */
	public void debug(String arg0, Throwable arg1) {
		logger.debug(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#debug(java.lang.String)
	 */
	public void debug(String arg0) {
		logger.debug(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#error(java.lang.Object, java.lang.Throwable)
	 */
	public void error(Object arg0, Throwable arg1) {
		logger.error(arg0, arg1);
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return logger.hashCode();
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#error(java.lang.Object)
	 */
	public void error(Object arg0) {
		logger.error(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#error(java.lang.String, java.lang.Object[])
	 */
	public void error(String arg0, Object... arg1) {
		logger.error(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#error(java.lang.String, java.lang.Throwable)
	 */
	public void error(String arg0, Throwable arg1) {
		logger.error(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#error(java.lang.String)
	 */
	public void error(String arg0) {
		logger.error(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#fatal(java.lang.Object, java.lang.Throwable)
	 */
	public void fatal(Object arg0, Throwable arg1) {
		logger.fatal(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#fatal(java.lang.Object)
	 */
	public void fatal(Object arg0) {
		logger.fatal(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#fatal(java.lang.String, java.lang.Object[])
	 */
	public void fatal(String arg0, Object... arg1) {
		logger.fatal(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#fatal(java.lang.String, java.lang.Throwable)
	 */
	public void fatal(String arg0, Throwable arg1) {
		logger.fatal(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#fatal(java.lang.String)
	 */
	public void fatal(String arg0) {
		logger.fatal(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#info(java.lang.Object, java.lang.Throwable)
	 */
	public void info(Object arg0, Throwable arg1) {
		logger.info(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#info(java.lang.Object)
	 */
	public void info(Object arg0) {
		logger.info(arg0);
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return logger.equals(obj);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#info(java.lang.String, java.lang.Object[])
	 */
	public void info(String arg0, Object... arg1) {
		logger.info(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#info(java.lang.String, java.lang.Throwable)
	 */
	public void info(String arg0, Throwable arg1) {
		logger.info(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#info(java.lang.String)
	 */
	public void info(String arg0) {
		logger.info(arg0);
	}

	/**
	 * @return
	 * @see jazmin.log.Logger#isDebugEnabled()
	 */
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	/**
	 * @param arg0
	 * @return
	 * @see jazmin.log.Logger#isEnabled(org.apache.logging.log4j.Level)
	 */
	public boolean isEnabled(Level arg0) {
		return logger.isEnabled(arg0);
	}

	/**
	 * @return
	 * @see jazmin.log.Logger#isErrorEnabled()
	 */
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	/**
	 * @return
	 * @see jazmin.log.Logger#isFatalEnabled()
	 */
	public boolean isFatalEnabled() {
		return logger.isFatalEnabled();
	}

	/**
	 * @return
	 * @see jazmin.log.Logger#isInfoEnabled()
	 */
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	/**
	 * @return
	 * @see jazmin.log.Logger#isTraceEnabled()
	 */
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	/**
	 * @return
	 * @see jazmin.log.Logger#isWarnEnabled()
	 */
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#trace(java.lang.Object, java.lang.Throwable)
	 */
	public void trace(Object arg0, Throwable arg1) {
		logger.trace(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#trace(java.lang.Object)
	 */
	public void trace(Object arg0) {
		logger.trace(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#trace(java.lang.String, java.lang.Object[])
	 */
	public void trace(String arg0, Object... arg1) {
		logger.trace(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#trace(java.lang.String, java.lang.Throwable)
	 */
	public void trace(String arg0, Throwable arg1) {
		logger.trace(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#trace(java.lang.String)
	 */
	public void trace(String arg0) {
		logger.trace(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#warn(java.lang.Object, java.lang.Throwable)
	 */
	public void warn(Object arg0, Throwable arg1) {
		logger.warn(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#warn(java.lang.Object)
	 */
	public void warn(Object arg0) {
		logger.warn(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#warn(java.lang.String, java.lang.Object[])
	 */
	public void warn(String arg0, Object... arg1) {
		logger.warn(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see jazmin.log.Logger#warn(java.lang.String, java.lang.Throwable)
	 */
	public void warn(String arg0, Throwable arg1) {
		logger.warn(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#warn(java.lang.String)
	 */
	public void warn(String arg0) {
		logger.warn(arg0);
	}

	/**
	 * @param arg0
	 * @see jazmin.log.Logger#catching(java.lang.Throwable)
	 */
	public void catching(Throwable arg0) {
		logger.catching(arg0);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return logger.toString();
	}

	private static jazmin.log.Logger logger=LoggerFactory.get(JettyLogger.class);

	@Override
	public void debug(Throwable arg0) {
		logger.debug(arg0);
	}

	@Override
	public void debug(String arg0, long arg1) {
		logger.debug(arg0,arg1);
	}

	@Override
	public Logger getLogger(String arg0) {
		return null;
	}

	@Override
	public void ignore(Throwable arg0) {
		
	}

	@Override
	public void info(Throwable arg0) {
		logger.info(arg0);
	}

	@Override
	public void warn(Throwable arg0) {
		logger.warn(arg0);
	}

	@Override
	public void setDebugEnabled(boolean arg0) {
		
	}
	
}
