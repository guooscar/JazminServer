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
public class IndexFile implements Comparable<IndexFile>{
	private static Logger logger=LoggerFactory.get(IndexFile.class);
	//
	int index;
	File indexFile;
	FileChannel channel;
	private int capacity;
	private int size;
	MappedByteBuffer buffer;
	//
	int removedCount=0;
	//
	public IndexFile(String file,int capacity){
		indexFile=new File(file);
		this.capacity=capacity;	
	}
	//
	public int getRemovedCount(){
		return removedCount;
	}
	//
	public File getIndexFile(){
		return indexFile;
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
		}else{
			long fileLength=indexFile.length();
			long expectLength=IndexFileItem.FILE_ITEM_SIZE*capacity;
			if(fileLength!=IndexFileItem.FILE_ITEM_SIZE*capacity){
				throw new IllegalArgumentException(
						"index file length should be "+expectLength+" but "+fileLength+" "+indexFile);
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
		flush();
		try{
			channel.close();
		}catch (IOException e) {
			logger.catching(e);
		}
		logger.info("close index file {}",indexFile);
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
	public void delete(){
		close();
		boolean f=indexFile.delete();
		if(!f){
			logger.warn("can not delete index file "+indexFile.getAbsolutePath());
		}
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
	public void addItem(IndexFileItem item){
		if(size>=capacity){
			throw new IllegalArgumentException(indexFile.getName()+" index file full "+capacity+"/"+size);
		}
		long temp=index;
		item.uuid=temp<<32|size;
		int writeOffset=size*IndexFileItem.FILE_ITEM_SIZE;
		buffer.position(writeOffset);
		buffer.mark();
		buffer.put(IndexFileItem.get(item));
		size++;
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
		buffer.position(position+1+8+4+8+2);
		buffer.mark();
		buffer.put(flag);
	}
	//
	public void updateLastDelieverTime(int index,long time,short times){
		int position=index*IndexFileItem.FILE_ITEM_SIZE;
		buffer.position(position+1+8+4+8+2+1);
		buffer.mark();
		buffer.putLong(time);
		buffer.putShort(times);
	}
	//
	@Override
	public int compareTo(IndexFile o) {
		return this.index-o.index;
	}
	@Override
	public String toString() {
		return "IndexFile [index=" + index + ", indexFile=" + indexFile + "]";
	}
	
}
