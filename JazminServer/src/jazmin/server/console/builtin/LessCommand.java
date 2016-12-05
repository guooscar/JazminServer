package jazmin.server.console.builtin;

import java.util.List;
import java.util.concurrent.TimeUnit;

import jazmin.server.console.ascii.TerminalWriter;
import jazmin.server.console.repl.ReadLine;
import jazmin.util.IOUtil;


/**
 * 
 * @author yama 26 Dec, 2014
 */
public class LessCommand extends ConsoleCommand {
	private int currentLine;
	private boolean showLineNumber;
	//	
	public LessCommand() {
		super(false);
		id = "less";
		desc = "simple string viewer";
	}
	//
	@Override
	protected void run() throws Exception {
		if(!isPiped()){
			printHelp();
			return;
		}
		List<String>inputLines=IOUtil.getContentList(inStream);
		print(inputLines);
		while(true){
			if(stdin.available()==0){
				TimeUnit.MILLISECONDS.sleep(100);		
			}else{
				char opt=(char) stdin.read();
				if(opt=='j'){
					currentLine++;
				}
				if(opt=='k'){
					currentLine--;
				}
				if(opt=='g'){
					currentLine=0;
				}
				if(opt=='G'){
					currentLine=inputLines.size();
				}
				if(opt=='n'){
					showLineNumber=!showLineNumber;
				}
				//
				int maxScroll=(inputLines.size()-environment.getLines());
				//
				if(currentLine>=maxScroll){
					outStream.write(ReadLine.BEL);
					outStream.flush();
					currentLine=maxScroll;
				}
				if(currentLine<0){
					outStream.write(ReadLine.BEL);
					outStream.flush();
					currentLine=0;
				}
				//
				print(inputLines);
				//
				if(opt=='q'){
					out.println();
					break;
				}
			}
		}
	}
	//
	private void print(List<String>inputLines){
		TerminalWriter tw=new TerminalWriter(out);
		tw.cls();
		int lines=environment.getLines();
		boolean isEndLine=false;
		for(int i=currentLine;i<lines-1+currentLine;i++){
			if(i<inputLines.size()){
				if(showLineNumber){
					tw.fyellow();
					out.printf("%3s ",i+1);
					tw.reset();
				}
				out.println(inputLines.get(i));
			}else{
				isEndLine=true;
				out.println("~");
			}
		}
		//
		tw.bcyan();
		tw.fmagenta();
		String line=isEndLine?"END":(currentLine+1)+"";
		out.printf("%-10s%"+(environment.getColumns()-10)+"s",
				line,
				"j-scroll forward. k-scroll backward.g-scroll top.G-scroll end.n-toggle linenum.q-quit");
		tw.reset();
		//
		out.flush();
	}
}
