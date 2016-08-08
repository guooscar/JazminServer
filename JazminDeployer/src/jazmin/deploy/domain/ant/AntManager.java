/**
 * 
 */
package jazmin.deploy.domain.ant;

import java.util.ArrayList;
import java.util.List;

import jazmin.deploy.domain.OutputListener;


/**
 * @author yama
 * 25 Dec, 2015
 */
public class AntManager {
	String antPath="/usr/local/bin/ant";
	String commonLib;
	OutputListener outputListener;
	public AntManager(String antPath){
		this.antPath=antPath;
	}
	//
	
	//
	public int antCall(String target,String buildFile)throws Exception{
		List<String>commands=new ArrayList<String>();
		commands.add(antPath);
		commands.add("-f");
		commands.add(buildFile);
		if(commonLib!=null){
			commands.add("-lib");
			commands.add(commonLib);
		}
		commands.add(target);
		ProcessBuilder pb=new ProcessBuilder(commands);
		pb.redirectErrorStream(true);
		Process process=pb.start();
		InputStreamReader is=new InputStreamReader(process);
		is.setOutputListener(outputListener);
		is.startThread();
		return process.waitFor();
	}

	/**
	 * @return the commonLib
	 */
	public String getCommonLib() {
		return commonLib;
	}

	/**
	 * @param commonLib the commonLib to set
	 */
	public void setCommonLib(String commonLib) {
		this.commonLib = commonLib;
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
}
