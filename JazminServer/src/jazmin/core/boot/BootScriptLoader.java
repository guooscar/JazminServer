/**
 * 
 */
package jazmin.core.boot;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.core.job.JobStore;
import jazmin.core.monitor.Monitor;
import jazmin.core.task.TaskStore;
import jazmin.core.thread.Dispatcher;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.DumpUtil;
import jazmin.util.IOUtil;

/**
 * @author yama
 * 27 Dec, 2014
 */
public class BootScriptLoader {
	static {
	    final TrustManager[] trustAllCertificates = new TrustManager[] {
	        new X509TrustManager() {
	            @Override
	            public X509Certificate[] getAcceptedIssuers() {
	                return null; // Not relevant.
	            }
	            @Override
	            public void checkClientTrusted(X509Certificate[] certs, String authType) {
	                // Do nothing. Just allow them all.
	            }
	            @Override
	            public void checkServerTrusted(X509Certificate[] certs, String authType) {
	                // Do nothing. Just allow them all.
	            }
	        }
	    };

	    try {
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCertificates, new SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	    } catch (GeneralSecurityException e) {
	        throw new ExceptionInInitializerError(e);
	    }
	}
	//
	private InputStream inputStream;
	public BootScriptLoader(InputStream input) {
		this.inputStream=input;
	}
	//
	public void load()throws Exception{
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("nashorn");
		SimpleScriptContext ssc=new SimpleScriptContext();
		//
		BootContext bc=new BootContextImpl();
		//
		ssc.setAttribute("$",bc, ScriptContext.ENGINE_SCOPE);
		ssc.setAttribute("jazmin",bc, ScriptContext.ENGINE_SCOPE);
		//
		Bindings bindings=engine.createBindings();
		bindings.put("$", bc);
		bindings.put("jazmin",bc);
		engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		//
		String importScript=
				"load('nashorn:mozilla_compat.js');"+
				"importPackage(Packages.jazmin.core);"+
				"importPackage(Packages.jazmin.core.aop);"+
				"importPackage(Packages.jazmin.core.app);"+
				"importPackage(Packages.jazmin.core.job);"+
				"importPackage(Packages.jazmin.core.monitor);"+
				"importPackage(Packages.jazmin.core.task);"+
				//drivers
				"importPackage(Packages.jazmin.driver.jdbc);"+
				"importPackage(Packages.jazmin.driver.file);"+
				"importPackage(Packages.jazmin.driver.memcached);"+
				"importPackage(Packages.jazmin.driver.mcache);"+
				"importPackage(Packages.jazmin.driver.rpc);"+
				"importPackage(Packages.jazmin.driver.lucene);"+
				"importPackage(Packages.jazmin.driver.process);"+
				"importPackage(Packages.jazmin.driver.redis);"+
				"importPackage(Packages.jazmin.driver.http);"+
				"importPackage(Packages.jazmin.driver.mail);"+
				"importPackage(Packages.jazmin.driver.mongodb);"+
				"importPackage(Packages.jazmin.driver.influxdb);"+
				
				//servers
				"importPackage(Packages.jazmin.server.console);"+
				"importPackage(Packages.jazmin.server.cdn);"+
				"importPackage(Packages.jazmin.server.file);"+
				"importPackage(Packages.jazmin.server.jmx);"+
				"importPackage(Packages.jazmin.server.msg);"+
				"importPackage(Packages.jazmin.server.im);"+
				"importPackage(Packages.jazmin.server.rtmp);"+
				"importPackage(Packages.jazmin.server.sip);"+
				"importPackage(Packages.jazmin.server.rpc);"+
				"importPackage(Packages.jazmin.server.ftp);"+
				"importPackage(Packages.jazmin.server.relay);"+
				"importPackage(Packages.jazmin.server.proxy);"+
				"importPackage(Packages.jazmin.server.webssh);"+
				"importPackage(Packages.jazmin.server.mysqlproxy);"+
				"importPackage(Packages.jazmin.server.websockify);"+
				"importPackage(Packages.jazmin.server.web);\n";
		String script=IOUtil.getContent(inputStream);
		engine.eval(importScript+script, ssc); 
	}
	//
	//
	static class BootContextImpl implements BootContext{
		private static Logger logger=LoggerFactory.get(BootContextImpl.class);
		/*stop server and show halt information*/
		@Override
		public void halt(String msg) {
			logger.fatal(msg);
			throw new RuntimeException(msg);
		}
		/*log info*/
		@Override
		public void log(String info) {
			logger.info(info);
		}
		/*set log level*/
		@Override
		public void setLogLevel(String level) {
			LoggerFactory.setLevel(level);
		}
		@Override
		public void setLogFile(String logFile,boolean immdiateFlush) {
			LoggerFactory.setFile(logFile,immdiateFlush);
		}
		@Override
		public void disableConsoleLog() {
			LoggerFactory.disableConsoleLog();
		}
		@Override
		public void addServer(Server server) {
			Jazmin.addServer(server);
		}
		@Override
		public void addDriver(Driver driver) {
			Jazmin.addDriver(driver);
		}
		@Override
		public void loadApplication(String appImage) {
			Jazmin.loadApplication(appImage);
		}
		//
		@Override
		public void include(String bootFileURI)throws Exception{
			Jazmin.bootFromURI(bootFileURI);
		}
		@Override
		public String getServerName() {
			return Jazmin.getServerName();
		}
		@Override
		public String getServerPath() {
			return Jazmin.getServerPath();
		}
		//
		@Override
		public void copyFile(String sourceURI, String destFilePath)throws Exception{
			logger.info("download file from {} to {}",sourceURI,destFilePath);
			AtomicInteger lastPercent=new AtomicInteger(-1);
			IOUtil.copyFile(sourceURI, destFilePath,(total,current)->{
				if(total<=0){
					logger.warn("total length is 0.no progress output");
					return;
				}
				float percent=((float)current/(float)total)*100;
				if(percent>=lastPercent.intValue()+10||lastPercent.intValue()<0){
					lastPercent.set((int)percent);
					logger.info("total {} current {} percent {}%",
							DumpUtil.byteCountToString(total),
							DumpUtil.byteCountToString(current),
							String.format("%.2f",percent));
				}
			});
			logger.info("file saved to {}",destFilePath);
		}
		
		//
		@Override
		public void setEnv(String k, String v) {
			Jazmin.environment.put(k, v);
		}
		//
		@Override
		public Dispatcher getDispatcher() {
			return Jazmin.dispatcher;
		}
		//
		@Override
		public JobStore getJobStore() {
			return Jazmin.jobStore;
		}
		//
		@Override
		public TaskStore getTaskStore() {
			return Jazmin.taskStore;
		}
		@Override
		public Monitor getMonitor() {
			return Jazmin.mointor;
		}
	}
}
