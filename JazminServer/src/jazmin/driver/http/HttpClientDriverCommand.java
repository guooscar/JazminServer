package jazmin.driver.http;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleCommand;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class HttpClientDriverCommand extends ConsoleCommand {
    private HttpClientDriver httpDriver;
	public HttpClientDriverCommand() {
    	super();
    	id="http";
    	desc="http client driver ctrl command";
    	addOption("i",false,"show driver information.",this::showDriverInfo);
    	addOption("stat",false,"show query stat.",this::showStats);
    	addOption("errorlog",false,"show query error logs.",this::showErrorLogs);
    	addOption("f",false,"show full url",null);
    	//
    	httpDriver=Jazmin.getDriver(HttpClientDriver.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(httpDriver==null){
			 err.println("can not find HttpClientDriver.");
			 return;
		 }
		 super.run();
	}
	//
	
    private void showErrorLogs(String args){
    	List<String>errorLogs=httpDriver.getErrorLogs();
    	Collections.reverse(errorLogs);
    	out.println("total "+errorLogs.size()+" error logs");
    	errorLogs.forEach(out::println);
    }
	//
    private void showStats(String args){
    	String format="%-5s : %-8s %-8s %-20s %-10s %-10s %-10s %-10s %-50s \n";
		int i=0;
		List<HttpHandler>handlers=httpDriver.getHandlers();
		out.println("total "+handlers.size()+" requests");
		out.format(format,"#","METHOD","STATUS","START","CTN-LEN","SENT","RECEIVED","RATE","URL");	
		for(HttpHandler handler:handlers){
			String url=handler.request.getUrl();
			if(!cli.hasOption('f')){
				if(url.length()>=81){
					url=url.substring(0,80);
				}
			}
			float rate=(handler.received-handler.lastReceived)/1024.0f;
			out.format(format,
					i++,
					handler.request.getMethod(),
					handler.status,
					formatDate(new Date(handler.startTime)),
					String.format("%.1fK",(handler.contentLength/1024.0f)),
					String.format("%.1fK",(handler.sent/1024.0f)),
					String.format("%.1fK",(handler.received/1024.0f)),
					String.format("%.1fK/S",(rate)),
					url);
			handler.lastReceived=handler.received;
		};
    }
	
    //
    private void showDriverInfo(String args)throws Exception{
    	out.println(httpDriver.info());
    }
    
}
