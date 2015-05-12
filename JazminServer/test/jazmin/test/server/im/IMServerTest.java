/**
 * 
 */
package jazmin.test.server.im;

import java.util.UUID;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.im.IMContext;
import jazmin.server.im.IMMessageServer;
import jazmin.server.im.IMResult;
import jazmin.server.im.IMService;
import jazmin.util.RandomUtil;

/**
 * @author yama
 * 8 May, 2015
 */
public class IMServerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoggerFactory.setLevel("INFO");
		//LoggerFactory.setFile("/tmp/xxx.log");
		LoggerFactory.disableConsoleLog();
		Jazmin.dispatcher.setCorePoolSize(16);
		Jazmin.dispatcher.setMaximumPoolSize(16);
		IMMessageServer server=new IMMessageServer();
		server.setMaxSessionRequestCountPerSecond(-1);
		server.registerService(new IMServerTest());
		//
		Jazmin.addServer(server);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
	//
	@IMService(id=0x01,syncOnSession=true)
	public void login(IMContext ctx,byte[]content)throws Exception{
		int length=RandomUtil.randomInt(100);
		byte empty[]=new byte[length];
		if(ctx.getSession().getPrincipal()==null){
			ctx.getServer().setPrincipal(ctx.getSession(),UUID.randomUUID().toString(), "");			
		}
		//Thread.sleep(RandomUtil.randomInt(25));
		ctx.setResult(new IMResult(empty));
	}
}
