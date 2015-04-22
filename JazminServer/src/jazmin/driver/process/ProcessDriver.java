/**
 * 
 */
package jazmin.driver.process;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.aop.Dispatcher;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.server.console.ConsoleServer;

/**
 * @author yama
 *
 */
public class ProcessDriver extends Driver{
	private static Logger logger=LoggerFactory.get(ProcessDriver.class);
	//
	private Map<String, ProcessInfo>processMap;
	private ScheduledFuture<?>checkStatusScheduledFuture;
	private ProcessLifecycleListener lifecycleListener;
	private Method lifecycleStartMethod=Dispatcher.getMethod(
			ProcessLifecycleListener.class,
			"processStarted", ProcessInfo.class);
	private Method lifecycleDestroyMethod=Dispatcher.getMethod(
			ProcessLifecycleListener.class,
			"processDestroyed", ProcessInfo.class);
	public ProcessDriver() {
		processMap=new ConcurrentHashMap<String, ProcessInfo>();
	}
	//
	/**
	 * @return the lifecycleListener
	 */
	public ProcessLifecycleListener getLifecycleListener() {
		return lifecycleListener;
	}

	/**
	 * @param lifecycleListener the lifecycleListener to set
	 */
	public void setLifecycleListener(ProcessLifecycleListener lifecycleListener) {
		this.lifecycleListener = lifecycleListener;
	}
	/**
	 * start process using specified id and args
	 * @param id
	 * @param args
	 * @return
	 * @throws IOException
	 */
	public synchronized ProcessInfo start(
			String id,
			String args[]) 
			throws IOException{
		return start(id, args, null,null);
	}
	
	/**
	 * start process using specified id and args
	 * @param id
	 * @param args
	 * @param homeDirectory
	 * @param env
	 * @return
	 * @throws IOException
	 */
	public synchronized ProcessInfo start(
			String id,
			String args[],
			String homeDirectory,
			Map<String,String>env) 
			throws IOException{
		if(processMap.containsKey(id)){
			throw new IllegalStateException("process :"+id+" already exists");
		}
		ProcessInfo pi=new ProcessInfo();
		pi.commands=args;
		pi.homeDirectory=homeDirectory;
		pi.env=env;
		pi.createTime=new Date();
		ProcessBuilder pb=new ProcessBuilder(args);
		if(homeDirectory!=null){
			pb.directory(new File(homeDirectory));
		}
		if(env!=null){
			pb.environment().putAll(env);
		}
		Process p=pb.start();
		pi.process=p;
		pi.id=id;
		//
		processMap.put(id,pi);
		if(lifecycleListener!=null){
			Jazmin.dispatcher.invokeInPool(pi.id,
					lifecycleListener,
					lifecycleStartMethod,
					Dispatcher.EMPTY_CALLBACK,pi);
		}
		return pi;
	}
	//
	private void checkProcessStatus(){
		for(ProcessInfo pi:processMap.values()){
			try{
				checkProcessInfo(pi);
			}catch(Exception e){
				logger.catching(e);
			}
		}
	}
	//
	private void checkProcessInfo(ProcessInfo pi){
		if(!pi.process.isAlive()){
			processMap.remove(pi.id);
			if(lifecycleListener!=null){
				Jazmin.dispatcher.invokeInPool(pi.id,
						lifecycleListener,
						lifecycleDestroyMethod,
						Dispatcher.EMPTY_CALLBACK,pi);
			}
		}
	}
	//
	public List<ProcessInfo> getProcesses(){
		return new ArrayList<ProcessInfo>(processMap.values());
	}
	//
	public ProcessInfo getProcess(String id){
		return processMap.get(id);
	}
	//--------------------------------------------------------------------------
	@Override
	public void start() throws Exception {
		checkStatusScheduledFuture=Jazmin.scheduleAtFixedRate(
				this::checkProcessStatus,0,1,TimeUnit.SECONDS);
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(new ProcessDriverCommand());
		}
	}
	//
	@Override
	public void stop() throws Exception {
		if(checkStatusScheduledFuture!=null){
			checkStatusScheduledFuture.cancel(true);
		}
	}
	public String info() {
		InfoBuilder ib= InfoBuilder.create().format("%-30s:%-30s\n")
				.print("lifecycleListener",lifecycleListener);
		return ib.toString();
	}
}
