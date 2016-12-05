package jazmin.server.console.builtin;

import java.util.List;

import jazmin.util.IOUtil;


/**
 * 
 * @author yama 26 Dec, 2014
 */
public class TailCommand extends ConsoleCommand {
	public TailCommand() {
		super(false);
		id = "tail";
		desc = "display last line of strings";
		addOption("n", true, "line number", null);
		addOption("v", false, "invert select", null);	
	}
	//
	@Override
	protected void run() throws Exception {
		if(!isPiped()){
			printHelp();
			return;
		}
		List<String>inputLines=IOUtil.getContentList(inStream);
		int number=inputLines.size();
		try{
			if(cli.hasOption('n')){
				number=Integer.valueOf(cli.getOptionValue('n'));
			}
		}catch(Exception e){}
		//
		int start=inputLines.size()-number;
		if(start<0){
			start=0;
		}
		//
		if(cli.hasOption('v')){
			//skip
			for(int i=0;i<inputLines.size()-number;i++){
				out.println(inputLines.get(i));
			}
		}else{
			for(int i=start;i<inputLines.size();i++){
				out.println(inputLines.get(i));
			}
		}
	}
	
}
