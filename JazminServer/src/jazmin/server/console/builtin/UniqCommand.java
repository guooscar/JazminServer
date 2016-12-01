package jazmin.server.console.builtin;

import java.util.List;

import jazmin.util.IOUtil;


/**
 * 
 * @author yama 26 Dec, 2014
 */
public class UniqCommand extends ConsoleCommand {
	public UniqCommand() {
		super();
		id = "uniq";
		desc = "filter out repeated lines";
	}
	//
	@Override
	protected void run() throws Exception {
		if(!isPiped()){
			printHelp();
			return;
		}
		List<String>inputLines=IOUtil.getContentList(inStream);
		inputLines.stream().distinct().forEach(out::println);
	}
}
