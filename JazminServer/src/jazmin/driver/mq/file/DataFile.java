/**
 * 
 */
package jazmin.driver.mq.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
	FileChannel readChannel;
	FileChannel writeChannel;
	FileInputStream fis;
	FileOutputStream fos;
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
			fis=new FileInputStream(dataFile);
			readChannel=fis.getChannel();
			//
			fos=new FileOutputStream(dataFile,true);
			writeChannel=fis.getChannel();
		}catch (Exception e) {
			throw new IllegalStateException(e);
		}	
	}
	//
	public void flush(){
		try {
			writeChannel.force(true);
		} catch (IOException e) {
			logger.catching(e);
		}
	}
	//
	public void close(){
		flush();
		try{
			writeChannel.close();
			readChannel.close();
			fis.close();
			fos.close();
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
			readChannel.position(offset);;
			readChannel.read(headBuffer);
			item.magic=headBuffer.get();
			item.payloadType=headBuffer.get();
			item.payloadLength=headBuffer.getShort();
			item.payload=new byte[item.payloadLength];
			ByteBuffer bodyBuffer=ByteBuffer.wrap(item.payload);
			readChannel.read(bodyBuffer);
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
			writeChannel.position(writeChannel.size());
			offset=writeChannel.position();
			buffer=ByteBuffer.wrap(new byte[DataItem.HEAD_LENGTH+item.payload.length]);
			buffer.put(DataItem.MAGIC);
			buffer.put(item.payloadType);
			buffer.putShort(item.payloadLength);
			buffer.put(item.payload);
			return offset;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
