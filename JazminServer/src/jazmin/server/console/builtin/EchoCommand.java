package jazmin.server.console.builtin;



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
   		if(rawInput.length()>=5){
   			//trim command name
   			rawInput=rawInput.substring(5,rawInput.length());
   		}else{
   			rawInput="";
   		}
   		out.println(rawInput);
    }
}
