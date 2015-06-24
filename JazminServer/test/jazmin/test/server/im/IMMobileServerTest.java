/**
 * 
 */
package jazmin.test.server.im;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.im.IMContext;
import jazmin.server.im.IMMessageServer;
import jazmin.server.im.IMResult;
import jazmin.server.im.IMService;

/**
 * @author yama
 * 8 May, 2015
 */
public class IMMobileServerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoggerFactory.setLevel("ALL");
		Jazmin.dispatcher.setCorePoolSize(16);
		Jazmin.dispatcher.setMaximumPoolSize(16);
		IMMessageServer server=new IMMessageServer();
		server.setEnableMobile(true);
		server.setMaxSessionRequestCountPerSecond(-1);
		server.registerService(new IMMobileServerTest());
		//
		Jazmin.addServer(server);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
	//
	@IMService(mobileId="test")
	public void login(IMContext ctx,byte[]content)throws Exception{
		String s=new String(content);
		System.err.println(s);
		ctx.setResult(new IMResult("123333333333333".getBytes()));
	}
}
