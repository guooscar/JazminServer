/**
 * 
 */
package jazmin.example.rpc;

import jazmin.core.app.AutoWireCompleted;
import jazmin.core.app.AutoWired;

/**
 * @author yama
 *
 */
public class DemoRpcServiceImpl implements DemoRpcService{
	@AutoWired
	EchoService echoService;
	//
	@AutoWireCompleted
	private void setup(){
		//do something when echoService setted
	}
	//
	@Override
	public String echo(String input) {
		return echoService.doEcho(input);
	}
	
}
