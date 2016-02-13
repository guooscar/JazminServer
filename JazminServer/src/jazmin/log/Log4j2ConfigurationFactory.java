package jazmin.log;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.zip.Deflater;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.DeleteAction;
import org.apache.logging.log4j.core.appender.rolling.action.Duration;
import org.apache.logging.log4j.core.appender.rolling.action.IfFileName;
import org.apache.logging.log4j.core.appender.rolling.action.IfLastModified;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.appender.rolling.action.PathSortByModificationTime;
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
	@SuppressWarnings("serial")
	class Log4j2Configuration extends DefaultConfiguration {
		private Appender consoleAppender;
		private Appender fileAppender;
		private String file;
		public static final String COLOR_PATTERN_LAYOUT = 
				"%highlight{[%d] [%-5level] [%logger{1}] [%t]- %msg %n}"+ 
				"{"
						+ "FATAL=red,"
						+ "ERROR=magenta, "
						+ "WARN=yellow, "
						+ "INFO=white, "
						+ "DEBUG=green, "
						+ "TRACE=blue"+ 
				"}";
		public static final String PATTERN_LAYOUT = 
				"[%d] [%-5level] [%logger{1}] [%t]- %msg%n";
		private static final String CONSOLE_LOG_NAME="JazminConsoleLog";
		//
		public Log4j2Configuration() {
			setName("jazmin-log-config");
			// MARKER
			Layout<? extends Serializable> layout = PatternLayout.createLayout(
					PATTERN_LAYOUT, 
					null,
					null,
					null,
					Charset.defaultCharset(),
					true, true, null,null);
			
			consoleAppender = ConsoleAppender.createAppender(
					layout, 
					null,
					Target.SYSTEM_OUT,
					CONSOLE_LOG_NAME,
					true,
					false);
			//remove default console logger
			getRootLogger().removeAppender("DefaultConsole-1");
			
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
			File logFilePath=new File(file);
			File patternPath=new File(logFilePath.getParent(),"${date:yyyyMM}/"
						+logFilePath.getName()+"-%d{yyyyMMdd}.log.gz");
		
			final TimeBasedTriggeringPolicy timeBasedTriggeringPolicy = 
					TimeBasedTriggeringPolicy.createPolicy("6", "true");
			//
			//
			DeleteAction deleteAction=	DeleteAction.createDeleteAction(
					logFilePath.getParent(), 
					true, 
					3, 
					false,
					PathSortByModificationTime.createSorter(true),
					new PathCondition[]{
						IfFileName.createNameCondition("*/*.log.gz",null),
						IfLastModified.createAgeCondition(Duration.parse("10d"))
					},
					null,
					this);		
			//
			final DefaultRolloverStrategy strategy = 
					DefaultRolloverStrategy.createStrategy(
							"1", 
							"1", 
							"min", 
							Deflater.BEST_SPEED+ "", 
							new Action[]{
									deleteAction
							},
							true,
							this);
			Layout<? extends Serializable> layout = PatternLayout.createLayout(
					COLOR_PATTERN_LAYOUT,
					null,
					null,
					null,
					Charset.defaultCharset(),
					true,
					false,
					null,
					null);
			fileAppender = RollingFileAppender.createAppender(
					file,
					patternPath.getPath(), 
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
