package jazmin.server.console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.console.repl.ReadLineEnvironment;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class ConsoleCommand{
	protected static LinkedList<String>commandHistory=new LinkedList<String>();
	//
	private static Logger logger=LoggerFactory.get(ConsoleCommand.class);
	//
	public static interface OptionRunnable{
		void run(String input)throws Exception;
	}
	//
	public static class OptionDefine{
		String name;
		boolean hasArgs;
		String desc;
		OptionRunnable runnable;
	}
	//
    protected PrintWriter out;
    protected PrintWriter err;
    protected InputStream stdin;
    protected InputStream inStream;
    protected OutputStream outStream;
    protected OutputStream errStream;
    protected ReadLineEnvironment environment;
    protected String[] args;
    protected String id;
    protected String desc;
    protected CommandLine cli;
    private volatile boolean finished;
    //
    private Options options;
    //
    private Map<String,OptionDefine>commandOptionMap;
    //
    public boolean isPiped(){
    	return (inStream instanceof PipedInputStream);
    }
    //
    
    //
    public ConsoleCommand() {
    	finished=false;
    	options=new Options();
    	commandOptionMap=new HashMap<String,OptionDefine>();
	}
    /**
	 * @return the finished
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * @param finished the finished to set
	 */
	public void setFinished(boolean f) {
		this.finished=f;
	}

	//
    public void addOption(String id,boolean hasArgs,String desc,OptionRunnable runnable){
    	OptionDefine od=new OptionDefine();
    	od.name=id;
    	od.hasArgs=hasArgs;
    	od.desc=desc;
    	od.runnable=runnable;
    	commandOptionMap.put(id, od);
    	options.addOption(id, hasArgs, desc);
    }
    //
    public String getId() {
        return id;
    }
    public String getDesc(){
    	return desc;
    }
    //
    public void printHelp(){
    	out.println(getHelpInfo());
    }
    //
    public String getHelpInfo(){
    	StringWriter sw=new StringWriter();
    	PrintWriter pw=new PrintWriter(sw);
    	//Stringwr
      	HelpFormatter formatter = new HelpFormatter();
    	formatter.printHelp(pw,80,id,desc,options,3,3,"",true);
    	pw.flush();
    	pw.close();
    	return sw.toString();
    }
    //
    protected void run() throws Exception{
    	boolean hit=false;
    	for(Entry<String, OptionDefine> e:commandOptionMap.entrySet()){
    		OptionDefine od=e.getValue();
    		String optionName=e.getKey();
    		if(cli.hasOption(optionName)&&od.runnable!=null){
    			hit=true;
    			String args=null;
    			if(e.getValue().hasArgs){
    				args=cli.getOptionValue(e.getKey());
    			}
    			e.getValue().runnable.run(args);	
    		}
    	}
    	if(!hit){
    		printHelp();
    	}
    }
    //
    public final void run(
    		InputStream stdin, 
    		InputStream in, 
    		OutputStream out, 
    		OutputStream err, 
    		ReadLineEnvironment environment, 
    		String line,
    		String[] args){
    	try{
    		this.stdin=stdin;
	    	this.inStream=in;
	    	this.outStream=out;
	    	this.errStream=err;
	    	this.out = new PrintWriter(outStream);
	    	this.err = new PrintWriter(errStream);
	    	this.environment = environment;
	    	this.args = args;
        	GnuParser parser=new GnuParser();
			this.cli = parser.parse(options,args);
			commandHistory.add(line);
			if(commandHistory.size()>500){
				commandHistory.removeFirst();
			}
			run();
		} catch (Throwable e) {
			if(!(e instanceof IOException)){
				logger.catching(e);		
			}
			this.err.println(e.getMessage());
		}finally{
			try{
				this.out.flush();
				this.out.close();
				this.err.flush();
				this.err.close();
			}catch(Exception e){}
		}
    }
    //
    protected String formatDate(Date date){
    	if(date==null){
    		return "null";
    	}
    	SimpleDateFormat sdf=new SimpleDateFormat("MM/dd HH:mm:ss");
    	return  sdf.format(date);
    }
    //
    protected void printLine(char c,int width){
    	for(int i=0;i<width;i++){
    		out.print(c);
    	}
    	out.print("\n");
    }
}
