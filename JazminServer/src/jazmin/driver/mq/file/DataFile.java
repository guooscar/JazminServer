/**
 * 
 */
package jazmin.driver.mq.file;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class DataFile {
	private static Logger logger=LoggerFactory.get(IndexFile.class);
	//
	File dataFile;
	FileChannel channel;
	public DataFile(String file){
		dataFile=new File(file);
	
	}
	public void open(){
		if(!dataFile.exists()){
			try {
				dataFile.createNewFile();
				logger.info("create data file {}",dataFile.getAbsolutePath());
			} catch (IOException e) {
				throw new IllegalArgumentException("can not create data file:"+dataFile.getAbsolutePath(),e);
			}
		}
		try{
			channel=FileChannel.open(dataFile.toPath(),
					StandardOpenOption.READ,
					StandardOpenOption.WRITE);
		}catch (Exception e) {
			throw new IllegalStateException(e);
		}	
	}
	//
	public void flush(){
		try {
			channel.force(true);
		} catch (IOException e) {
			logger.catching(e);
		}
	}
	//
	public void close(){
		flush();
		try{
			channel.close();
		}catch (IOException e) {
			logger.catching(e);
		}
	}
	//
	public void delete(){
		close();
		boolean f=dataFile.delete();
		if(!f){
			logger.warn("can not delete data file "+dataFile.getAbsolutePath());
		}
	}
	//
	public DataItem get(int offset){
		DataItem item=new DataItem();
		byte headBytes[]=new byte[DataItem.HEAD_LENGTH];
		ByteBuffer headBuffer=ByteBuffer.wrap(headBytes);
		try{
			channel.position(offset);;
			channel.read(headBuffer);
			item.magic=headBuffer.get();
			item.payloadType=headBuffer.get();
			item.payloadLength=headBuffer.getShort();
			item.payload=new byte[item.payloadLength];
			ByteBuffer bodyBuffer=ByteBuffer.wrap(item.payload);
			channel.read(bodyBuffer);
			return item;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	//
	public long append(DataItem item){
		long offset=0;
		ByteBuffer buffer=null;
		try{
			channel.position(channel.size());
			offset=channel.position();
			buffer=ByteBuffer.wrap(new byte[DataItem.HEAD_LENGTH+item.payload.length]);
			buffer.put(DataItem.MAGIC);
			buffer.put(item.payloadType);
			buffer.putShort(item.payloadLength);
			buffer.put(item.payload);
			channel.force(true);
			return offset;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
