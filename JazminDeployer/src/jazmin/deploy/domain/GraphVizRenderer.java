/**
 * 
 */
package jazmin.deploy.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jazmin.deploy.manager.DeployManager;

/**
 * @author yama
 *
 */
public class GraphVizRenderer {
	//
	private Map<String,String>nodeColorMap;
	private Map<String,String>nodeStyleMap;
	public GraphVizRenderer() {
		nodeColorMap=new HashMap<String, String>();
		nodeStyleMap=new HashMap<String, String>();
		//
		nodeColorMap.put(Application.LAYER_USER,"aliceblue");
		nodeColorMap.put(Application.LAYER_PROXY,"pink1");
		nodeColorMap.put(Application.LAYER_WEB,"pink1");
		nodeColorMap.put(Application.LAYER_APP,"lightblue2");
		nodeColorMap.put(Application.LAYER_CACHE,"olivedrab1");
		nodeColorMap.put(Application.LAYER_DB,"olivedrab1");
		nodeColorMap.put(Application.LAYER_OTHER,"yellow3");
		//
		nodeStyleMap.put(Application.LAYER_USER,"component");
		nodeStyleMap.put(Application.LAYER_PROXY,"box");
		nodeStyleMap.put(Application.LAYER_WEB,"box");
		nodeStyleMap.put(Application.LAYER_APP,"box");
		nodeStyleMap.put(Application.LAYER_CACHE,"tab");
		nodeStyleMap.put(Application.LAYER_DB,"tab");
		nodeStyleMap.put(Application.LAYER_OTHER,"component");
	}
	//
	private void renderCommon(StringBuilder result,String system){
		result.append("digraph ").
		append("\"").
		append(system).
		append("\"\n{");
		result.append("graph[page=\"fill\",size=\"13,7\",ratio=fill,center=1,bgcolor=\"transparent\"];\n");
		result.append("rankdir=LR;\n");
		result.append("node[fontname=\"simhei\",width=3.0,style=\"filled,rounded,solid\"];\n");
		result.append("edge[fontname=\"simhei\",fillcolor=gray];\n");
	}
	//
	public String renderInstanceGraph(String system,String cluster){
		StringBuilder result=new StringBuilder();
		List<Application>apps=DeployManager.getApplicationBySystem(system);
		List<Instance>instances=DeployManager.getInstances();
		renderCommon(result,system);
		//
		renderNodeList(result,Application.LAYER_USER,apps,instances,cluster);
		renderNodeList(result,Application.LAYER_PROXY,apps,instances,cluster);
		renderNodeList(result,Application.LAYER_WEB,apps,instances,cluster);
		renderNodeList(result,Application.LAYER_APP,apps,instances,cluster);
		renderNodeList(result,Application.LAYER_CACHE,apps,instances,cluster);
		renderNodeList(result,Application.LAYER_DB,apps,instances,cluster);
		renderNodeList(result,Application.LAYER_OTHER,apps,instances,cluster);
		renderNodeStatusList(result,instances,cluster);
		//
		renderEdge(result,Application.LAYER_USER,Application.LAYER_PROXY);
		renderEdge(result,Application.LAYER_PROXY,Application.LAYER_WEB);
		renderEdge(result,Application.LAYER_WEB,Application.LAYER_APP);
		renderEdge(result,Application.LAYER_APP,Application.LAYER_CACHE);
		renderEdge(result,Application.LAYER_CACHE,Application.LAYER_DB);
		renderEdge(result,Application.LAYER_DB,Application.LAYER_OTHER);
		//
		for(Application a:apps){
			renderDepends(result,a,instances,cluster);
		}
		//
		result.append("}");
		return result.toString();
	}
	//
	//
	private  void renderDepends(StringBuilder sb,
			Application app,
			List<Instance>instances,
			String cluster){
		for(String depend:app.depends){
			List<String>selfInstance=getInstanceIds(app.id, cluster, instances);
			List<String>dependInstances=getInstanceIds(depend, cluster, instances);
			if(selfInstance.isEmpty()){
				selfInstance.add(app.id);
			}
			if(dependInstances.isEmpty()){
				dependInstances.add(depend);
			}
			//
			for(String f:selfInstance){
				for(String t:dependInstances){
					renderEdge(sb, f,t);			
				}
			}
		}
	}
	//
	
	//
	private static List<String>getInstanceIds(String appId,String cluster,List<Instance>instances){
		List<String>result=new ArrayList<String>();
		for(Instance selfInstance:instances){
			if(selfInstance.appId.equals(appId)&&selfInstance.cluster.equals(cluster)){
				result.add(instanceToNode(selfInstance));
			}
		}
		return result;
	}
	//
	private static String instanceToNode(Instance i){
		String node= i.id+"\\n"+i.application.type+"["+i.machineId+":"+i.port+"]";
		return node;
	}
	//
	
	//
	private  void renderNodeList(
			StringBuilder sb,
			String layer,
			List<Application>apps,List<Instance>instances,String cluster){
		String color=nodeColorMap.get(layer);
		String style=nodeStyleMap.get(layer);
		sb.append("{node [shape="+style+",fillcolor="+color+",style=\"filled,rounded\"];\n");
		sb.append("rank = same;\n");
		sb.append("\""+layer+"\";\n");
		for(Application a:apps){
			if(a.getLayer().equals(layer)){
				boolean find=false;
				for(Instance i:instances){
					if(i.appId.equals(a.id)&&i.cluster.equals(cluster)){
						find=true;
						sb.append("\""+instanceToNode(i)+"\";\n");		
					}
				}
				if(!find){
					sb.append("\""+a.id+"\";\n");				
				}
			}
		}
		sb.append("}\n");
	}
	//
	private  void renderNodeStatusList(
			StringBuilder sb,
			List<Instance>instances,String cluster){
		for(Instance i:instances){
			if(i.cluster.equals(cluster)){
				if(!i.isAlive){
					sb.append("\""+instanceToNode(i)+"\" [fillcolor=gray]\n");		
				}
			}
		}
	}
	//
	private  void renderEdge(StringBuilder sb,String from,String to){
		sb.append("\""+from+"\"");
		sb.append("->");
		sb.append("\""+to+"\";\n");
	}
	
}
