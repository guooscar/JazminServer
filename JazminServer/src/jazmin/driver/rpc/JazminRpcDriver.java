/**
 * 
 */
package jazmin.driver.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
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
import jazmin.core.monitor.Monitor;
import jazmin.core.monitor.MonitorAgent;
import jazmin.core.thread.Dispatcher;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;
import jazmin.misc.io.InvokeStat;
import jazmin.server.console.ConsoleServer;
import jazmin.server.rpc.RpcClient;
import jazmin.server.rpc.RpcMessage;
import jazmin.server.rpc.RpcSession;

/**
 * use JazminRPCDriver to connect to JazminRPCServer 
 * @author yama
 * 25 Dec, 2014
 */
public class JazminRpcDriver extends Driver{
	private static Logger logger=LoggerFactory.get(JazminRpcDriver.class);
	//
	private RpcClient client;
	private Map<String,List<RpcSession>>sessionMap;
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
		public String credential;
		public boolean enableSSL;
	}
	//
	public JazminRpcDriver(){
		sessionMap=new ConcurrentHashMap<String, List<RpcSession>>();
		serverInfoMap=new ConcurrentHashMap<String, List<RemoteServerInfo>>();
		syncProxyMap=new ConcurrentHashMap<String, Object>();
		syncHandlerMap=new ConcurrentHashMap<String, InvocationHandler>();
		asyncProxyMap=new ConcurrentHashMap<String, Object>();
		asyncHandlerMap=new ConcurrentHashMap<String, InvocationHandler>();
		topicMap=new ConcurrentHashMap<String, Set<String>>();
		methodStats=new ConcurrentHashMap<String, InvokeStat>();
		totalInvokeCount=new LongAdder();
		pushCallbackMethod=PushCallback.class.getMethods()[0];
		client=new RpcClient();
	}
	/**
	 * set principal of this rpc driver
	 * @param p the principal
	 */
	public void setPrincipal(String p){
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.principal=p;
	}
	/**
	 * get principal of this rpc client
	 * @return
	 */
	public String getPrincipal(){
		return this.principal;
	}
	/**
	 * if disable server push message
	 * @param dpm if disable flag
	 */
	public void setDisablePushMessage(boolean dpm){
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		disablePushMessage=dpm;
	}
	/**
	 * return if disable server push message feature
	 * @return if disable flag
	 */
	public boolean isDisablePushMessage(){
		return disablePushMessage;
	}
	/**
	 * add remote server using information specified by uri.
	 * jazmin://credential@host:port/cluster/name
	 * @param url the remote server uri
	 * @throws MalformedURLException 
	 */
	public void addRemoteServer(String uri) throws URISyntaxException{
		URI u=new URI(uri);
		String schema=u.getScheme();
		if(schema==null||(!schema.equals("jazmin")&&!schema.equals("jazmins"))){
			throw new IllegalArgumentException("schema should be jazmin or jazmins");
		}
		String host=u.getHost();
		int port=u.getPort();
		String credential=u.getUserInfo();
		String path=u.getPath();
		boolean enableSSL=schema.equals("jazmins");
		//
		String ss[]=path.split("/");
		if(ss.length<3){
			throw new IllegalArgumentException("can not find cluster or server name");
		}
		if(credential==null){
			addRemoteServer(ss[1],ss[2], host, port,enableSSL);
		}else{
			addRemoteServer(ss[1],ss[2],credential,host, port,enableSSL);
		}
	}
	/**
	 * add remote server that rpc driver will connecting
	 * @param cluster the cluster of this server
	 * @param name the name of this server
	 * @param host the host of this server
	 * @param port the port of this server
	 */
	public void addRemoteServer(
			String cluster,
			String name,
			String host,
			int port){
		addRemoteServer(cluster,name,null,host,port,false);
	}
	/**
	 * add remote server that rpc driver will connecting
	 * @param cluster the cluster of this server
	 * @param name the name of this server
	 * @param host the host of this server
	 * @param port the port of this server
	 * @param if enable ssl connect
	 */
	public void addRemoteServer(
			String cluster,
			String name,
			String host,
			int port,
			boolean enableSSL){
		addRemoteServer(cluster,name,null,host,port,enableSSL);
	}
	/**
	 * 
	/**
	 * add remote server that rpc driver will connecting
	 * @param cluster the cluster of this server
	 * @param name the name of this server
	 * @param credential the credential of this server
	 * @param host the host of this server
	 * @param port the port of this server
	 */
	public void addRemoteServer(
			String cluster,
			String name,
			String credential,
			String host,
			int port,
			boolean enableSSL){
		if(isInited()){
			throw new IllegalArgumentException("set before inited");
		}
		RemoteServerInfo si=new RemoteServerInfo();
		si.cluster=cluster;
		si.name=name;
		si.remoteHostAddress=host;
		si.remotePort=port;
		si.credential=credential;
		si.enableSSL=enableSSL;
		List<RemoteServerInfo>serverList=serverInfoMap.get(cluster);
		if(serverList==null){
			serverList=new ArrayList<>();
			serverInfoMap.put(cluster, serverList);
		}
		serverList.add(si);
	}
	//
	private void connectToRemoteServer(RemoteServerInfo serverInfo){
		RpcSession session=new RpcSession();
		session.setRemoteHostAddress(serverInfo.remoteHostAddress);
		session.setRemotePort(serverInfo.remotePort);
		session.setCluster(serverInfo.cluster);
		session.setPrincipal(principal);
		session.setCredential(serverInfo.credential);
		session.setDisablePushMessage(disablePushMessage);
		session.setEnableSSL(serverInfo.enableSSL);
		Set<String>topics=topicMap.get(serverInfo.cluster);
		if(topics!=null){
			topics.forEach((topic)->session.subscribe(topic));
		}
		try {
			client.connect(session);
		} catch (Exception e) {
			logger.catching(e);
		}	
		List<RpcSession>sessionList=sessionMap.get(serverInfo.cluster);
		if(sessionList==null){
			sessionList=(new ArrayList<RpcSession>());
			sessionMap.put(serverInfo.cluster,sessionList);
		}
		logger.info("create rpc session:"+session);
		sessionList.add(session);
	}
	/**
	 * set push callback of this driver
	 * @param pushCallback the pushCallback to set
	 * @see PushCallback
	 */
	public void setPushCallback(PushCallback pushCallback) {
		this.pushCallback = pushCallback;
	}
	/**
	 * return push callback of this driver
	 * @return  push callback of this driver
	 */
	public PushCallback getPushCallback(){
		return pushCallback;
	}
	/**
	 * return all session of this driver
	 * @return all session of this driver
	 * @see RpcSession
	 */
	public List<RpcSession>getSessions(){
		List<RpcSession>ss=new ArrayList<RpcSession>();
		sessionMap.forEach((cluster,sessionList)->ss.addAll(sessionList));
		return ss;
	}
	/**
	 * return all synchronized proxy of this driver
	 * @return all synchronized proxy of this server 
	 */
	public List<String>getSyncProxys(){
		return new ArrayList<String>(syncProxyMap.keySet());
	}
	/**
	 * return all asynchronized proxy of this driver
	 * @return all asynchronized proxy of this server 
	 */
	public List<String>getAsyncProxys(){
		return new ArrayList<String>(asyncProxyMap.keySet());
	}
	/**
	 * return all remote server info of this driver
	 * @return all remote server info of this driver
	 */
	public List<RemoteServerInfo>getRemoteServers(){
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
						try {
							client.connect(session);
						} catch (Exception e) {
							logger.error("can not connect to {}:{}",
									session.getRemoteHostAddress(),
									session.getRemotePort());
						}
					}
				}
			});
			
		});
	}
	private void heartbeat(){
		sessionMap.forEach((cluster,sessionList)->{
			sessionList.forEach(session->{
				synchronized (session) {
					if(session.isConnected()){
						if(logger.isDebugEnabled()){
							logger.debug("sent heartbeat {}",session.getPrincipal());
						}
						//
						try{
							client.heartbeat(session);
						}catch(Exception e){
							logger.catching(e);
						}
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
		List<RpcSession>sessions=sessionMap.get(clusterName);
		if(sessions==null){
			throw new IllegalArgumentException("can not find cluster with name:"
					+clusterName);
		}
		//
		InvocationHandler handler=handlerMap.get(clusterName);
		if(handler==null){
			if(isAsync){
				handler=new AsyncRpcInvocationHandler(this,client,sessions);	
			}else{
				handler=new SyncRpcInvocationHandler(this,client,sessions);			
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
	 * create a local sync  proxy object which connect to remote cluster
	 * @param clazz the local proxy object interface
	 * @param clusterName the remote proxy name
	 * @return the proxy object
	 */
	public <T> T create(Class<T>clazz,String clusterName){
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
	 * create a local async  proxy object which connect to remote cluster
	 * @param clazz the local proxy object interface
	 * @param clusterName the remote proxy name
	 * @return the proxy object
	 */
	public <T> T createAsync(Class<T>clazz,String clusterName){
		checkAsyncClassSignature(clazz);
		return create0(clazz, clusterName, asyncProxyMap,asyncHandlerMap,true);
	}
	//--------------------------------------------------------------------------
	//pub sub
	/**
	 * subscribe a topic event from remote cluster
	 * @param cluster the cluster name
	 * @param topic the topic name
	 */
	public void subscribe(String cluster,String topic){
		if(isStarted()){
			throw new IllegalArgumentException("set before inited");
		}
		Set<String>topics=topicMap.get(cluster);
		if(topics==null){
			topics=new TreeSet<String>();
			topicMap.put(cluster, topics);
		}
		topics.add(topic);	
	}
	//--------------------------------------------------------------------------
	private void handlePushMessage(RpcSession session,RpcMessage message){
		if(pushCallback==null){
			return;
		}
		String cluster=session.getCluster();
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
		ms.invoke(e!=null, time,time);
		totalInvokeCount.increment();
	}
	/**
	 * return all invoke statics information
	 * @return
	 */
	public List<InvokeStat>getInvokeStats(){
		return new ArrayList<InvokeStat>(methodStats.values());
	}
	/**
	 * return total invoke count of this driver
	 * @return total invoke count of this driver
	 */
	public long getTotalInvokeCount(){
		return totalInvokeCount.longValue();
	}
	/**
	 * return request timeout time in million seconds
	 * @return request timeout time in million seconds
	 */
	public long getRequestTimeout(){
		return client.getTimeout();
	}
	/**
	 * set  request timeout time in million seconds
	 * @param timeout request timeout time in million seconds
	 */
	public void setRequestTimeout(long timeout){
		client.setTimeout(timeout);
	}
	//
	//--------------------------------------------------------------------------
	@Override
	public void init() throws Exception {
		
	}
	//
	@Override
	public void start() throws Exception {
		if(principal==null){
			principal=Jazmin.getServerName();
		}
		client.setPrincipal(principal);
		client.setPushMessageCallback(this::handlePushMessage);
		serverInfoMap.forEach((cluster,serverList)->{
			serverList.forEach(serverInfo->connectToRemoteServer(serverInfo));
		});
		Jazmin.scheduleAtFixedRate(
				this::checkSessionActiveStatus, 30,30, TimeUnit.SECONDS);
		Jazmin.scheduleAtFixedRate(
				this::heartbeat, 5,5, TimeUnit.MINUTES);
		
		ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
		if(cs!=null){
			cs.registerCommand(JazminRpcDriverCommand.class);
		}
		//
		Jazmin.mointor.registerAgent(new JazminRpcDriverMonitorAgent());
	}
	//
	@Override
	public void stop() throws Exception {
		for(List<RpcSession> sessions:sessionMap.values()){
			for(RpcSession s:sessions){
				s.close();
			}
		}
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
				ib.print(rs.name,rs.name+"/"+rs.remoteHostAddress+":"+rs.remotePort+" ssl:"+rs.enableSSL);	
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
	
	//
	private class JazminRpcDriverMonitorAgent implements MonitorAgent{
		@Override
		public void sample(int idx,Monitor monitor) {
			Map<String,String>info1=new HashMap<String, String>();
			info1.put("invokeCount", getTotalInvokeCount()+"");
			monitor.sample("JazminRpcDriver.InvokeCount",Monitor.CATEGORY_TYPE_COUNT,info1);
		}
		//
		@Override
		public void start(Monitor monitor) {
			Map<String,String>info=new HashMap<String, String>();
			for(Entry<String, List<RemoteServerInfo>>e:serverInfoMap.entrySet()){
				e.getValue().forEach(rs->{
					info.put("RemoteServer-"+rs.name,rs.name+"/"+rs.remoteHostAddress+":"+rs.remotePort);	
				});
			}
			monitor.sample("JazminRpcDriver.Info",Monitor.CATEGORY_TYPE_KV,info);
		}
	}
}
