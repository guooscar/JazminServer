package jazmin.deploy.manager;

import java.util.Date;
import java.util.List;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.optlog.BeanFile;
import jazmin.deploy.domain.optlog.OptLog;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * 
 * @author skydu
 *
 */
public class OptLogManager {
	//
	private static Logger logger=LoggerFactory.get(OptLogManager.class);
	//
	public static class OptLogBeanFile extends BeanFile<OptLog>{
	}
	//
	private static OptLogBeanFile file=new OptLogBeanFile();
	//
	public static void addOptLog(String optType,String remark){
		try {
			OptLog log=new OptLog();
			log.userId=DeploySystemUI.getUserId();
			log.optType=optType;
			log.ip=DeploySystemUI.getClientIpAddress();
			log.remark=remark;
			addOptLog(log);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	/**
	 * 
	 * @param optLog
	 */
	public static void addOptLog(OptLog optLog){
		try {
			optLog.createTime=new Date();
			file.add(optLog);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	//
	/**
	 * 
	 * @param optLog
	 */
	public static List<OptLog> getOptLogs(){
		return file.getList();
	}
}
