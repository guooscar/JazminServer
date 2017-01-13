package jazmin.server.console.builtin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.TreeSet;


/**
 * 
 * @author yama 26 Dec, 2014
 */
public class CutCommand extends ConsoleCommand {
	public CutCommand() {
		super(false);
		id = "cut";
		desc = "cut out selected portions of each line";
		addOption("d", true, "delim char", null);
		addOption("f", true, "the list specifies fields, separated in the input by the field delimiter character", null);
		
	}
	//
	@Override
	protected void run() throws Exception {
		if(!isPiped()){
			printHelp();
			return;
		}
		String fields="";
		if(cli.hasOption('f')){
			fields=cli.getOptionValue("f");
		}
		String splitChar="\\s+";
		if(cli.hasOption('d')){
			splitChar=cli.getOptionValue('d');
		}
		//
		String qq[]=fields.split(",");
		TreeSet<Integer>fieldSet=new TreeSet<>();
		for(int i=0;i<qq.length;i++){
			try{
				fieldSet.add(Integer.valueOf(qq[i]));
			}catch (Exception e) {
				out.print("bad field value:"+qq[i]);
				return;
			}
		}
		//
		BufferedReader br=new BufferedReader(new InputStreamReader(inStream));
		String t=null;
		while((t=br.readLine())!=null){
			String spliteArray[]=t.split(splitChar);
			for(int i=0;i<spliteArray.length;i++){
				if(fieldSet.contains(i)){
					out.print(spliteArray[i]);
					out.print("\t");
				}
			}
			out.println();
		}
	}
}
