package jazmin.server.console.builtin;

import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 * 
 * @author yama 26 Dec, 2014
 */
public class NlCommand extends ConsoleCommand {
	public NlCommand() {
		super(false);
		id = "nl";
		desc = "line numbering filter";	
	}
	//
	@Override
	protected void run() throws Exception {
		if(!isPiped()){
			printHelp();
			return;
		}
		BufferedReader br=new BufferedReader(new InputStreamReader(inStream));
		String t=null;
		int lineCount=1;
		while((t=br.readLine())!=null){
			out.format("%-10s %s\n",lineCount++,t);
		}
	}
	
}
