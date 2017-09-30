/**
 * 
 */
package jazmin.driver.mq.file;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;

import jazmin.driver.mq.Message;
import jazmin.driver.mq.MessageQueueDriver;
import jazmin.driver.mq.TopicChannel;
import jazmin.driver.mq.TopicQueue;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class FileTopicChannel extends TopicChannel{
	private static Logger logger=LoggerFactory.get(FileTopicChannel.class);
	//
	LinkedList<IndexFile>indexSegmentFiles;
	LinkedList<IndexFile>takeIndexSegmentFiles;
	private int currentIndex=0;
	//
	public FileTopicChannel(TopicQueue queue) {
		super(queue);
		indexSegmentFiles=new LinkedList<IndexFile>();
		takeIndexSegmentFiles=new LinkedList<IndexFile>();
	}
	//
	void load(File files[]){
		for(File f:files){
			logger.info("load index file:{}",f.getAbsoluteFile());
			FileTopicQueue fq=(FileTopicQueue) queue;
			IndexFile file=IndexFile.get(f.getAbsolutePath(),fq.indexFileCapacity);
			if(file!=null){
				indexSegmentFiles.add(file);
				takeIndexSegmentFiles.add(file);
			}
		}
		//
		Collections.sort(indexSegmentFiles);
		Collections.sort(takeIndexSegmentFiles);
	}
	//
	private IndexFile newIndexFile(int index,short subscriber){
		FileTopicQueue fq=(FileTopicQueue) queue;
		File newIdxFile=new File(fq.workDirFile.getAbsoluteFile(),subscriber+"-"+index+".index");
		IndexFile file=new IndexFile(newIdxFile.getAbsolutePath(), fq.indexFileCapacity);
		file.index=index;
		file.open();
		return file;
	}
	//
	public void append(int dataFileId,long dataOffset) {
		IndexFile currentIndexFile;
		synchronized (lockObject) {
			if(indexSegmentFiles.isEmpty()){
				currentIndexFile=newIndexFile(0,subscriberId);
				indexSegmentFiles.add(currentIndexFile);
				takeIndexSegmentFiles.add(currentIndexFile);
			}
			currentIndexFile=indexSegmentFiles.getLast();
			//
			if(currentIndexFile.space()<=0){
				currentIndexFile.flush();
				//full add new index
				int nextIndex=(currentIndexFile.index+1);
				currentIndexFile=newIndexFile(nextIndex,subscriberId);
				indexSegmentFiles.add(currentIndexFile);
				takeIndexSegmentFiles.add(currentIndexFile);
			}
			//
			IndexFileItem idxItem=new IndexFileItem();
			idxItem.dataOffset=dataOffset;
			idxItem.flag=IndexFileItem.FLAG_READY;
			idxItem.magic=IndexFileItem.MAGIC;
			idxItem.subscriber=subscriberId;
			idxItem.dataFileId=dataFileId;
			currentIndexFile.addItem(idxItem);
		}
	}
	//
	@Override
	public Message take() {
		synchronized (lockObject) {
			FileTopicQueue fq=(FileTopicQueue)queue;
			if(takeIndexSegmentFiles.isEmpty()){
				return null;
			}
			Message retMessage=null;
			IndexFile indexFile=takeIndexSegmentFiles.getFirst();
			if(currentIndex==0){
				//reset remove count
				indexFile.removedCount=0;
			}
			IndexFileItem item=indexFile.getItem(currentIndex);
			if(item.flag==IndexFileItem.FLAG_ACCEPTED||item.flag==IndexFileItem.FLAG_EXPRIED){
				indexFile.removedCount++;
			}
			//
			if(item.lastDelieverTime>0&&item.flag!=IndexFileItem.FLAG_EXPRIED){
				if((System.currentTimeMillis()-item.lastDelieverTime)>fq.getMaxTtl()){
					//max ttl 
					item.flag=IndexFileItem.FLAG_EXPRIED;
					indexFile.updateFlag(currentIndex, item.flag);
					expriedCount.increment();
					retMessage=MessageQueueDriver.takeNext;
				}	
			}
			//
			if((System.currentTimeMillis()-item.lastDelieverTime)<fq.getRedelieverInterval()){
				retMessage=MessageQueueDriver.takeNext;
			}
			//
			if(retMessage!=MessageQueueDriver.takeNext){
				if(item.flag==IndexFileItem.FLAG_READY){
					retMessage=getMessage(indexFile,item);
					item.delieverTimes++;
					indexFile.updateLastDelieverTime(currentIndex,
							System.currentTimeMillis(),(short) (item.delieverTimes));
					retMessage.delieverTimes=item.delieverTimes;
				}
				if(item.flag==IndexFileItem.FLAG_REJECTED){
					retMessage=getMessage(indexFile,item);
					item.delieverTimes++;
					indexFile.updateLastDelieverTime(currentIndex,
							System.currentTimeMillis(),(short) (item.delieverTimes));
					retMessage.delieverTimes=item.delieverTimes;
				}
				if(item.flag==IndexFileItem.FLAG_ACCEPTED
						||item.flag==IndexFileItem.FLAG_EXPRIED){
					retMessage=MessageQueueDriver.takeNext;
				}
			}
			
			//
			currentIndex++;
			if(currentIndex>=fq.indexFileCapacity){
				currentIndex=0;
				if(indexFile.removedCount>=fq.indexFileCapacity){
					indexFile.delete();
					takeIndexSegmentFiles.removeFirst();
					indexSegmentFiles.remove(indexFile);
					logger.info("delete index file {}",indexFile.indexFile.getAbsoluteFile());
				}else{
					IndexFile head=takeIndexSegmentFiles.removeFirst();
					takeIndexSegmentFiles.add(head);
				}
			}
			//
			if(retMessage==null){
				//rewind
				currentIndex=0;
				if(indexFile!=null){
					indexFile.removedCount=0;
				}
				if(takeIndexSegmentFiles.size()>1){
					IndexFile head=takeIndexSegmentFiles.removeFirst();
					takeIndexSegmentFiles.add(head);
				}	
			}
			return retMessage;
		}
	}
	//
	//
	private Message getMessage(IndexFile indexFile,IndexFileItem item){
		FileTopicQueue fq=(FileTopicQueue)queue;
		Message message=new Message();
		message.id=item.uuid;
		message.subscriber=item.subscriber;
		DataItem data=fq.getDataItem(item.dataFileId,item.dataOffset);
		if(data.payloadType==DataItem.PAYLOAD_TYPE_RAW){
			message.payload=data.payload;
		}
		message.payload=data.payload;
		message.delieverTimes=item.delieverTimes;
		return message;
	}
	//
	@Override
	public void reject(long id) {
		super.reject(id);
		synchronized (lockObject) {
			updateFlag(id,IndexFileItem.FLAG_REJECTED);
		}
	
	}
	//
	@Override
	public void accept(long id) {
		super.accept(id);
		synchronized (lockObject) {
			updateFlag(id,IndexFileItem.FLAG_ACCEPTED);
		}
	}
	//
	private void updateFlag(long id,byte flag){
		int indexSegmentId=(int) (id>>32);
		int offsetId=(int) (id&0x00000000FFFFFFFF);
		for(IndexFile file : indexSegmentFiles){
			if(file.index==indexSegmentId){
				file.updateFlag(offsetId, flag);
			}
		}
	}
	//
	@Override
	public int length() {
		synchronized (lockObject) {
			int total=0;
			for(IndexFile file:indexSegmentFiles){
				total+=file.size();
			}
			return total;
		}
	}
	//
	public void stop(){
		synchronized (lockObject) {
			for(IndexFile file:indexSegmentFiles){
				file.close();
			}
		}
	}
}
