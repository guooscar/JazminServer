/**
 * 
 */
package jazmin.deploy.workflow.test;

import java.util.ArrayList;
import java.util.Scanner;

import jazmin.deploy.workflow.WorkflowEngine;
import jazmin.deploy.workflow.definition.Node;
import jazmin.deploy.workflow.definition.Transition;
import jazmin.deploy.workflow.definition.WorkflowProcess;
import jazmin.deploy.workflow.execute.ProcessInstance;
import jazmin.util.JSONUtil;

/**
 * @author yama
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WorkflowProcess process=new WorkflowProcess();
		process.id="test";
		process.name="test";
		process.nodes=new ArrayList<>();
		//
		process.nodes.add(new Node("start","start",Node.TYPE_START,null,
				new Transition("add","add")
		));
		process.nodes.add(new Node("add","add",Node.TYPE_TASK,"jsfile:script/add.js",
				new Transition("cond","cond")
		));
		process.nodes.add(new Node("cond","cond",Node.TYPE_DECISION,"jsfile:script/cond.js",
				new Transition(">5","print"),
				new Transition("<5","add")				
		));
		//
		process.nodes.add(new Node("print","print",Node.TYPE_TASK,"java:io.itit.workflow.builtin.PrintNodeExecute",
				new Transition("end","end")
		));
		//
		process.nodes.add(new Node("end","end",Node.TYPE_END,null));
		//
		System.err.println(JSONUtil.toJson(process));
		//
		WorkflowEngine engine=new WorkflowEngine();
		engine.registerEventListener((e)->{
			String ss="event:"+e.type+" "+ e.node.id+"--"+e.instance.getId()+"\n"+
					"getTokenNodes:"+e.instance.getTokenNodes();
			System.err.println(ss);
			
		});	
		//
		ProcessInstance instance=engine.startProcess(process);
		
		//
		Scanner sc=new Scanner(System.in);
		while(sc.hasNext()){
			String line=sc.nextLine();
			instance.signal(line);
		}
	}

}
