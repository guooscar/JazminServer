package jazmin.server.console;



/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class EchoCommand extends ConsoleCommand {
   public EchoCommand() {
    	super();
    	id="echo";
    	desc="echo input string";
    }
    //
   	@Override
    public void run()throws Exception{
   		if(!isPiped()){
			return;
		}
    	while(inStream.available()>0){
    		outStream.write(inStream.read());
    	}
    }
}
