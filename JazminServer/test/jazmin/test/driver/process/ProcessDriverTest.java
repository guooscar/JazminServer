/**
 * 
 */
package jazmin.test.driver.process;

import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.driver.process.ProcessDriver;
import jazmin.driver.process.ProcessInfo;
import jazmin.driver.process.ProcessLifecycleListener;
import jazmin.server.console.ConsoleServer;

/**
 * @author yama
 *
 */
public class ProcessDriverTest {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ProcessDriver pd = new ProcessDriver();
		Jazmin.addDriver(pd);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();

		pd.setLifecycleListener(new ProcessLifecycleListener() {

			@Override
			public void processStarted(ProcessInfo pi) throws Exception {
				System.out.println(pi.getId() + " started");
			}

			@Override
			public void processDestroyed(ProcessInfo pi) throws Exception {
				System.out.println(pi.getId() + " destroyed");
			}
		});
		ProcessInfo pi = pd.start("test", new String[] { "notepad.exe" });
		Jazmin.schedule(() -> {
			pi.getProcess().destroy();
		}, 5, TimeUnit.SECONDS);

	}

}
