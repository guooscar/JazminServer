/**
 * 
 */
package jazmin.server.relay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jazmin.codec.rtp.RtpPacket;
import jazmin.codec.rtp.RtpPayloadType;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class RtpDumpRelayChannel extends RelayChannel{
	private static Logger logger=LoggerFactory.get(RtpDumpRelayChannel.class);
	private File dumpFile;
	private OutputStream outputStream;
	//
	public RtpDumpRelayChannel() {
		super();
	}
	//
	public RtpDumpRelayChannel(String filePath,boolean append) {
		super();
		this.dumpFile=new File(filePath);
		try {
			if(!dumpFile.exists()){
				if(dumpFile.createNewFile()){
					outputStream=new FileOutputStream(dumpFile, append);			
				}
			}
		}catch (IOException e) {
			logger.catching(e);
		}
		if(outputStream==null){
			logger.error("can not create dump file:"+filePath);
		}
	}
	//
	@Override
	public void write(byte [] buffer) throws Exception{
		packetSentCount++;
		byteSentCount+=buffer.length;
		RtpPacket pkg=RtpPacket.decode(buffer);
		if(pkg.getVersion()!=RtpPacket.V2){
			//bad package ignore
			return;
		}
		if(outputStream!=null){
			outputStream.write(pkg.getDataAsArray());
		}
		if(logger.isDebugEnabled()){
			logger.debug("\nRtpPackage #{}\n{}\n{}",
					packetSentCount,pkg,
					RtpPayloadType.get(pkg.getPayloadType()));
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
		if(outputStream!=null){
			outputStream.close();
		}
	}
}
