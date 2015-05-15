/**
 * 
 */
package jazmin.deploy.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;



/**
 * @author yama
 *
 */
public class TopSearch {
	public static List<Application> topSearch(List<Application>apps){
		if(apps.isEmpty()){
			return apps;
		}
		List<Application>result=new ArrayList<Application>();
		LinkedList<String>zeroInNodes=new LinkedList<String>();
		Set<String>dependsMap=new TreeSet<String>();
		
		for(Application a:apps){
			for(String d:a.depends){
				dependsMap.add(a.id+"_"+d);
			}
		}
		for(Application a:apps){
			if(isZeroInput(a.id,dependsMap)){
				zeroInNodes.add(a.id);
			}
		}
		if(zeroInNodes.isEmpty()){
			throw new IllegalStateException("has circle");
		}
		List<String>temp=new ArrayList<String>();
		while(!zeroInNodes.isEmpty()){
			String take=zeroInNodes.removeFirst();
			temp.add(take);
			for(String s:new ArrayList<String>(dependsMap)){
				if(s.startsWith(take+"_")){
					dependsMap.remove(s);
					//
					String toNode=s.split("_")[1];
					if(isZeroInput(toNode, dependsMap)){
						zeroInNodes.add(toNode);
					}
				}
			}
			
		}
		//
		Map<String,Application>appMap=new HashMap<String, Application>();
		apps.forEach(a->appMap.put(a.id,a));
		temp.forEach(s->result.add(appMap.get(s)));
		//
		return result;
	}
	//
	private static boolean isZeroInput(String node,Set<String>depends){
		boolean hasDepend=true;
		for(String s:depends){
			if(s.endsWith("_"+node)){
				hasDepend=false;
			}
		}
		return hasDepend;
	}
}
