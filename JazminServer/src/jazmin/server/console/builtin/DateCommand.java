package jazmin.server.console.builtin;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 
 * @author yama 26 Dec, 2014
 */
public class DateCommand extends ConsoleCommand {
	public DateCommand() {
		super(false);
		id = "date";
		desc = "display date and time";	
	}
	//
	@Override
	protected void run() throws Exception {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		out.println(sdf.format(new Date()));
	}
	
}
