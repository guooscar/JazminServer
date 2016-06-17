package jazmin.deploy.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import com.ibm.icu.text.SimpleDateFormat;

import jazmin.core.Jazmin;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.monitor.MonitorInfo;
import jazmin.deploy.domain.monitor.MonitorInfoQuery;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.DumpUtil;
import jazmin.util.FileUtil;

/**
 * 
 * @author icecooly
 *
 */
public class MonitorManager implements Runnable {
	//
	private static Logger logger = LoggerFactory.get(MonitorManager.class);
	//
	private static final String LOG_PATH = "log" + File.separator + "monitor";
	
	private static final String SUFFIX=".log";
	//
	private static MonitorManager instance;
	//
	private Queue<MonitorInfo> queue;
	//
	public static MonitorManager get() {
		if (instance == null) {
			instance = new MonitorManager();
		}
		return instance;
	}
	//
	private MonitorManager() {
		queue = new ConcurrentLinkedQueue<MonitorInfo>();
		Jazmin.scheduleAtFixedRate(this, 10, 10, TimeUnit.SECONDS);
	}
	//
	public void addMonitorInfo(MonitorInfo info) {
		queue.add(info);
		Instance instance=DeployManager.getInstance(info.instance);
		if(instance!=null){
			instance.setAlive(true);
			instance.setLastAliveTime(new Date());
		}
	}
	//
	private MonitorInfo getMonitorInfo() {
		return queue.poll();
	}
	//
	public static void move(File srcFile, String destPath) {
		File dir = new File(destPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		srcFile.renameTo(new File(dir, srcFile.getName()));
	}
	//获取距离今天num天的日期0点 
	public static Date getNextDay(int num){
		 Calendar now=Calendar.getInstance();
		 now.add(Calendar.DAY_OF_YEAR,num);
		 now.set(Calendar.HOUR_OF_DAY, 0);
		 now.set(Calendar.MINUTE, 0);
		 now.set(Calendar.SECOND, 0);
		 now.set(Calendar.MILLISECOND, 0);
	     return now.getTime();
	}
	//
	public static boolean isToday(Date date) {
		Calendar calDateA = Calendar.getInstance();
		calDateA.setTime(date);
		Calendar calDateB = Calendar.getInstance();
		calDateB.setTime(new Date());
		return calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)
				&& calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)
				&& calDateA.get(Calendar.DAY_OF_MONTH) == calDateB.get(Calendar.DAY_OF_MONTH);
	}
	//
	private BufferedReader getReader(MonitorInfoQuery query) throws IOException {
		File folder = new File(LOG_PATH, query.instance);
		if(!folder.exists()){
			return null;
		}
		if(query.startTime!=null){
			Date startTime=new Date(query.startTime);
			if (!isToday(startTime)){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				String date = sdf.format(startTime);
				folder=new File(folder.getAbsolutePath()+File.separator+date);
			}
		}
		String fileName = query.name + "-" + query.type + SUFFIX;
		File file = new File(folder, fileName);
		return new BufferedReader(new FileReader(file));
	}
	//
	private BufferedWriter getWriter(MonitorInfo monitorInfo) throws IOException {
		File folder = new File(LOG_PATH, monitorInfo.instance);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		String fileName = monitorInfo.name + "-" + monitorInfo.type + SUFFIX;
		File file = new File(folder, fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		long lastModified = file.lastModified();
		if (monitorInfo.type != null && !monitorInfo.type.equals(MonitorInfo.CATEGORY_TYPE_KV)) {
			if (!isToday(new Date(lastModified))) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				String date = sdf.format(new Date(lastModified));
				move(file, folder.getAbsolutePath() + File.separator + date);
				file.createNewFile();
			}
		}
		boolean append = true;
		if (monitorInfo.type.equals(MonitorInfo.CATEGORY_TYPE_KV)) {
			append = false;
		}
		return new BufferedWriter(new FileWriter(file, append));
	}
	//
	private void addData(MonitorInfo info) throws IOException {
		BufferedWriter writer = getWriter(info);
		try {
			writer.write(info.toString());
			writer.newLine();
			writer.flush();
			//delete 7 days ago log
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String date = sdf.format(getNextDay(-7));
			File folder = new File(LOG_PATH, info.instance+File.separator + date);
			if(folder.exists()){
				FileUtil.deleteDirectory(folder);
			}
		} finally {
			writer.close();
		}
	}
	//
	public List<MonitorInfo> getMonitorInfos(String instance) {
		List<MonitorInfo> list = new ArrayList<>();
		File directory = new File(LOG_PATH, instance);
		if(!directory.exists()){
			return list;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				continue;
			}
			String fileName = file.getName();
			MonitorInfo info = new MonitorInfo();
			info.instance = instance;
			info.name = fileName.split("-")[0];
			info.type = fileName.split("-")[1].substring(0, fileName.split("-")[1].lastIndexOf("."));
			info.time = file.lastModified();
			list.add(info);
		}
		return list;
	}

	public List<MonitorInfo> getData(MonitorInfoQuery query) {
		List<MonitorInfo> list = new ArrayList<>();
		BufferedReader reader = null;
		try {
			reader = getReader(query);
			if(reader==null){
				return list;
			}
			String tmpStr = null;
			while ((tmpStr = reader.readLine()) != null) {
				String[] datas = tmpStr.split("\t");
				String saveKey = datas[2];
				if (saveKey.equals(query.name)) {
					MonitorInfo info = new MonitorInfo();
					info.time = Long.valueOf(datas[0]);
					info.type = datas[1];
					info.name = datas[2];
					info.value = datas[3];
					if (query.startTime != null) {
						if (info.time < query.startTime) {
							continue;
						}
					}
					if (query.endTime != null) {
						if (info.time > query.endTime) {
							continue;
						}
					}
					list.add(info);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return list;
	}

	@Override
	public void run() {
		while (true) {
			MonitorInfo info = getMonitorInfo();
			if (info == null) {
				break;
			}
			try {
				logger.debug("info:{}", DumpUtil.dump(info));
				addData(info);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		//
		long now=System.currentTimeMillis();
		DeployManager.getInstances().forEach((in)->{
			if(in.isAlive){
				if(in.lastAliveTime!=null&&(now-in.lastAliveTime.getTime())>1000*60){
					in.setAlive(false);
				}
			}
		});
	}
}
