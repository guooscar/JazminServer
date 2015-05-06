/**
 * 
 */
package jazmin.server.cdn;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 * 7 May, 2015
 */
public class CachePolicy implements FileFilter{
	private static Logger logger=LoggerFactory.get(CachePolicy.class);
	//
	long defaultTTL=3600*1000*24;//one day;
	Map<String,Long>policyMap;
	public CachePolicy() {
		policyMap=new ConcurrentHashMap<String, Long>();
	}
	//
	public boolean accept(File file){
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
		for(File f:rootDir.listFiles(this)){
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
				"jazmin-cdn");
		return tempFile;
	}
	//
	public void moveTo(File tempFile,File dest) throws IOException{
		if(!dest.getParentFile().exists()){
			boolean success=dest.getParentFile().mkdirs();
			if(!success){
				logger.error("can not mkdir {}",dest.getParentFile());
			}
		}	
		tempFile.renameTo(dest);
	}
}
