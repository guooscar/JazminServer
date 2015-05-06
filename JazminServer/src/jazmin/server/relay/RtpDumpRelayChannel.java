/**
 * 
 */
package jazmin.server.relay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

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
	private RtpPacket rtpPacket;
	//
	public RtpDumpRelayChannel(RelayServer server) {
		super(server);
		rtpPacket=new RtpPacket(RtpPacket.RTP_PACKET_MAX_SIZE, true);
	}
	//
	public RtpDumpRelayChannel(RelayServer server,String filePath,boolean append) {
		super(server);
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
	public void dataFromRelay(RelayChannel channel,byte [] buffer) throws Exception{
		super.dataFromRelay(channel, buffer);
		ByteBuffer rtpBuffer=rtpPacket.getBuffer();
		rtpBuffer.clear();
		rtpBuffer.put(buffer, 0, buffer.length);
		rtpBuffer.flip();
		if(rtpPacket.getVersion()!=RtpPacket.VERSION){
			//bad package ignore
			return;
		}
		if(outputStream!=null){
			outputStream.write(buffer);
		}
		if(logger.isDebugEnabled()){
			logger.debug("\nRtpPackage #{}\n{}\n{}",
					packetRelayCount,rtpBuffer,
					RtpPayloadType.get(rtpPacket.getPayloadType()));
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
