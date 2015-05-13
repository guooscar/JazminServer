package jazmin.server.console.builtin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jazmin.server.console.ascii.TerminalWriter;

/**
 * 
 * @author yama 26 Dec, 2014
 */
public class GrepCommand extends ConsoleCommand {
	public GrepCommand() {
		super();
		id = "grep";
		desc = "selecting lines that match one or more patterns";
		addOption("c", false, "hightlight matches", null);
		addOption("v", false, "invert match", null);
	}
	//
	@Override
	protected void run() throws Exception {
		if(!isPiped()){
			return;
		}
		String args[]=cli.getArgs();
		if(args.length<1){
			printHelp();
		}
		grepE(args[0]);
	}
	//
	public void grepE(String args) throws Exception {
		Pattern p = Pattern.compile(args);
		BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
		String line = null;
		boolean printColor = cli.hasOption('c');
		boolean isRevert=cli.hasOption('v');
		while ((line = br.readLine()) != null) {
			int offset = 0;
			Matcher matcher = p.matcher(line);
			StringBuilder sb = new StringBuilder(line);
			boolean matches = false;
			while (matcher.find()) {
				matches = true;
				if (printColor) {
					sb.insert(matcher.start() + offset, TerminalWriter.FRED);
					offset += TerminalWriter.FRED.length();
					sb.insert(matcher.end() + offset, TerminalWriter.RESET);
					offset += TerminalWriter.RESET.length();
				}
			}
			//
			if (matches&&!isRevert) {
				out.println(sb.toString());
			}else if(!matches&&isRevert){
				out.println(sb.toString());	
			}
		}
	}
}
