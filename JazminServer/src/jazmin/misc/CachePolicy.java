/**
 * 
 */
package jazmin.misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * 7 May, 2015
 */
public class CachePolicy {
	private static Logger logger=LoggerFactory.get(CachePolicy.class);
	//
	long defaultTTL=3600*1000*24*30L;
	Map<String,Long>policyMap;
	public CachePolicy() {
		policyMap=new ConcurrentHashMap<String, Long>();
	}
	
	public long getDefaultTTL() {
		return defaultTTL;
	}

	public void setDefaultTTL(long defaultTTL) {
		this.defaultTTL = defaultTTL;
	}

	/**
	 * 
	 * @param type
	 * @param ttlInSeconds
	 */
	public void addPolicy(String type,int ttlInSeconds){
		policyMap.put(type, ttlInSeconds*1000L);
	}
	//
	public Map<String,Long>getPolicyMap(){
		return new HashMap<String, Long>(policyMap);
	}
	//
	public boolean acceptExpire(File file){
		if(file.isDirectory()){
			return true;
		}
		//device file
		if(!file.isFile()){
			return false;
		}
		//
		long now=System.currentTimeMillis();
		long ttl=defaultTTL;
		String fileName=file.getName();
		if(fileName.indexOf('.')!=-1){
			String fileType=fileName.substring(fileName.lastIndexOf('.'));
			ttl=policyMap.getOrDefault(fileType,defaultTTL);
		}
		return (now-file.lastModified())>ttl;
	}
	//
	public void cleanFile(File rootDir){
		for(File f:rootDir.listFiles(this::acceptExpire)){
			if(f.isDirectory()){
				cleanFile(f);
			}else{
				logger.info("delete expire file {},lastModified {}",f,
						new Date(f.lastModified()));
				boolean success=f.delete();
				if(!success){
					logger.warn("can not delete file {} maybe in use,try next time",f);
				}
			}
		}
	}
	//
	public File createTempFile()throws Exception{
		File tempFile=File.createTempFile(UUID.randomUUID().toString(),
				"jazmin-temp");
		return tempFile;
	}
	//
	public void moveTo(File tempFile,File dest) throws IOException{
		if(!dest.getParentFile().exists()){
			boolean success=dest.getParentFile().mkdirs();
			if(!success){
				throw new IllegalArgumentException("can not mkdir "+dest.getParentFile());
			}
		}	
		Files.move(tempFile.toPath(),dest.toPath(),StandardCopyOption.ATOMIC_MOVE);
		if(logger.isDebugEnabled()){
			logger.debug("move file {} to {}",tempFile,dest);
		}
	}
	//
	public static void main(String[] args) {
		CachePolicy cp=new CachePolicy();
		cp.cleanFile(new File("/Users/yama/Desktop/file-driver-test"));
	}
}
