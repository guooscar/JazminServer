/**
 * 
 */
package jazmin.server.console.repl;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author yama
 * 15 Jan, 2015
 */
public class TerminalInputStream extends InputStream{
	private boolean isBreak;
	InputStream in;
	public TerminalInputStream(InputStream in) {
		this.in=in;
	}
	
	@Override
	public int read() throws IOException {
		int r=in.read();
		//if user input ctrl-c break out
		isBreak=(r==ReadLine.ETX);
		return r;
	}
	@Override
	public int available() throws IOException {
		if(isBreak){
			throw new IOException("user break");
		}
		return in.available();
	}
	//

	/**
	 * @return the isBreak
	 */
	public boolean isBreak() {
		return isBreak;
	}

	/**
	 * @param isBreak the isBreak to set
	 */
	public void setBreak(boolean isBreak) {
		this.isBreak = isBreak;
	}
	
}
