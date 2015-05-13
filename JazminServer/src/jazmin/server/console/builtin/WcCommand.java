package jazmin.server.console.builtin;

import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 * 
 * @author yama 26 Dec, 2014
 */
public class WcCommand extends ConsoleCommand {
	public WcCommand() {
		super();
		id = "wc";
		desc = "line count";
	}
	//
	@Override
	protected void run() throws Exception {
		if(!isPiped()){
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
		int count=0;
		while ((br.readLine()) != null) {
			count++;
		}
		out.println(count);
	}
	
}
