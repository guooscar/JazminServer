package jazmin.log;

import java.io.Serializable;
import java.net.URI;
import java.util.zip.Deflater;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * 
 * @author yama
 * @date Jun 4, 2014
 */
@Plugin(category = "ConfigurationFactory", name = "Log4j2ConfigurationFactory")
@Order(0)
public class Log4j2ConfigurationFactory extends ConfigurationFactory {
	//
	private Log4j2Configuration configuration;
    public Log4j2ConfigurationFactory(){
    	configuration=new Log4j2Configuration();	
    }
	//
	@Override
	protected String[] getSupportedTypes() {
		return null;
	}
	public Log4j2Configuration getConfiguration(){
		return configuration;
	}
	//
	@Override
	public Configuration getConfiguration(ConfigurationSource source) {
		return configuration;
	}

	@Override
	public Configuration getConfiguration(String name, URI configLocation) {
		return configuration;
	}
	//
	/**
	 * 
	 * @author yama
	 * 22 Dec, 2014
	 */
	class Log4j2Configuration extends DefaultConfiguration {
		private Appender consoleAppender;
		private Appender fileAppender;
		private String file;
		public static final String COLOR_PATTERN_LAYOUT = 
				"%highlight{[%d] [%t] [%-5level] [%logger{1}]- %msg %n}"+ 
				"{"
						+ "FATAL=red,"
						+ "ERROR=magenta, "
						+ "WARN=yellow, "
						+ "INFO=white, "
						+ "DEBUG=green, "
						+ "TRACE=blue"+ 
				"}";
		//public static final String PATTERN_LAYOUT = 
		//		"[%d] [%t] [%-5level] [%logger{1}]- %msg%n";
		public static final String LOG_FILE_NAME_PATTERN = ".%d{yyyy-MM-dd}";
		private static final String CONSOLE_LOG_NAME="JazminConsoleLog";
		//
		public Log4j2Configuration() {
			setName("jazmin-log-config");
			removeAppender("Console");
			// MARKER
			Layout<? extends Serializable> layout = PatternLayout.createLayout(
					COLOR_PATTERN_LAYOUT, null,null, null, true, true, null,null);
			
			consoleAppender = ConsoleAppender.createAppender(layout, null,
					"SYSTEM_OUT", CONSOLE_LOG_NAME, null, "false");
			//remove default console logger
			getRootLogger().removeAppender("Console");
			//
			addAppender(consoleAppender);
			getRootLogger().addAppender(consoleAppender, Level.ALL, null);
			getRootLogger().setLevel(Level.ALL);
			isShutdownHookEnabled=false;
		}
		//
		public void disableConsoleOutput(){
			getRootLogger().removeAppender(CONSOLE_LOG_NAME);
			doConfigure();
		}
		//
		public void setLogLevel(Level level) {
			getRootLogger().setLevel(level);
			LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
			Configuration conf = ctx.getConfiguration();
			ctx.updateLoggers(conf);
		}
		/**
		 * 
		 * @param file
		 */
		public void setFile(String file){
			setFile(file,false);
		}
		//
		public String getFile(){
			return file;
		}
		//
		public void setFile(String file,boolean immediateFlush){
			this.file=file;
			final TimeBasedTriggeringPolicy timeBasedTriggeringPolicy = 
					TimeBasedTriggeringPolicy.createPolicy("1", "true");
			final DefaultRolloverStrategy strategy = 
					DefaultRolloverStrategy.createStrategy(
							"7", 
							"1", 
							null, 
							Deflater.DEFAULT_COMPRESSION+ "", 
							this);
			Layout<? extends Serializable> layout = PatternLayout.createLayout(
					COLOR_PATTERN_LAYOUT, null,null, null, true, true, null,null);

			fileAppender = RollingFileAppender.createAppender(
					file,
					file+LOG_FILE_NAME_PATTERN, 
					"true", 
					file, 
					"true",
					null,
					new Boolean(immediateFlush).toString(),
					timeBasedTriggeringPolicy,
					strategy, 
					layout, 
					null,null, null, null, null);
			addAppender(fileAppender);
			getRootLogger().addAppender(fileAppender, Level.ALL, null);
			doConfigure();
		}
		//
		public void stop(){
			if(consoleAppender!=null){
				consoleAppender.stop();
			}
			if(fileAppender!=null){
				fileAppender.stop();
			}
		}
	}
}
