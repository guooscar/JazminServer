/**
 * 
 */
package jazmin.deploy.workflow.definition;

import java.util.LinkedList;
import java.util.List;

/**
 * @author yama
 *
 */
public class Node implements Comparable<Node>{
	public static final String TYPE_START="start";
	public static final String TYPE_END="end";
	public static final String TYPE_TASK="task";
	public static final String TYPE_FORK="fork";
	public static final String TYPE_JOIN="join";
	public static final String TYPE_DECISION="decision";
	//
	public static final String SCRIPT_TYPE_JAVA="java";
	public static final String SCRIPT_TYPE_JS="js";
	public static final String SCRIPT_TYPE_JSFILE="jsfile";
	//
	//info
	public String type;
	public String name;
	public String id;
	//
	//
	public List<Transition>transitions=new LinkedList<>();
	//
	public String scriptType;
	public String execute;
	//graphics
	public int x;
	public int y;
	//
	public Node(){
		
	}
	public Node(String id,String name,String type,String execute,Transition ...t){
		this.id=id;
		this.name=name;
		this.type=type;
		this.execute=execute;
		if(t!=null){
			for(Transition tt:t){
				transitions.add(tt);
			}
		}
	}
	//
	@Override
	public int compareTo(Node o) {
		return this.id.compareTo(o.id);
	}
}
