package jazmin.driver.mq.file;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.driver.mq.MessageQueueDriver;
import jazmin.driver.mq.TopicChannel;
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
		maxDataFileLength= 1024L*1024L*100;//100m
		indexFileCapacity=10000;
		dataFiles=new ConcurrentHashMap<>();
	}
	//
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
			FileTopicChannel c=new FileTopicChannel(this,s);
			topicChannels.put(s.id,c);
			File files[]=workDirFile.listFiles((dir,name)->{
				return name.endsWith(".index")&&name.startsWith(s.id+"-");
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
	/**
	 * 
	 * @return
	 */
	public long getMaxDataFileLength() {
		return maxDataFileLength;
	}
	/**
	 * 
	 * @param maxDataFileLength
	 */
	public void setMaxDataFileLength(long maxDataFileLength) {
		this.maxDataFileLength = maxDataFileLength;
	}
	//
	DataFile getDataFile(){
		if(currentDataFile==null){
			currentDataFile=newDataFile(1);
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
		removeUnusedDataFile();
		File dataFilePath=new File(workDirFile,index+".data");
		DataFile df=new DataFile(dataFilePath.getAbsolutePath());
		df.open();
		dataFiles.put(df.index, df);
		return df;
	}
	//
	private void removeUnusedDataFile(){
		Set<Integer>used=new TreeSet<Integer>();
		for(TopicChannel tc:topicChannels.values()){
			FileTopicChannel ftc=(FileTopicChannel) tc;
			used.addAll(ftc.getUsedDataFileIndex());
		}
		//
		Set<Integer>removedSet=new TreeSet<Integer>();
		dataFiles.forEach((k,v)->{
			if(!used.contains(v.index)){
				removedSet.add(v.index);
			}
		});
		//
		removedSet.forEach((i)->{
			DataFile df=dataFiles.remove(i);
			logger.info("remove unused data file {}",df.dataFile);
			df.delete();
		});
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
		DataFile dataFile=dataFiles.get(dataFileId);
		synchronized (dataFile) {
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
		DataItem dataItem=getDataItem(obj);
		
		synchronized (lockObject) {
			DataFile dataFile=getDataFile();
			LongHolder holder=new LongHolder();
			synchronized (dataFile) {
				holder.value=dataFile.append(dataItem);
			}
			topicSubscribers.forEach(s->{
				((FileTopicChannel)topicChannels.get(s.id)).append(currentDataFile.index,holder.value);
			});
		}	
	}
	//
	static class LongHolder{
		long value;
	}
}
