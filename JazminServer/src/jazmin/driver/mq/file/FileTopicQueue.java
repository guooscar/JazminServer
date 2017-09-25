/**
 * 
 */
package jazmin.driver.mq.file;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import jazmin.driver.mq.Message;
import jazmin.driver.mq.MessageQueueDriver;
import jazmin.driver.mq.TopicQueue;

/**
 * @author yama
 *
 */
public class FileTopicQueue extends TopicQueue{
	private long maxTtl;
	private long redelieverInterval;
	private String workDir;
	private Set<String> topicSubscribers;
	//
	private int logFileMaxMessageCount;
	//
	File workDirFile;
	public FileTopicQueue(String id) {
		this.id=id;
		this.type=MessageQueueDriver.TOPIC_QUEUE_TYPE_FILE;
		maxTtl=1000*60*60;//1 hour
		logFileMaxMessageCount=1000;
		topicSubscribers=new TreeSet<String>();
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
		}
		//
		
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void subscribe(String name) {
		if(topicSubscribers.contains(name)){
			throw new IllegalArgumentException(name+" already exists");
		}
		topicSubscribers.add(name);
	}

	@Override
	public void publish(Object obj) {
		
		//
		
		
	}

	@Override
	public Message take() {
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
