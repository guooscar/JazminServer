/**
 * 
 */
package jazmin.driver.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.aop.Dispatcher;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.misc.InvokeStat;
import jazmin.server.console.ConsoleServer;
import jazmin.server.rpc.RPCClient;
import jazmin.server.rpc.RPCMessage;
import jazmin.server.rpc.RPCSession;
import jazmin.server.rpc.RemoteService;

/**
 * @author yama
 * 25 Dec, 2014
 */
public class JazminRPCDriver extends Driver{
	private static Logger logger=LoggerFactory.get(JazminRPCDriver.class);
	//
	private RPCClient client;
	private Map<String,List<RPCSession>>sessionMap;
	private Map<String,List<RemoteServerInfo>>serverInfoMap;
	private Map<String,Object>syncProxyMap;
	private Map<String,InvocationHandler>syncHandlerMap;
	private Map<String,Object>asyncProxyMap;
	private Map<String,InvocationHandler>asyncHandlerMap;
	private Map<String,Set<String>>topicMap;
	private PushCallback pushCallback;
	private Method pushCallbackMethod;
	private String principal;
	private boolean disablePushMessage;
	private Map<String,InvokeStat>methodStats;
	private LongAdder totalInvokeCount;
	//
	public static class RemoteServerInfo{
		public String remoteHostAddress;
		public int remotePort;
		public String cluster;
		public String name;
	}
	//
	public JazminRPCDriver(){
		sessionMap=new ConcurrentHashMap<String, List<RPCSession>>();
		serverInfoMap=new ConcurrentHashMap<String, List<RemoteServerInfo>>();
		syncProxyMap=new ConcurrentHashMap<String, Object>();
		syncHandlerMap=new ConcurrentHashMap<String, InvocationHandler>();
		asyncProxyMap=new ConcurrentHashMap<String, Object>();
		asyncHandlerMap=new ConcurrentHashMap<String, InvocationHandler>();
		topicMap=new ConcurrentHashMap<String, Set<String>>();
		methodStats=new ConcurrentHashMap<String, InvokeStat>();
		totalInvokeCount=new LongAdder();
		pushCallbackMethod=PushCallback.class.getMethods()[0];
	}
	//
	public void principal(String p){
		this.principal=p;
	}
	//
	public String principal(){
		return this.principal;
	}
	//
	public void disablePushMessage(boolean dpm){
		disablePushMessage=dpm;
	}
	//
	public boolean disablePushMessage(){
		return disablePushMessage;
	}
	//
	/**
	 * 
	 */
	public void addRemoteServer(String cluster,String name,String host,int port){
		if(started()){
			throw new IllegalStateException("register before started.");
		}
		RemoteServerInfo si=new RemoteServerInfo();
		si.cluster=cluster;
		si.name=name;
		si.remoteHostAddress=host;
		si.remotePort=port;
		List<RemoteServerInfo>serverList=serverInfoMap.get(cluster);
		if(serverList==null){
			serverList=new ArrayList<>();
			serverInfoMap.put(cluster, serverList);
		}
		serverList.add(si);
	}
	//
	private void connectToRemoteServer(RemoteServerInfo serverInfo){
		RPCSession session=new RPCSession();
		session.remoteHostAddress(serverInfo.remoteHostAddress);
		session.remotePort(serverInfo.remotePort);
		session.cluster(serverInfo.cluster);
		session.principal(principal);
		session.disablePushMessage(disablePushMessage);
		Set<String>topics=topicMap.get(serverInfo.cluster);
		if(topics!=null){
			topics.forEach((topic)->session.subscribe(topic));
		}
		client.connect(session);	
		List<RPCSession>sessionList=sessionMap.get(serverInfo.cluster);
		if(sessionList==null){
			sessionList=(new ArrayList<RPCSession>());
			sessionMap.put(serverInfo.cluster,sessionList);
		}
		logger.info("create rpc session:"+session);
		sessionList.add(session);
	}
	/**
	 * @param pushCallback the pushCallback to set
	 */
	public void pushCallback(PushCallback pushCallback) {
		this.pushCallback = pushCallback;
	}
	//
	public PushCallback pushCallback(){
		return pushCallback;
	}
	//
	public List<RPCSession>sessions(){
		List<RPCSession>ss=new ArrayList<RPCSession>();
		sessionMap.forEach((cluster,sessionList)->ss.addAll(sessionList));
		return ss;
	}
	//
	public List<String>syncProxys(){
		return new ArrayList<String>(syncProxyMap.keySet());
	}
	//
	public List<String>asyncProxys(){
		return new ArrayList<String>(asyncProxyMap.keySet());
	}
	//
	public List<RemoteServerInfo>remoteServers(){
		List<RemoteServerInfo>ss=new ArrayList<RemoteServerInfo>();
		serverInfoMap.forEach((cluster,serverList)->ss.addAll(serverList));
		return ss;
	}
	//--------------------------------------------------------------------------
	private void checkSessionActiveStatus(){
		sessionMap.forEach((cluster,sessionList)->{
			sessionList.forEach(session->{
				synchronized (session) {
					if(!session.isConnected()){
						if(logger.isWarnEnabled()){
							logger.warn("rpc session "+session+" deactived");
						}
						client.connect(session);
					}
				}
			});
			
		});
	}
	//--------------------------------------------------------------------------
	//
	@SuppressWarnings("unchecked")
	private <T> T create0(
			Class<T>clazz,
			String clusterName,
			Map<String,Object>proxyMap,
			Map<String,InvocationHandler>handlerMap,
			boolean isAsync){
		if(!clazz.isInterface()){
			throw new IllegalArgumentException("target class must be interface.");
		}
		Object proxyObject=proxyMap.get(clusterName+"."+clazz.getName());
		if(proxyObject!=null){
			return (T) proxyObject;
		}
		//
		List<RPCSession>sessions=sessionMap.get(clusterName);
		if(sessions==null){
			throw new IllegalArgumentException("can not find cluster with name:"
					+clusterName);
		}
		//
		InvocationHandler handler=handlerMap.get(clusterName);
		if(handler==null){
			if(isAsync){
				handler=new AsyncRPCInvocationHandler(this,client,sessions);	
			}else{
				handler=new SyncRPCInvocationHandler(this,client,sessions);			
			}
			handlerMap.put(clusterName, handler);
		}
		proxyObject=Proxy.newProxyInstance(
				clazz.getClassLoader(),
				new Class<?>[]{clazz}, 
				handler);
		proxyMap.put(clusterName+"."+clazz.getName(),proxyObject);
		return (T) proxyObject;
	}
	//
	/**
	 * create remote class proxy
	 */
	public <T> T create(Class<T>clazz,String clusterName){
		if(!RemoteService.class.isAssignableFrom(clazz)){
			throw new IllegalArgumentException(clazz+" must be subclass of "+
					RemoteService.class);
		}
		return create0(clazz, clusterName, syncProxyMap,syncHandlerMap,false);
	}
	//
	private void checkAsyncClassSignature(Class<?>c){
		if(!c.getSimpleName().endsWith("Async")){
			throw new IllegalArgumentException("class name:"+
						c.getSimpleName()+" end with Async");
		}
		for(Method m:c.getDeclaredMethods()){
			if(!m.getReturnType().equals(void.class)){
				throw new IllegalArgumentException("async method:"+m.getName()
						+" should be void.");
			}
			Class<?>pTypes[]=m.getParameterTypes();
			if(pTypes.length==0||!pTypes[pTypes.length-1].equals(AsyncCallback.class)){
				throw new IllegalArgumentException("async method:"+m.getName()
						+" last parameter type should be "+AsyncCallback.class);
			}
		}
	}
	//
	/**
	 * create async remote class proxy
	 */
	public <T> T createAsync(Class<T>clazz,String clusterName){
		checkAsyncClassSignature(clazz);
		return create0(clazz, clusterName, asyncProxyMap,asyncHandlerMap,true);
	}
	//--------------------------------------------------------------------------
	//pub sub
	//
	public void subscribe(String cluster,String topic){
		Set<String>topics=topicMap.get(cluster);
		if(topics==null){
			topics=new TreeSet<String>();
			topicMap.put(cluster, topics);
		}
		topics.add(topic);	
	}
	//--------------------------------------------------------------------------
	private void handlePushMessage(RPCSession session,RPCMessage message){
		if(pushCallback==null){
			return;
		}
		String cluster=session.cluster();
		String serviceId=(String)message.payloads[0];
		Object payload=message.payloads[1];
		Jazmin.dispatcher.invokeInPool(cluster+"."+serviceId,
				pushCallback,
				pushCallbackMethod,
				Dispatcher.EMPTY_CALLBACK,
				cluster,serviceId,payload);
	}
	//
	//
	void statMethod(Method m,Throwable e,int time){
		String name=m.getDeclaringClass().getSimpleName()+"."+m.getName();
		InvokeStat ms=methodStats.get(name);
		if(ms==null){
			ms=new InvokeStat();
			ms.name=name;
			methodStats.put(name, ms);
		}
		ms.invoke(e!=null, time);
		totalInvokeCount.increment();
	}
	//
	public List<InvokeStat>invokeStats(){
		return new ArrayList<InvokeStat>(methodStats.values());
	}
	//
	public long totalInvokeCount(){
		return totalInvokeCount.longValue();
	}
	//--------------------------------------------------------------------------
	@Override
	public void init() throws Exception {
		if(principal==null){
			principal=Jazmin.serverName();
		}
		client=new RPCClient();
		client.principal(principal);
		client.setPushMessageCallback(this::handlePushMessage);
		serverInfoMap.forEach((cluster,serverList)->{
			serverList.forEach(serverInfo->connectToRemoteServer(serverInfo));
		});
	}
	//
	@Override
	public void start() throws Exception {
		Jazmin.scheduleAtFixedRate(
				this::checkSessionActiveStatus, 30,30, TimeUnit.SECONDS);
		ConsoleServer cs=Jazmin.server(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(new JazminRPCDriverCommand());
		}
	}
	//
	@Override
	public void stop() throws Exception {
		if(client!=null){
			client.stop();
		}
	}
	//
	@Override
	public String info() {
		InfoBuilder ib=InfoBuilder.create();
		ib.section("info");
		ib.format("%-30s:%-30s\n");
		ib.print("pushCallback",pushCallback);
		ib.print("principal",principal);
		//
		ib.section("remote servers");
		ib.format("%-30s:%-30s\n");
		for(Entry<String, List<RemoteServerInfo>>e:serverInfoMap.entrySet()){
			ib.println("cluster:"+e.getKey());
			e.getValue().forEach(rs->{
				ib.print(rs.name,rs.name+"/"+rs.remoteHostAddress+":"+rs.remotePort);	
			});
			
		}
		//
		ib.section("async proxys");
		ib.format("%s\n");
		asyncProxyMap.keySet().forEach(o->ib.print(o));
		ib.section("sync proxys");
		ib.format("%s\n");
		syncProxyMap.keySet().forEach(o->ib.print(o));
		return ib.toString();
	}
}
