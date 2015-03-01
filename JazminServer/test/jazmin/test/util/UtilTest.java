/**
 * 
 */
package jazmin.test.util;


import java.util.concurrent.atomic.AtomicInteger;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.IOUtil;
import jazmin.util.StringUtil;

/**
 * @author yama
 * 6 Jan, 2015
 */
public class UtilTest {
	public static void main(String[] args) throws Exception{
		String sourceURI="http://cocostudio.download.appget.cn/Cocos2D-X/cocos2d-x-3.2.zip";
		String destFilePath="/tmp/xxx";
		Logger logger=LoggerFactory.get(UtilTest.class);
		logger.info("download file from {} to {}",sourceURI,destFilePath);
		AtomicInteger lastPercent=new AtomicInteger(-1);
		IOUtil.copyFile(sourceURI, destFilePath,(total,current)->{
			if(total<=0){
				logger.warn("total length is 0.no progress output");
				return;
			}
			float percent=((float)current/(float)total)*100;
			if(percent>=lastPercent.intValue()+10||lastPercent.intValue()<0){
				lastPercent.set((int)percent);
				logger.info("total {} current {} percent {}%",
						dumpByte(total),
						dumpByte(current),
						StringUtil.format("%.2f",percent));
			}
		});
		logger.info("file saved to {}",destFilePath);
		
	}
	//
	private static String dumpByte(long byteCount){
    	return StringUtil.format(
    			"(%5s KB %5s MB)", 
		    	byteCount/1024+"",
		    	byteCount/(1024*1024)+"");
    	
    }
}
