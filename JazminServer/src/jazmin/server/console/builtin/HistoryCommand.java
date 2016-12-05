package jazmin.server.console.builtin;



/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class HistoryCommand extends ConsoleCommand {
    public HistoryCommand() {
    	super(false);
    	id="history";
    	desc="show command history.";
    	addOption("c", false, "clear history",null);
    }
    //
    @Override
    public void run(){
    	if(cli.hasOption('c')){
    		consoleServer.clearHistory();
    	}else{
    		consoleServer.getCommandHistory().forEach(cmd->out.printf("%s\n", cmd)); 
    	}
    }
}
