package jazmin.log;

import org.apache.logging.log4j.Level;

/**
 * @author yama
 * @date Jun 4, 2014
 */
public class Logger{
	private org.apache.logging.log4j.Logger realLogger;
	public Logger(org.apache.logging.log4j.Logger realLogger ) {
		this.realLogger=realLogger;
	}
	public String getName(){
		return realLogger.getName();
	}
	//
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#debug(java.lang.Object, java.lang.Throwable)
	 */
	public void debug(Object arg0, Throwable arg1) {
		realLogger.debug(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#debug(java.lang.Object)
	 */
	public void debug(Object arg0) {
		realLogger.debug(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#debug(java.lang.String, java.lang.Object[])
	 */
	public void debug(String arg0, Object... arg1) {
		realLogger.debug(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#debug(java.lang.String, java.lang.Throwable)
	 */
	public void debug(String arg0, Throwable arg1) {
		realLogger.debug(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#debug(java.lang.String)
	 */
	public void debug(String arg0) {
		realLogger.debug(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#error(java.lang.Object, java.lang.Throwable)
	 */
	public void error(Object arg0, Throwable arg1) {
		realLogger.error(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#error(java.lang.Object)
	 */
	public void error(Object arg0) {
		realLogger.error(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#error(java.lang.String, java.lang.Object[])
	 */
	public void error(String arg0, Object... arg1) {
		realLogger.error(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#error(java.lang.String, java.lang.Throwable)
	 */
	public void error(String arg0, Throwable arg1) {
		realLogger.error(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#error(java.lang.String)
	 */
	public void error(String arg0) {
		realLogger.error(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#fatal(java.lang.Object, java.lang.Throwable)
	 */
	public void fatal(Object arg0, Throwable arg1) {
		realLogger.fatal(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#fatal(java.lang.Object)
	 */
	public void fatal(Object arg0) {
		realLogger.fatal(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#fatal(java.lang.String, java.lang.Object[])
	 */
	public void fatal(String arg0, Object... arg1) {
		realLogger.fatal(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#fatal(java.lang.String, java.lang.Throwable)
	 */
	public void fatal(String arg0, Throwable arg1) {
		realLogger.fatal(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#fatal(java.lang.String)
	 */
	public void fatal(String arg0) {
		realLogger.fatal(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#info(java.lang.Object, java.lang.Throwable)
	 */
	public void info(Object arg0, Throwable arg1) {
		realLogger.info(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#info(java.lang.Object)
	 */
	public void info(Object arg0) {
		realLogger.info(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#info(java.lang.String, java.lang.Object[])
	 */
	public void info(String arg0, Object... arg1) {
		realLogger.info(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#info(java.lang.String, java.lang.Throwable)
	 */
	public void info(String arg0, Throwable arg1) {
		realLogger.info(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#info(java.lang.String)
	 */
	public void info(String arg0) {
		realLogger.info(arg0);
	}
	/**
	 * @return
	 * @see org.apache.logging.log4j.Logger#isDebugEnabled()
	 */
	public boolean isDebugEnabled() {
		return realLogger.isDebugEnabled();
	}
	/**
	 * @param arg0
	 * @return
	 * @see org.apache.logging.log4j.Logger#isEnabled(org.apache.logging.log4j.Level)
	 */
	public boolean isEnabled(Level arg0) {
		return realLogger.isEnabled(arg0);
	}
	/**
	 * @return
	 * @see org.apache.logging.log4j.Logger#isErrorEnabled()
	 */
	public boolean isErrorEnabled() {
		return realLogger.isErrorEnabled();
	}
	/**
	 * @return
	 * @see org.apache.logging.log4j.Logger#isFatalEnabled()
	 */
	public boolean isFatalEnabled() {
		return realLogger.isFatalEnabled();
	}
	/**
	 * @return
	 * @see org.apache.logging.log4j.Logger#isInfoEnabled()
	 */
	public boolean isInfoEnabled() {
		return realLogger.isInfoEnabled();
	}
	/**
	 * @return
	 * @see org.apache.logging.log4j.Logger#isTraceEnabled()
	 */
	public boolean isTraceEnabled() {
		return realLogger.isTraceEnabled();
	}
	/**
	 * @return
	 * @see org.apache.logging.log4j.Logger#isWarnEnabled()
	 */
	public boolean isWarnEnabled() {
		return realLogger.isWarnEnabled();
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#trace(java.lang.Object, java.lang.Throwable)
	 */
	public void trace(Object arg0, Throwable arg1) {
		realLogger.trace(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#trace(java.lang.Object)
	 */
	public void trace(Object arg0) {
		realLogger.trace(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#trace(java.lang.String, java.lang.Object[])
	 */
	public void trace(String arg0, Object... arg1) {
		realLogger.trace(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#trace(java.lang.String, java.lang.Throwable)
	 */
	public void trace(String arg0, Throwable arg1) {
		realLogger.trace(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#trace(java.lang.String)
	 */
	public void trace(String arg0) {
		realLogger.trace(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#warn(java.lang.Object, java.lang.Throwable)
	 */
	public void warn(Object arg0, Throwable arg1) {
		realLogger.warn(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#warn(java.lang.Object)
	 */
	public void warn(Object arg0) {
		realLogger.warn(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#warn(java.lang.String, java.lang.Object[])
	 */
	public void warn(String arg0, Object... arg1) {
		realLogger.warn(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.logging.log4j.Logger#warn(java.lang.String, java.lang.Throwable)
	 */
	public void warn(String arg0, Throwable arg1) {
		realLogger.warn(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#warn(java.lang.String)
	 */
	public void warn(String arg0) {
		realLogger.warn(arg0);
	}
	/**
	 * @param arg0
	 * @see org.apache.logging.log4j.Logger#catching(java.lang.Throwable)
	 */
	public void catching(Throwable arg0) {
		realLogger.catching(arg0);
	}
	
}
