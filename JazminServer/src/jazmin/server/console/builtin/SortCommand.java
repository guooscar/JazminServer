package jazmin.server.console.builtin;

import java.util.Comparator;
import java.util.List;

import jazmin.util.IOUtil;


/**
 * 
 * @author yama 26 Dec, 2014
 */
public class SortCommand extends ConsoleCommand {
	public SortCommand() {
		super();
		id = "sort";
		desc = "sort lines of text";
		addOption("k", true, "key column index", null);
		addOption("n", false, "treat key column as number", null);
		addOption("r", false, "reverse output", null);
		addOption("d", true, "delim char", null);
	}
	//
	@Override
	protected void run() throws Exception {
		if(!isPiped()){
			printHelp();
			return;
		}
		int keyIndex=0;
		try{
			if(cli.hasOption('k')){
				keyIndex=(Integer.valueOf(cli.getOptionValue('k')));
			}
		}catch(Exception e){}
		String splitChar="\\s+";
		if(cli.hasOption('d')){
			splitChar=cli.getOptionValue('d');
		}
		boolean isTreatAsNumber=cli.hasOption('n');
		boolean isReverse=cli.hasOption('r');
		//
		List<String>inputLines=IOUtil.getContentList(inStream);
		inputLines.sort(new SortComparter(
				keyIndex,
				isTreatAsNumber,
				isReverse,
				splitChar));
		inputLines.forEach(out::println);
	}
	//
	private static class SortComparter implements Comparator<String>{
		int keyIndex;
		boolean number;
		boolean isReverse;
		String splitChar;
		public SortComparter(
				int keyIndex,
				boolean number,
				boolean isReverse,
				String splitChar) {
			this.keyIndex=keyIndex;
			this.number=number;
			this.isReverse=isReverse;
			this.splitChar=splitChar;
		}
		@Override
		public int compare(String s1, String s2) {
			String ss1[]=s1.split(splitChar);
			String ss2[]=s2.split(splitChar);
			int result=0;
			if(keyIndex<ss1.length&&keyIndex<ss2.length){
				if(number){
					try{
						int n1=Integer.valueOf(ss1[keyIndex]);
						int n2=Integer.valueOf(ss2[keyIndex]);
						result=n1-n2;
					}catch(Exception e){
						result=0;
					}
				}else{
					result= ss1[keyIndex].compareTo(ss2[keyIndex]);
				}
			}
			return isReverse?-result:result;
		}
	}
}
