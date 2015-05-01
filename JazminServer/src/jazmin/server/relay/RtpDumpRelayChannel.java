/**
 * 
 */
package jazmin.server.relay;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
	void sendData(ByteBuf buffer) throws Exception{
		packetSentCount++;
		byteSentCount+=buffer.capacity();
		ByteBuf buf= Unpooled.copiedBuffer(buffer);
		byte array[]=buf.array();
		RtpPacket pkg=RtpPacket.decode(array);
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
	public void close() throws Exception {
		super.close();
		if(outputStream!=null){
			outputStream.close();
		}
	}
}
