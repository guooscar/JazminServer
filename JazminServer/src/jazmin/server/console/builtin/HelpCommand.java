package jazmin.server.console.builtin;
import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleServer;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class HelpCommand extends ConsoleCommand {
    public HelpCommand() {
    	super();
    	id="help";
    	desc="show command list.";
    }
    //
    @Override
    public void run(){
    	out.println("command list:");
    	ConsoleServer cs=Jazmin.getServer(ConsoleServer.class);
    	cs.getCommands().forEach(cmd->{
    		if(cmd instanceof ConsoleCommand){
    			ConsoleCommand cc=(ConsoleCommand)cmd;
    			out.printf("%-20s : %s\n", cc.getId(),cc.getDesc());		
    		}
    	}); 
    }
    
}
