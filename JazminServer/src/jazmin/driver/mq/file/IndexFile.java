/**
 * 
 */
package jazmin.driver.mq.file;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class IndexFile {
	private static Logger logger=LoggerFactory.get(IndexFile.class);
	//
	File indexFile;
	FileChannel channel;
	private int capacity;
	private int size;
	MappedByteBuffer buffer;
	//
	public IndexFile(String file,int capacity){
		indexFile=new File(file);
		this.capacity=capacity;
	}
	//
	public void open(){
		if(!indexFile.exists()){
			try {
				indexFile.createNewFile();
				logger.info("create index file {}",indexFile.getAbsolutePath());
			} catch (IOException e) {
				throw new IllegalArgumentException("can not create index file:"+indexFile.getAbsolutePath(),e);
			}
		}
		try{
			channel=FileChannel.open(indexFile.toPath(),StandardOpenOption.READ,StandardOpenOption.WRITE);
			buffer = channel.map(
					FileChannel.MapMode.READ_WRITE, 
					0, 
					IndexFileItem.FILE_ITEM_SIZE*capacity);
		}catch (Exception e) {
			throw new IllegalStateException(e);
		}	
		checkSize();
	}
	//
	public void close(){
		try {
			channel.force(true);
		} catch (IOException e) {
			logger.catching(e);
		}
	}
	//
	private void checkSize(){
		long totalLength=capacity*IndexFileItem.FILE_ITEM_SIZE;
		byte itemBytes[]=new byte[IndexFileItem.FILE_ITEM_SIZE];
		for(int offset=0;offset<totalLength;offset+=IndexFileItem.FILE_ITEM_SIZE){
			buffer.get(itemBytes);
			IndexFileItem item=IndexFileItem.get(itemBytes);
			if(item.dataOffset!=0){
				size++;
			}else{
				break;
			}
		}
		logger.info("check index file size {} {}",indexFile.getAbsolutePath(),size);
	}
	//
	public int size(){
		return size;
	}
	//
	public boolean addItem(IndexFileItem item){
		synchronized (buffer) {
			if(size>=capacity){
				return false;
			}
			int writeOffset=size*IndexFileItem.FILE_ITEM_SIZE;
			buffer.position(writeOffset);
			buffer.put(IndexFileItem.get(item));
			size++;
			return true;
		}
	}
}
