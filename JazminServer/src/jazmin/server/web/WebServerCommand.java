package jazmin.server.web;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ascii.TablePrinter;
import jazmin.server.console.builtin.ConsoleCommand;
import jazmin.server.web.mvc.ControllerStub;
import jazmin.server.web.mvc.DispatchServlet;
import jazmin.server.web.mvc.MethodStub;
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
    	addOption("openlogger",false,"open jetty logger.",this::openLogger);
    	addOption("closelogger",false,"close jetty logger.",this::closeLogger);
    	//
    	webServer=Jazmin.getServer(WebServer.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(webServer==null){
			 out.println("can not find WebServer.");
			 return;
		 }
		 super.run();
	}
	//
	private void closeLogger(String args){
		  JettyLogger.enable=false;
	}
	//
	private void openLogger(String args){
		JettyLogger.enable=true;
	}
    //
    private void showServerInfo(String args){
    	out.println(webServer.info());
	}
    //
    private void showServices(String args){
    	TablePrinter tp=TablePrinter.create(out)
    			.length(40,10,10)
    			.headers("URL","METHOD","ACTION");
    	List<ControllerStub>csList=DispatchServlet.dispatcher.controllerStubs();
		List<MethodStub>msList=new ArrayList<MethodStub>();
		csList.forEach(cs->msList.addAll(cs.methodStubs()));
		Collections.sort(msList);
		for(MethodStub ms:msList){
			String action=ms.invokeMethod.getDeclaringClass().getSimpleName()
					+"."+ms.invokeMethod.getName();
			tp.print(
					ms.controllerId+"/"+ms.id,
					ms.method,
					action);	
		}
    }
}
