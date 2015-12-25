/**
 * 
 */
package jazmin.deploy.domain.ant;

import java.io.InputStream;

import jazmin.deploy.domain.OutputListener;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama 26 Dec, 2015
 */
public class InputStreamReader {
	private static Logger logger = LoggerFactory.get(InputStreamReader.class);
	private Process process;
	private OutputListener outputListener;
	public InputStreamReader(Process process) {
		this.process = process;
	}

	/**
	 * @return the outputListener
	 */
	public OutputListener getOutputListener() {
		return outputListener;
	}

	/**
	 * @param outputListener the outputListener to set
	 */
	public void setOutputListener(OutputListener outputListener) {
		this.outputListener = outputListener;
	}

	//
	public void startThread() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				InputStream is = process.getInputStream();
				try {
					int n = 0;
					byte[] buffer = new byte[1024];
					while (-1 != (n = is.read(buffer))) {
						String s = new String(buffer, 0, n);
						sendMessage(s);
					}
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		});
		t.start();
	}

	//
	private void sendMessage(String s) {
		if (logger.isDebugEnabled()) {
			logger.debug(s);
		}
		if(outputListener!=null){
			outputListener.onOutput(s);
		}
	}
}
