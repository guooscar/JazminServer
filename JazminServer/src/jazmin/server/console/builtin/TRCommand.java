package jazmin.server.console.builtin;

import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 * 
 * @author yama 26 Dec, 2014
 */
public class TRCommand extends ConsoleCommand {
	public TRCommand() {
		super(false);
		id = "tr";
		desc = "translate characters";	
		addOption("u", false, "uppercase", null);
		addOption("l", false, "lowercase", null);
		addOption("e", false, "delete empty line", null);
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
		boolean uppercase=cli.hasOption('u');
		boolean lowercase=cli.hasOption('l');
		boolean emptyLine=cli.hasOption('e');
		
		while((t=br.readLine())!=null){
			if(emptyLine&&t.trim().isEmpty()){
				continue;
			}
			if(uppercase){
				out.println(t.toUpperCase());
				continue;
			}
			if(lowercase){
				out.println(t.toLowerCase());
				continue;
			}
			out.println(t);
		}
	}
	
}
