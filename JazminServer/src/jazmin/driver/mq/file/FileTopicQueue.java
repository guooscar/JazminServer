package jazmin.driver.mq.file;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.driver.mq.MessageQueueDriver;
import jazmin.driver.mq.TopicQueue;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * 
 * @author yama
 *
 */
public class FileTopicQueue extends TopicQueue{
	private static Logger logger=LoggerFactory.get(FileTopicQueue.class);
	//
	private String workDir;
	int indexFileCapacity;
	File workDirFile;
	Map<Integer,DataFile>dataFiles;
	DataFile currentDataFile;
	long maxDataFileLength;
	private Object lockObject=new Object();
	//
	public FileTopicQueue(String id) {
		super(id,MessageQueueDriver.TOPIC_QUEUE_TYPE_FILE);
		maxTtl=1000*3600*24;//1 day
		maxDataFileLength= 1024L*1024L*100;//100m
		redelieverInterval=1000*5;//5 seconds redeliever 
		indexFileCapacity=10;
		dataFiles=new ConcurrentHashMap<>();
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
		topicSubscribers.forEach(s->{
			FileTopicChannel c=new FileTopicChannel(this);
			c.setSubscriberId(s);
			topicChannels.put(s,c);
			File files[]=workDirFile.listFiles((dir,name)->{
				return name.endsWith(".index")&&name.startsWith(s+"-");
			});
			c.load(files);
		});
		//load datafile
		File files[]=workDirFile.listFiles((dir,name)->{
			return name.endsWith(".data");
		});
		for(File file : files){
			DataFile df=new DataFile(file.getAbsolutePath());
			df.open();
			dataFiles.put(df.index,df);
		}
		//
		for(DataFile df:dataFiles.values()){
			if(currentDataFile==null){
				currentDataFile=df;
			}
			if(currentDataFile.index<df.index){
				currentDataFile=df;
			}
		}
	}
	//
	public DataFile getDataFile(){
		if(currentDataFile==null){
			currentDataFile=newDataFile(0);
		}
		//
		long dataFileLength=currentDataFile.dataFile.length();
		if(dataFileLength>=maxDataFileLength){
			logger.info("data file {} size:{}  > maxDataFileLength {}",
							currentDataFile.dataFile,
							dataFileLength,
							maxDataFileLength);
			currentDataFile=newDataFile(currentDataFile.index+1);
		}
		return currentDataFile;
	}
	//
	private DataFile newDataFile(int index){
		File dataFilePath=new File(workDirFile,index+".data");
		DataFile df=new DataFile(dataFilePath.getAbsolutePath());
		df.open();
		dataFiles.put(df.index, df);
		return df;
	}
	//
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
	public void stop(){
		topicChannels.forEach((k,v)->{
			v.stop();
		});
	}
	//
	DataItem getDataItem(int dataFileId,long offset){
		synchronized (lockObject) {
			DataFile dataFile=dataFiles.get(dataFileId);
			return dataFile.get(offset);		
		}
	}
	//
	private DataItem getDataItem(Object obj){
		DataItem item=new DataItem();
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
	public void publish(Object obj){
		super.publish(obj);
		synchronized (lockObject) {
			DataItem dataItem=getDataItem(obj);
			long offset=getDataFile().append(dataItem);
			topicSubscribers.forEach(s->{
				((FileTopicChannel)topicChannels.get(s)).append(currentDataFile.index,offset);
			});
		}	
	}
	
}
