package jazmin.server.console;
import jazmin.core.Jazmin;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class ManCommand extends ConsoleCommand {
    public ManCommand() {
    	super();
    	id="man";
    	desc="format and display the manual pages.";
    }
    //
    @Override
    public void run(){
    	if(cli.getArgs().length<1){
    		out.println("What manual page do you want?");
    		return;
    	}
    	ConsoleServer cs=Jazmin.server(ConsoleServer.class);
    	ConsoleCommand cmd=cs.getCommand(cli.getArgs()[0]);
    	if(cmd==null){
    		out.println("No manual entry for "+cli.getArgs()[0]);
    		return;
    	}
    	out.println(cmd.getHelpInfo());
    }
    
}
