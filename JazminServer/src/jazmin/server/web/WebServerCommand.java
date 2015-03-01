package jazmin.server.web;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleCommand;
import jazmin.server.web.mvc.ControllerStub;
import jazmin.server.web.mvc.DispatchServlet;
import jazmin.server.web.mvc.MethodStub;

import org.eclipse.jetty.webapp.WebAppContext;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class WebServerCommand extends ConsoleCommand {
    private WebServer webServer;
	public WebServerCommand() {
    	super();
    	id="websrv";
    	desc="web server ctrl command";
    	addOption("i",false,"show server information.",this::showServerInfo);
    	addOption("service",false,"show services.",this::showServices);
    	//
    	webServer=Jazmin.server(WebServer.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(webServer==null){
			 err.println("can not find WebServer.");
			 return;
		 }
		 super.run();
	}
    //
    private void showServerInfo(String args){
    	String format="%-20s: %-10s\n";
		out.printf(format,"port",webServer.port());
		out.printf(format,"idleTimeout",webServer.idleTimeout());
		out.printf(format,"dirAllowed",webServer.dirAllowed());
		out.printf(format,"webAppContext",webServer.webAppContext());
		//
		out.println("servlets:");
		int index=0;
		WebAppContext webAppContext=webServer.webAppContext();
		if(webAppContext!=null){
			List<String>servletNames=new ArrayList<String>(
					webAppContext.getServletContext().getServletRegistrations().keySet());
			for(String s:servletNames){
				out.printf(format,"servlet-"+(index++),s);		
			}
		}
		out.println("welcome files:");
		for(String s:webAppContext.getWelcomeFiles()){
			out.printf(format,"welcome file-"+(index++),s);						
		}
	}
    //
    private void showServices(String args){
    	List<ControllerStub>csList=DispatchServlet.dispatcher.controllerStubs();
		List<MethodStub>msList=new ArrayList<MethodStub>();
		csList.forEach(cs->msList.addAll(cs.methodStubs()));
		Collections.sort(msList);
		out.println("total "+msList.size()+" services");
		String format="%-40s %-10s %-10s\n";
		out.format(format,"URL","METHOD","ACTION");
		for(MethodStub ms:msList){
			out.format(format,
					ms.controllerId+"/"+ms.id,
					ms.method,
					ms.invokeMethod.getDeclaringClass().getSimpleName()+"."+
					ms.invokeMethod.getName());	
		}
    }
}
