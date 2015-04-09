package jazmin.server.console;
import java.net.InetSocketAddress;

import jazmin.core.Jazmin;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class WhoCommand extends ConsoleCommand {
   public WhoCommand() {
    	super();
    	id="who";
    	desc="show all login users.";
    }
    //
   	@Override
    public void run(){
    	out.println("user list:");
    	String format="%-30s %-20s\n";
    	ConsoleServer cs=Jazmin.getServer(ConsoleServer.class); 	
    	cs.sshServer.getActiveSessions().forEach(session->{
    		InetSocketAddress sa=(InetSocketAddress)session.getIoSession().getRemoteAddress();
        	String loginHostAddress=sa.getAddress().getHostAddress();
    		out.format(format, session.getUsername(),loginHostAddress+":"+sa.getPort());
    	});
    }

}
