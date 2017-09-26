/**
 * 
 */
package jazmin.driver.mq.file;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.UUID;

import jazmin.driver.mq.Message;
import jazmin.driver.mq.MessageQueueDriver;
import jazmin.driver.mq.TopicQueue;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.JSONUtil;

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
	private Object lockObject=new Object();
	//
	LinkedList<IndexFile>indexFiles;
	//
	public FileTopicQueue(String id) {
		super(id, MessageQueueDriver.TOPIC_QUEUE_TYPE_FILE);
		maxTtl=1000*60*60;//1 hour
		indexFileCapacity=1000;
		topicSubscribers=new TreeSet<>();
		indexFiles=new LinkedList<IndexFile>();
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
				indexFiles.add(file);
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
		return 0;
	}

	@Override
	public void publish(Object obj) {
		super.publish(obj);
		//
		IndexFile currentIndexFile;
		synchronized (indexFiles) {
			if(indexFiles.isEmpty()){
				currentIndexFile=newIndexFile(0);
				indexFiles.add(currentIndexFile);
			}else{
				currentIndexFile=indexFiles.getLast();
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
					indexFiles.add(currentIndexFile);
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
		if(obj instanceof byte[]){
			item.payloadType=DataItem.PAYLOAD_TYPE_RAW;
			item.payload=(byte[])obj;
		}else{
			try {
				item.payload=JSONUtil.toJson(obj).getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException(e);
			}
		}
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
	public Message take(short subscriber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reject(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(String id) {
		// TODO Auto-generated method stub
		
	}

}
