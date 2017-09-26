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
	int index;
	File indexFile;
	FileChannel channel;
	private int capacity;
	private int size;
	MappedByteBuffer buffer;
	//
	DataFile dataFile;
	//
	public IndexFile(String file,int capacity){
		indexFile=new File(file);
		this.capacity=capacity;	
	}
	//
	public static IndexFile get(String file,int capacity){
		int index=getFileIndex(file);
		if(index==-1){
			logger.warn("bad index file: {}",file);
		}
		IndexFile indexFile=new IndexFile(file, capacity);
		indexFile.index=index;
		indexFile.open();
		return indexFile;
	}
	//
	public static int getFileIndex(String file){
		int lastIndex1=file.lastIndexOf('-');
		int lastIndex2=file.lastIndexOf('.');
		if(lastIndex1>=lastIndex2){
			return -1;
		}
		int index=0;
		try{
			index=Integer.parseInt(file.substring(lastIndex1+1, lastIndex2));
		}catch(Exception e){
			return -1;
		}
		return index;
	}
	//
	public int getIndex(){
		return index;
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
		//
		dataFile=new DataFile(indexFile.getAbsolutePath()+".data");
		dataFile.open();
	}
	//
	public void close(){
		flush();
		try{
			channel.close();
		}catch (IOException e) {
			logger.catching(e);
		}
		dataFile.close();
	}
	//
	public void flush(){
		try {
			channel.force(true);
		} catch (IOException e) {
			logger.catching(e);
		}
		dataFile.flush();
	}
	//
	public void delete(){
		close();
		boolean f=indexFile.delete();
		if(!f){
			logger.warn("can not delete index file "+indexFile.getAbsolutePath());
		}
		dataFile.delete();
	}
	//
	private void checkSize(){
		long totalLength=capacity*IndexFileItem.FILE_ITEM_SIZE;
		for(int offset=0;offset<totalLength;offset+=IndexFileItem.FILE_ITEM_SIZE){
			buffer.position(offset);
			buffer.mark();
			byte magic=buffer.get();
			if(magic==IndexFileItem.MAGIC){
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
	public int space(){
		return capacity-size;
	}
	//
	public boolean addItem(IndexFileItem item){
		if(size>=capacity){
			return false;
		}
		int writeOffset=size*IndexFileItem.FILE_ITEM_SIZE;
		buffer.position(writeOffset);
		buffer.mark();
		buffer.put(IndexFileItem.get(item));
		size++;
		return true;
	}
	//
	public IndexFileItem getItem(int index){
		int position=index*IndexFileItem.FILE_ITEM_SIZE;
		buffer.position(position);
		buffer.mark();
		byte contentBytes[]=new byte[IndexFileItem.FILE_ITEM_SIZE];
		buffer.get(contentBytes);
		return IndexFileItem.get(contentBytes);
	}
	//
	public void updateFlag(int index,byte flag){
		int position=index*IndexFileItem.FILE_ITEM_SIZE;
		buffer.position(position+1+32+8+2);
		buffer.mark();
		buffer.put(flag);
	}
	//
	public void updateLastDelieverTime(int index,long time,short times){
		int position=index*IndexFileItem.FILE_ITEM_SIZE;
		buffer.position(position+1+32+8+2+1);
		buffer.mark();
		buffer.putLong(time);
		buffer.putShort(times);
	}
	
}
