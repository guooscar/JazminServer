/**
 * 
 */
package jazmin.deploy.workflow.execute;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.deploy.workflow.WorkflowEngine;
import jazmin.deploy.workflow.WorkflowEvent;
import jazmin.deploy.workflow.definition.Node;
import jazmin.deploy.workflow.definition.Transtion;
import jazmin.deploy.workflow.definition.WorkflowProcess;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class ProcessInstance {
	//
	private static Logger logger=LoggerFactory.get(ProcessInstance.class);
	//
	String id;
	//
	private WorkflowProcess process;
	private Node startNode;
	private Set<Node> endNodes;
	private Map<String, Node>nodeMap;
	//
	private Map<String,Execute>executeMap;
	WorkflowEngine engine;
	Map<String,ExecuteHistory>historyMap;
	List<ExecuteHistory>allHistories;
	ExecuteContext context;
	private boolean isDone;
	//
	private Set<String>tokenNodes;
	EventHandler eventHandler;
	ExceptionHandler exceptionHandler;
	private static AtomicInteger instanceCounter=new AtomicInteger(0);
	//
	public ProcessInstance(WorkflowProcess process,WorkflowEngine engine) {
		nodeMap=new HashMap<>();
		executeMap=new HashMap<>();
		this.engine=engine;
		endNodes=new TreeSet<>();
		context=new ExecuteContext(this);
		load(process);
		id=process.id+"-"+instanceCounter.incrementAndGet();
		tokenNodes=new TreeSet<>();
	}
	
	/**
	 * @return the exceptionHandler
	 */
	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * @param exceptionHandler the exceptionHandler to set
	 */
	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	//
	public List<ExecuteHistory>getAllExecuteHistories(){
		return allHistories;
	}
	/**
	 * @return
	 */
	public List<ExecuteHistory>getExecuteHistories(){
		return new ArrayList<ExecuteHistory>(historyMap.values());
	}
	/**
	 * @return the executeHandler
	 */
	public EventHandler getEventHandler() {
		return eventHandler;
	}

	/**
	 * @param executeHandler the executeHandler to set
	 */
	public void setEventHandler(EventHandler executeHandler) {
		this.eventHandler = executeHandler;
	}

	/**
	 * @return
	 */
	public WorkflowProcess getWorkflowProcess(){
		return this.process;
	}
	/**
	 * @return
	 */
	public Set<String>getTokenNodes(){
		return new TreeSet<>(tokenNodes);
	}
	/**
	 * @return
	 */
	public String getId(){
		return id;
	}
	/**
	 * @return the startNode
	 */
	public Node getStartNode() {
		return startNode;
	}
	//
	public Set<Node>getEndNodes(){
		return endNodes;
	}
	/**
	 * @return the isDone
	 */
	public boolean isDone() {
		return isDone;
	}
	//
	public Map<String,Object>getVariableMap(){
		return new HashMap<>(context.variables);
	}
	//
	public void setVariable(String key,Object v){
		context.setVariable(key, v);
	}
	//
	public Object getVariable(String key){
		return context.getVariable(key);
	}
	//
	private void load(WorkflowProcess process){
		nodeMap.clear();
		executeMap.clear();
		this.process=process;
		for(Node n:process.nodes){
			if(nodeMap.containsKey(n.id)){
				throw new IllegalArgumentException("node "+n.id+" already exists");
			}
			nodeMap.put(n.id,n);
			//load execute
			if(n.scriptType!=null&&!n.scriptType.trim().isEmpty()){
				if(n.execute!=null&&!n.execute.trim().isEmpty()){
					Execute execute=engine.loadExecute(n.scriptType,n.execute);
					if(execute==null){
						throw new IllegalArgumentException("can not load execute with express:"+n.execute);
					}
					executeMap.put(n.execute,execute);
				}
			}
			//only one start node allowed
			if(n.type.equals(Node.TYPE_START)){
				if(startNode!=null){
					throw new IllegalArgumentException("start node already set "+startNode.id+" - "+n.id);
				}
				startNode=n;
			}
			//must have one end node at least
			if(n.type.equals(Node.TYPE_END)){
				endNodes.add(n);
				if(n.transtions.size()>0){
					throw new IllegalArgumentException(n.type+" node allow no out "+n.id);
				}
			}
			//
			//only one out
			if(n.type.equals(Node.TYPE_JOIN)
					||n.type.equals(Node.TYPE_START)
					||n.type.equals(Node.TYPE_TASK)
					){
				if(n.transtions.size()>1){
					throw new IllegalArgumentException(n.type+" node allow one out "+n.id);
				}
			}
			
		}
		//
		if(startNode==null){
			throw new IllegalArgumentException("no start node");
		}
		if(endNodes.isEmpty()){
			throw new IllegalArgumentException("no end node");
		}
		//
	}
	/**
	 * return node by id
	 */
	public Node getNode(String id){
		return nodeMap.get(id);
	}
	/**
	 * start execute
	 * @param nodeId
	 */
	public void start(){
		isDone=false;
		historyMap=new ConcurrentHashMap<>();
		allHistories=new ArrayList<>();
		enter(null,startNode);
		signal(startNode.id);
	}
	//
	public void signal(String nodeId){
		Node node=nodeMap.get(nodeId);
		if(node==null){
			throw new IllegalArgumentException("can not find node:"+nodeId);
		}
		//
		ExecuteHistory history=historyMap.get(node.id);
		Execute execute=null;
		if(node.execute!=null){
			execute=executeMap.get(node.execute);
		}
		if(execute!=null){
			try {
				context.currentNode=node;
				execute.execute(context);
			} catch (Exception e) {
				handleException(e);
				history.error=e;
			}
		}
		
		//
		if(node.type.equals(Node.TYPE_START)){
			transition(node);
			return;
		}
		//
		if(node.type.equals(Node.TYPE_END)){
			leave(node);
			isDone=true;
			return;
		}
		//
		if(node.type.equals(Node.TYPE_FORK)){
			transition(node);
		}	
		//
		if(node.type.equals(Node.TYPE_TASK)){
			transition(node);
		}
		//
		if(node.type.equals(Node.TYPE_JOIN)){
			boolean allPresent=true;
			for(Node dependNode:findDependNodes(node.id)){
				System.err.println(dependNode.id);
				if(!history.fromNodes.contains(dependNode.id)){
					allPresent=false;
				}
			}
			if(allPresent){
				transition(node);
			}
			return;
		}
	}
	//
	private void handleException(Throwable e){
		logger.catching(e);
		if(exceptionHandler!=null){
			try{
				exceptionHandler.onException(context, e);
			}catch (Exception ee) {
				logger.catching(ee);
			}
		}
	}
	//
	private List<Node>findDependNodes(String nodeId){
		List<Node>result=new ArrayList<>();
		nodeMap.forEach((k,v)->{
			for(Transtion t:v.transtions){
				if(t.to!=null&&t.to.equals(nodeId)){
					result.add(v);
				}
			}
		});
		return result;
	}
	//
	void transition(Node node){
		transition(node,null);
	}
	//
	void transition(Node node,String toNodeId){
		leave(node);
		if(toNodeId!=null){
			Node toNode=null;
			for(Transtion t:node.transtions){
				if(t.to.equals(toNodeId)){
					toNode=getNode(t.to);
				}
			}
			if(toNode==null){
				throw new IllegalArgumentException("can not find transition:"+toNodeId+" on node:"+node.id);
			}
			enter(node, toNode);
			if(!toNode.type.equals(Node.TYPE_TASK)){
				signal(toNode.id);
			}
		}else{
			for(Transtion t:node.transtions){
				Node toNode=getNode(t.to);
				enter(node, toNode);
				if(!toNode.type.equals(Node.TYPE_TASK)){
					signal(toNode.id);
				}
			}
		}	
	}
	//
	void enter(Node fromNode,Node node){
		if(node==null){
			throw new IllegalArgumentException("enter node null");
		}
		//
		ExecuteHistory history=historyMap.get(node.id);
		if(history==null){
			history=new ExecuteHistory();
			history.id=UUID.randomUUID().toString();
			history.node=node.id;
			history.status=ExecuteHistory.STATUS_ENTER;
			historyMap.put(node.id, history);
		}
		if(fromNode!=null){
			history.fromNodes.add(fromNode.id);
		}
		//
		ExecuteHistory allHistory=new ExecuteHistory();
		allHistory.node=node.id;
		allHistory.startTime=new Date();
		allHistories.add(allHistory);
		//
		tokenNodes.add(node.id);
		WorkflowEvent enterEvent=new WorkflowEvent();
		enterEvent.instance=this;
		enterEvent.node=node;
		enterEvent.type=WorkflowEvent.TYPE_ENTER;
		engine.fireEvent(enterEvent);
		//
		if(eventHandler!=null){
			try {
				context.currentNode=node;
				eventHandler.onEvent(context,EventHandler.EVENT_TYPE_ENTER);
			} catch (Exception e) {
				history.error=e;
				handleException(e);
			}
		}
	}
	//
	void leave(Node node){
		if(!tokenNodes.contains(node.id)){
			throw new IllegalArgumentException("node "+node.id+" doesnot have token");
		}
		if(node.type.equals(Node.TYPE_END)){
			isDone=true;
		}
		//
		ExecuteHistory history=historyMap.get(node.id);
		history.status=ExecuteHistory.STATUS_FINISHED;
		history.endTime=new Date();
		history.fromNodes.clear();
		//
		ExecuteHistory allHistory=null;
		for(int i=allHistories.size()-1;i>=0;i--){
			ExecuteHistory eh=allHistories.get(i);
			if(eh.node.equals(node.id)){
				allHistory=eh;
			}
		}
		if(allHistory!=null){
			allHistory.endTime=new Date();
		}
		//
		tokenNodes.remove(node.id);
		//
		WorkflowEvent enterEvent=new WorkflowEvent();
		enterEvent.instance=this;
		enterEvent.node=node;
		enterEvent.type=WorkflowEvent.TYPE_LEAVE;
		engine.fireEvent(enterEvent);
		//
		if(eventHandler!=null){
			try {
				context.currentNode=node;
				eventHandler.onEvent(context,EventHandler.EVENT_TYPE_LEAVE);
			} catch (Exception e) {
				history.error=e;
				handleException(e);
			}
		}
	}
	//
	
}
