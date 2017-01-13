package jazmin.server.console.builtin;

import java.time.Duration;
import java.util.Date;

import jazmin.core.Jazmin;


/**
 * 
 * @author yama 26 Dec, 2014
 */
public class UpTimeCommand extends ConsoleCommand {
	public UpTimeCommand() {
		super(false);
		id = "uptime";
		desc = "show how long system has been running";	
	}
	//
	@Override
	protected void run() throws Exception {
		Date endTime=new Date();
		Duration d=Duration.between(Jazmin.getStartTime().toInstant(),endTime.toInstant());
		out.println(d.toString());
	}
	
}
