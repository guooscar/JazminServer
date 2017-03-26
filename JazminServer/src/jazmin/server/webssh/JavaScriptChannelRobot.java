/**
 * 
 */
package jazmin.server.webssh;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.IOUtil;

/**
 * @author yama
 *
 */
public class JavaScriptChannelRobot extends ChannelRobot implements ScriptChannelContext{
	private static Logger logger=LoggerFactory.get(JavaScriptChannelRobot.class);
	static class ExpectHolder{
		String regex;
		boolean once;
		ExpectCallback callback;
	}
	//
	HookInputCallback hookInputCallback;
	TicketCallback ticketCallback;
	ActionCallback openCallback;
	ActionCallback closeCallback;
	Map<String,ExpectHolder>expectCallbacks;
	WebSshChannel webSshChannel;
	Map<Long,List<ActionCallback>>afterTicketCallbacks;
	Map<String,Object>context;
	//
	public JavaScriptChannelRobot(String source) throws ScriptException {
		this(source,null);
	}
	//
	public JavaScriptChannelRobot(String source,Map<String,Object>context)
			throws ScriptException {
		this.context=context;
		expectCallbacks=new ConcurrentHashMap<>();
		afterTicketCallbacks=new ConcurrentHashMap<>();
		loadJavaScript(source);
	}
	//----------------------------------------------------------------------
	private void loadJavaScript(String source)throws ScriptException{
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("nashorn");
		
		Bindings bindings=engine.createBindings();
	
		SimpleScriptContext ssc=new SimpleScriptContext();
		ssc.setAttribute("robot",this, ScriptContext.ENGINE_SCOPE);
		bindings.put("robot", this);
		if(context!=null){
			context.forEach((name,object)->{
				ssc.setAttribute(name,object, ScriptContext.ENGINE_SCOPE);
				bindings.put("name",object);
					
			});
		}
		engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		String commonScript=
					"load('nashorn:mozilla_compat.js');"+
					"importPackage(Packages.jazmin.server.webssh);\n";
		engine.eval(commonScript+source, ssc); 
	}
	//----------------------------------------------------------------------
	@Override
	public String name() {
		if(webSshChannel!=null){
			return webSshChannel.connectionInfo.name+"";
		}
		return "undefined";
	}
	@Override
	public long setTimeout(int ticket, ActionCallback callback) {
		long targetTicket=webSshChannel.ticket+ticket;
		List<ActionCallback>list=afterTicketCallbacks.get(targetTicket);
		if(list==null){
			list=new LinkedList<>();
			afterTicketCallbacks.put(targetTicket, list);
		}
		list.add(callback);
		return targetTicket;
	}
	//
	@Override
	public void clearTimeout(long ticket) {
		afterTicketCallbacks.remove(ticket);
	}
	//
	@Override
	public boolean enableInput() {
		if(webSshChannel!=null){
			return webSshChannel.connectionInfo.enableInput;
		}
		return false;
	}
	@Override
	public void enableInput(boolean enable) {
		if(webSshChannel!=null){
			webSshChannel.connectionInfo.enableInput=enable;
		}	
	}
	//
	@Override
	public void sends(String msg) {
		if(webSshChannel!=null){
			webSshChannel.sendMessageToServer(msg);
		}
	}
	//
	@Override
	public void sendc(String msg) {
		if(webSshChannel!=null){
			webSshChannel.sendMessageToClient(msg);
		}
	}
	//
	@Override
	public void close() {
		if(webSshChannel!=null){
			webSshChannel.closeChannel();
		}
	}
	//
	@Override
	public void ticket(TicketCallback callback) {
		this.ticketCallback=callback;
	}
	//
	private void expect0(String regex,boolean once,ExpectCallback callback){
		synchronized (expectCallbacks) {
			ExpectHolder holder=new ExpectHolder();
			holder.callback=callback;
			holder.once=once;
			holder.regex=regex;
			expectCallbacks.put(regex, holder);
		}
	}
	//
	@Override
	public void expect(String regex, ExpectCallback callback) {
		expect0(regex,false,callback);
	}
	@Override
	public void expectOnce(String regex,ExpectCallback callback){
		expect0(regex,true,callback);
	}
	@Override
	public void expectClear() {
		synchronized (expectCallbacks) {
			expectCallbacks.clear();
		}
	}
	@Override
	public void hookin(HookInputCallback callback) {
		this.hookInputCallback=callback;
		enableInput(callback!=null);
	}
	//
	@Override
	public void open(ActionCallback callback) {
		this.openCallback=callback;;
	}
	//
	@Override
	public void close(ActionCallback callback) {
		this.closeCallback=callback;
	}
	//
	@Override
	public String host() {
		return webSshChannel==null?null:webSshChannel.connectionInfo.host;
	}
	//
	@Override
	public int port() {
		return webSshChannel==null?null:webSshChannel.connectionInfo.port;
	}
	//
	@Override
	public String user() {
		return webSshChannel==null?null:webSshChannel.connectionInfo.user;
	}
	//
	@Override
	public String password() {
		return webSshChannel==null?null:webSshChannel.connectionInfo.password;
	}
	//
	@Override
	public long ticket() {
		return webSshChannel==null?0:webSshChannel.ticket;
	}
	//
	@Override
	public void http(String url,HttpCallback callback) {
		try {
			String result=IOUtil.getContent(new URL(url).openStream());
			callback.invoke(result, null);
		} catch (Exception e) {
			callback.invoke(null, e.getMessage());
		} 
	}
	@Override
	public void log(String msg) {
		logger.info(msg);
	}
	//----------------------------------------------------------------------
	@Override
	public void onOpen(WebSshChannel channel) {
		this.webSshChannel=channel;
		if(this.openCallback!=null){
			this.openCallback.invoke();
		}
	}
	//
	@Override
	public void onInput(WebSshChannel channel, String message) {
		if(hookInputCallback!=null){
			hookInputCallback.invoke(message);
		}
	}
	//
	@Override
	public boolean inputSendToServer() {
		return hookInputCallback==null?true:false;
	}
	//
	StringBuilder messageBuffer=new StringBuilder();
	//
	@Override
	public void onMessage(WebSshChannel channel, String message) {
		synchronized (messageBuffer) {
			for(char c:message.toCharArray()){
				messageBuffer.append(c);
				if(c=='\n'||c=='\r'){
					matchMessage(messageBuffer.toString().trim());
					messageBuffer.delete(0, messageBuffer.length());
				}
			}
		}
	}
	//
	private void matchMessage(String line){
		if(line.trim().isEmpty()){
			return;
		}
		List<String>removedCallbacks=new LinkedList<>();
		expectCallbacks.forEach((regex,holder)->{
			if(Pattern.matches(regex, line)){
				holder.callback.invoke(line);
				if(holder.once){
					removedCallbacks.add(holder.regex);
				}
			}
		});
		for(String s:removedCallbacks){
			expectCallbacks.remove(s);
		}
	}
	//
	@Override
	public void onTicket(WebSshChannel channel, long ticket) {
		/**
		 * last message is not end of \n 
		 * get them on next ticket
		 */
		synchronized (messageBuffer) {
			if(messageBuffer.length()>0){
				matchMessage(messageBuffer.toString().trim());
				messageBuffer.delete(0, messageBuffer.length());
			}
		}
		afterTicketCallbacks.forEach((target,list)->{
			if(target==ticket){
				list.forEach(action->action.invoke());
			}
		});
		afterTicketCallbacks.remove(ticket);
		if(this.ticketCallback!=null){
			this.ticketCallback.invoke(ticket);
		}
	}
	//
	@Override
	public void onClose(WebSshChannel channel) {
		if(this.closeCallback!=null){
			this.closeCallback.invoke();
		}
	}
	
	
}
