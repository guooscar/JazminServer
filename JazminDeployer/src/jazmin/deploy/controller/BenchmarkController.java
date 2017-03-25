/**
 * 
 */
package jazmin.deploy.controller;

import jazmin.deploy.domain.monitor.MonitorInfo;
import jazmin.deploy.manager.BenchmarkSession;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.manager.MonitorManager;
import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.Controller;
import jazmin.server.web.mvc.HttpMethod;
import jazmin.server.web.mvc.JsonView;
import jazmin.server.web.mvc.ResourceView;
import jazmin.server.web.mvc.Service;

/**
 * 
 * @author icecooly
 *
 */
@Controller(id = "benchmark")
public class BenchmarkController {
	//
	@Service(id = "data", method = HttpMethod.POST)
	public void data(Context c) {
		BenchmarkSession session=DeployManager.getBenchmarkSession(c.getString("id"));
		if(session!=null){
			//c.put(session,session.);
		}
		c.view(new JsonView());
	}
	//
	@Service(id = "graph", method = HttpMethod.GET)
	public void graph(Context c) {
		BenchmarkSession session=DeployManager.getBenchmarkSession(c.getString("id"));
		if(session!=null){
			//c.put(session,session.);
		}
		c.view(new ResourceView("/jsp/benchmark_graph.jsp"));
	}
}