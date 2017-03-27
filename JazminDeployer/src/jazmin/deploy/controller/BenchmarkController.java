/**
 *
 */
package jazmin.deploy.controller;

import jazmin.deploy.manager.BenchmarkSession;
import jazmin.deploy.manager.DeployManager;
import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.Controller;
import jazmin.server.web.mvc.HttpMethod;
import jazmin.server.web.mvc.JsonView;
import jazmin.server.web.mvc.ResourceView;
import jazmin.server.web.mvc.Service;

/**
 * @author icecooly
 */
@Controller(id = "benchmark")
public class BenchmarkController {
    @Service(id = "data", method = HttpMethod.POST)
    public void data(Context c) {
        BenchmarkSession session = DeployManager.getBenchmarkSession(c.getString("id"));
        if (session != null) {
            c.put("total", session.getTotalStat());
            c.put("all", session.getAllTotalStats());
            c.put("finished", session.finished);
        }
        c.view(new JsonView());
    }

    @Service(id = "stats", method = HttpMethod.POST)
    public void stats(Context c) {
        BenchmarkSession session = DeployManager.getBenchmarkSession(c.getString("id"));
        if (session != null) {
            c.put("list", session.getAllStats());
            c.put("finished", session.finished);
        }
        c.view(new JsonView());
    }

    //
    @Service(id = "graph", method = HttpMethod.GET)
    public void graph(Context c) {
        String benchmarkId = c.getString("id");
        BenchmarkSession session = DeployManager.getBenchmarkSession(benchmarkId);
        if (session != null) {
            c.put("session", session);
        }
        c.put("benchmarkId", benchmarkId);
        c.view(new ResourceView("/jsp/benchmark_graph.jsp"));
    }

}