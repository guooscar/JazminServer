package jazmin.server.console.builtin;
import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleServer;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class ManCommand extends ConsoleCommand {
    public ManCommand() {
    	super(false);
    	id="man";
    	desc="format and display the manual pages";
    }
    //
    @Override
    public void run(){
    	if(cli.getArgs().length<1){
    		out.println("What manual page do you want?");
    		return;
    	}
    	ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
    	ConsoleCommand cmd=cs.getCommand(cli.getArgs()[0]);
    	if(cmd==null){
    		out.println("No manual entry for "+cli.getArgs()[0]);
    		return;
    	}
    	out.println(cmd.getHelpInfo());
    }
    
}
