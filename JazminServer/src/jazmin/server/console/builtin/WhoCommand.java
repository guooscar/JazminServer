package jazmin.server.console.builtin;
import jazmin.core.Jazmin;
import jazmin.server.console.ConsoleServer;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class WhoCommand extends ConsoleCommand {
   public WhoCommand() {
    	super(true);
    	id="who";
    	desc="show all login users.";
    }
    //
   	@Override
    public void run(){
    	out.println("user list:");
    	String format="%-30s %-20s\n";
    	ConsoleServer cs=Jazmin.getServer(ConsoleServer.class); 	
    	cs.getConsoleSession().forEach(session->{
    		out.format(format, session.user,session.remoteHost+":"+session.remotePort);
    	});
    }

}
