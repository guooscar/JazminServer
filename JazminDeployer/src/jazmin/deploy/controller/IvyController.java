/**
 * 
 */
package jazmin.deploy.controller;

import java.util.List;

import jazmin.core.Jazmin;
import jazmin.deploy.manager.DeployManager;
import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.Controller;
import jazmin.server.web.mvc.FileView;
import jazmin.server.web.mvc.HttpMethod;
import jazmin.server.web.mvc.PlainTextView;
import jazmin.server.web.mvc.Service;

/**
 * @author yama
 * 6 Jan, 2015
 */
@Controller(id="ivy")
public class IvyController {
	/**
	 *get boot file 
	 */
	@Service(id="config")
	public void getIvyConfig(Context c){
		String host = DeployManager.deployHostname;
		int port = DeployManager.deployHostport;
		StringBuilder result=new StringBuilder();
		result.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
		"<ivysettings>"+ 
		  "<settings defaultResolver=\"chained\"/> "+
		  "<resolvers> "+
		    "<chain name=\"chained\" returnFirst=\"true\"> "+
		      "<url name=\"integratebutton\"> "+
		        "<artifact pattern=\"http://"+host+":"+port+"/srv/ivy/repo/[artifact]-[revision].[ext]\" /> "+
		      "</url> "+
		    "</chain> "+
		  "</resolvers> "+
		 "</ivysettings>");
		c.view(new PlainTextView(result.toString()));
	}
	//
	@Service(id="repo",method=HttpMethod.ALL)
	public void getRepoFile(Context c){
		String repoDir = Jazmin.environment.getString("deploy.workspace","./workspace/");
		repoDir+="repo";
		List<String>querys=c.request().querys();
		if(querys.size()<3){
			return;
		}
		String fileName=querys.get(2);
		c.view(new FileView(repoDir+"/"+fileName));
	}
}
