/**
 * 
 */
package jazmin.driver.mq.file;

import java.io.File;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.UUID;

import jazmin.driver.mq.Message;
import jazmin.driver.mq.MessageQueueDriver;
import jazmin.driver.mq.TopicQueue;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class FileTopicQueue extends TopicQueue{
	private static Logger logger=LoggerFactory.get(FileTopicQueue.class);
	//
	private long maxTtl;
	private long redelieverInterval;
	private String workDir;
	private int indexFileCapacity;
	File workDirFile;
	
	LinkedList<IndexFile>indexSegmentFiles;
	private int currentIndex=0;
	
	//
	//
	public FileTopicQueue(String id) {
		super(id, MessageQueueDriver.TOPIC_QUEUE_TYPE_FILE);
		maxTtl=1000*60*60;//1 hour
		indexFileCapacity=10000*10;
		redelieverInterval=1000*5;//5 seconds redeliever 
		topicSubscribers=new TreeSet<>();
		indexSegmentFiles=new LinkedList<IndexFile>();
	}
	//
	@Override
	public void start() {
		super.start();
		workDirFile=new File(workDir,id);
		if(!workDirFile.exists()){
			boolean r=workDirFile.mkdirs();
			if(!r){
				throw new IllegalArgumentException("can not create work dir:"+workDirFile.getAbsolutePath());
			}
			logger.info("create work dir:{}",workDirFile.getAbsoluteFile());
		}
		//
		File files[]=workDirFile.listFiles((dir,name)->{
			return name.endsWith(".index")&&name.startsWith(id+"-");
		});
	
		for(File f:files){
			logger.info("load index file:{}",f.getAbsoluteFile());
			IndexFile file=IndexFile.get(f.getAbsolutePath(),indexFileCapacity);
			if(file!=null){
				indexSegmentFiles.add(file);
			}
		}
	}
	/**
	 * @return the workDir
	 */
	public String getWorkDir() {
		return workDir;
	}

	/**
	 * @param workDir the workDir to set
	 */
	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}
	//
	@Override
	public int length() {
		synchronized (indexSegmentFiles) {
			int total=0;
			for(IndexFile file:indexSegmentFiles){
				total+=file.size();
			}
			return total;
		}
	}

	@Override
	public void publish(Object obj) {
		super.publish(obj);
		//
		IndexFile currentIndexFile;
		synchronized (indexSegmentFiles) {
			if(indexSegmentFiles.isEmpty()){
				currentIndexFile=newIndexFile(0);
				indexSegmentFiles.add(currentIndexFile);
			}else{
				currentIndexFile=indexSegmentFiles.getLast();
			}
		}
		//
		DataItem item=getDataItem(obj);
		synchronized (lockObject) {
			long currentOffset=0;
			for(short subscriber :topicSubscribers){
				if(currentIndexFile.space()==0){
					currentIndexFile.flush();
					//full add new index
					int nextIndex=(currentIndexFile.index+1);
					currentIndexFile=newIndexFile(nextIndex);
					indexSegmentFiles.add(currentIndexFile);
					//new file save data
					currentOffset=currentIndexFile.dataFile.append(item);
				}
				if(currentOffset==0){
					currentOffset=currentIndexFile.dataFile.append(item);
				}
				IndexFileItem idxItem=new IndexFileItem();
				idxItem.dataOffset=currentOffset;
				idxItem.flag=IndexFileItem.FLAG_READY;
				idxItem.magic=IndexFileItem.MAGIC;
				idxItem.subscriber=subscriber;
				idxItem.uuid=UUID.randomUUID().toString().replace("-","");
				currentIndexFile.addItem(idxItem);
			}
			
		}
	}
	//
	private IndexFile newIndexFile(int index){
		File newIdxFile=new File(workDirFile.getAbsoluteFile(),id+"-"+index+".index");
		IndexFile file=new IndexFile(newIdxFile.getAbsolutePath(), indexFileCapacity);
		file.index=index;
		file.open();
		return file;
	}
	//
	private DataItem getDataItem(Object obj){
		DataItem item=new DataItem();
		item.payloadType=DataItem.PAYLOAD_TYPE_JSON;
		if(!(obj instanceof byte[])){
			throw new IllegalArgumentException("payload type must be byte[]");
		}
		item.payloadType=DataItem.PAYLOAD_TYPE_RAW;
		item.payload=(byte[])obj;
		if(item.payload.length>Short.MAX_VALUE){
			throw new IllegalArgumentException("payload length should less than "
					+Short.MAX_VALUE+" but "+item.payload.length);
		}
		item.payloadLength=(short) item.payload.length;
		item.magic=DataItem.MAGIC;
		return item;
	}
	//
	@Override
	public Message take() {
		synchronized (indexSegmentFiles) {
			if(indexSegmentFiles.isEmpty()){
				return null;
			}
			Message retMessage=null;
			IndexFile indexFile=indexSegmentFiles.getFirst();
			synchronized (lockObject) {
				IndexFileItem item=indexFile.getItem(currentIndex);
				//System.err.println("take"+DumpUtil.dump(item));
				//
				if(acceptSet.containsKey(item.uuid)){
					item.flag=IndexFileItem.FLAG_ACCEPTED;
					indexFile.updateFlag(currentIndex, item.flag);
					acceptSet.remove(item.uuid);
				}
				if(rejectSet.containsKey(item.uuid)){
					item.flag=IndexFileItem.FLAG_REJECTED;
					indexFile.updateFlag(currentIndex, item.flag);
					rejectSet.remove(item.uuid);
				}
				//
				if(item.lastDelieverTime>0){
					if((System.currentTimeMillis()-item.lastDelieverTime)>maxTtl){
						//max ttl 
						item.flag=IndexFileItem.FLAG_EXPRIED;
						indexFile.updateFlag(currentIndex, item.flag);
						retMessage=MessageQueueDriver.takeNext;
					}	
				}
				//
				if((System.currentTimeMillis()-item.lastDelieverTime)<redelieverInterval){
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
					if(item.flag==IndexFileItem.FLAG_ACCEPTED){
						retMessage=MessageQueueDriver.takeNext;
					}
					if(item.flag==IndexFileItem.FLAG_EXPRIED){
						retMessage=MessageQueueDriver.takeNext;
					}
				}
				
				//
				currentIndex++;
				if(currentIndex>=indexFileCapacity){
					currentIndex=0;
					indexSegmentFiles.add(indexSegmentFiles.removeFirst());
				}
				//
				if(retMessage==null){
					//rewind
					currentIndex=0;
				}
			}
			return retMessage;
		}
	}
	//
	private Message getMessage(IndexFile indexFile,IndexFileItem item){
		Message message=new Message();
		message.id=item.uuid;
		message.subscriber=item.subscriber;
		DataItem data=indexFile.dataFile.get(item.dataOffset);
		if(data.payloadType==DataItem.PAYLOAD_TYPE_RAW){
			message.payload=data.payload;
		}
		message.payload=data.payload;
		message.delieverTimes=item.delieverTimes;
		return message;
	}

}
