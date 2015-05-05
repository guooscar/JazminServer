/**
 * 
 */
package jazmin.server.relay;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.HexDump;

/**
 * @author yama
 *
 */
public class HexDumpRelayChannel extends RelayChannel{
	private static Logger logger=LoggerFactory.get(HexDumpRelayChannel.class);
	private File dumpFile;
	private BufferedWriter bufferedWriter;
	//
	public HexDumpRelayChannel() {
		super();
	}
	//
	public HexDumpRelayChannel(String filePath,boolean append) {
		super();
		this.dumpFile=new File(filePath);
		try {
			if(!dumpFile.exists()){
				if(dumpFile.createNewFile()){
					bufferedWriter=new BufferedWriter(new FileWriter(dumpFile,append));			
				}
			}
		}catch (IOException e) {
			logger.catching(e);
		}
		if(bufferedWriter==null){
			logger.error("can not create dump file:"+filePath);
		}
	}
	//
	@Override
	public void dataFromRelay(RelayChannel channel,byte buffer[]) throws Exception{
		super.dataFromRelay(channel, buffer);
		String output="#"+packetRelayCount+"\n"+HexDump.dumpHexString(buffer);
		if(bufferedWriter!=null){
			bufferedWriter.write(output+"\n");
		}
		if(logger.isDebugEnabled()){
			logger.debug("\n"+output);
		}
	}

	/**
	 * @return the dumpFile
	 */
	public File getDumpFile() {
		return dumpFile;
	}
	//
	@Override
	public String getInfo() {
		return "dump["+dumpFile+"]";
	}
	//
	@Override
	public void closeChannel() throws Exception {
		super.closeChannel();
		if(bufferedWriter!=null){
			bufferedWriter.close();
		}
	}
}
