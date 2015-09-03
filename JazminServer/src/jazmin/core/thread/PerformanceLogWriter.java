/**
 * 
 */
package jazmin.core.thread;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class PerformanceLogWriter {
	private static Logger logger=LoggerFactory.get(PerformanceLogWriter.class);
	//
	private String logFilePath;
	private File logFile;
	private FileWriter fileWriter;
	private Dispatcher dispatcher;
	public PerformanceLogWriter(String logFilePath,Dispatcher dispatcher) {
		this.logFilePath=logFilePath;
		this.dispatcher=dispatcher;
	}
	//
	public void start(){
		new LogWriter().start();
		logger.info("write performance log:{}",logFilePath);
	}
	//
	class LogWriter extends Thread{
		private Date lastDate;
		private SimpleDateFormat dateFormat;
		public LogWriter() {
			setName("PerformanceLogWriter");
			dateFormat=new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
			lastDate=new Date();
		}
		@Override
		public void run() {
			try {
				createLogFile();
			} catch (IOException e1) {
				logger.error("can not create performance log:"+logFilePath,e1);
				return;
			}
			while(true){
				try{
					writeLog();
				}catch(Exception e){
					logger.catching(e);
					break;
				}
			}
		}
		//
		private void createLogFile() throws IOException{
			if(logFilePath==null){
				return;
			}
			logFile = new File(logFilePath);
			if (!logFile.exists()) {
				if (!logFile.createNewFile()) {
					logger.error("can not create performance log {}",
							logFilePath);
					return;
				}
			}
			fileWriter = new FileWriter(logFile, true);
		}
		//
		private void writeLog() throws IOException{
			try {
				TimeUnit.MINUTES.sleep(1);
			} catch (InterruptedException e) {
				logger.catching(e);
			}
			PerformanceLog log=dispatcher.addPerformanceLog();
			if(logFilePath==null){
				return;
			}
			//
			if(logger.isDebugEnabled()){
				logger.debug("write performance log");
			}
			Calendar calendarLast=Calendar.getInstance();
			calendarLast.setTime(lastDate);
			Calendar now=Calendar.getInstance();
			lastDate=new Date();
			//
			if(now.get(Calendar.DAY_OF_MONTH)!=calendarLast.get(Calendar.DAY_OF_MONTH)){
				fileWriter.flush();
				fileWriter.close();
				String fileName=new SimpleDateFormat("yyyy-MM-dd").format(calendarLast.getTime());
				File destFile=new File(logFile.getAbsolutePath()+"-"+fileName);
				logFile.renameTo(destFile);
				createLogFile();
			}
			//
			fileWriter.write(String.format("%-10s %-15s %-15s "
					+ "AvgFullTime=%.2f "
					+ "AvgRunTime=%.2f "
					+ "%-20s %-20s %-20s\n",
					dateFormat.format(log.date),
					"PoolSize="+log.poolSize,
					"QueueSize="+log.queueSize,
					log.avgFullTime,
					log.avgRunTime,
					"RejectedCount="+log.rejectedCount,
					"InvokeCount="+log.invokeCount,
					"SubmitCount="+log.submitCount));
			fileWriter.flush();
		}
	}
}
