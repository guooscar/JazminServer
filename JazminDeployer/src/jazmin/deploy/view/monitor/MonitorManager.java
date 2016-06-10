package jazmin.deploy.view.monitor;

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
import jazmin.deploy.domain.MonitorInfo;
import jazmin.deploy.domain.MonitorInfoQuery;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.DumpUtil;

/**
 * 
 * @author icecooly
 *
 */
public class MonitorManager implements Runnable {
	//
	private static Logger logger = LoggerFactory.get(MonitorManager.class);
	//
	private String logPath = "log" + File.separator + "monitor";
	//
	static MonitorManager instance;
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
	}

	//
	private MonitorInfo getMonitorInfo() {
		return queue.poll();
	}
	//
	private File getWriterFile(String instance, String name, String type) throws IOException {
		File folder = new File(logPath, instance);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		String fileName = name + "-" + type + ".log";
		File file = new File(folder, fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		long lastModified = file.lastModified();
		logger.debug("instance:{} name:{} type:{} lastModified:{}", instance, name, type, lastModified);
		if (type != null && !type.equals(MonitorInfo.CATEGORY_TYPE_KV)) {
			if (!isToday(new Date(lastModified))) {// 归档
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				String date = sdf.format(new Date());
				move(file, folder.getAbsolutePath() + File.separator + date);
				file.createNewFile();// 重新创建文件
			}
		}
		return file;
	}
	
	private File getReaderFile(MonitorInfoQuery query) throws IOException {
		File folder = new File(logPath, query.instance);
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
		String fileName = query.name + "-" + query.type + ".log";
		File file = new File(folder, fileName);
		
		
		return file;
	}

	public static void move(File srcFile, String destPath) {
		File dir = new File(destPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		srcFile.renameTo(new File(dir, srcFile.getName()));
	}

	public static boolean isToday(Date date) {
		Calendar calDateA = Calendar.getInstance();
		calDateA.setTime(date);
		Calendar calDateB = Calendar.getInstance();
		calDateB.setTime(new Date());
		return calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)
				&& calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)
				&& calDateA.get(Calendar.DAY_OF_MONTH) == calDateB.get(Calendar.DAY_OF_MONTH);
	}

	private BufferedReader getReader(MonitorInfoQuery query) throws IOException {
		File file = getReaderFile(query);
		if(file==null){
			return null;
		}
		return new BufferedReader(new FileReader(file));
	}

	private BufferedWriter getWriter(MonitorInfo monitorInfo) throws IOException {
		File file = getWriterFile(monitorInfo.instance, monitorInfo.name, monitorInfo.type);
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
		} finally {
			writer.close();
		}
	}

	public List<MonitorInfo> getMonitorInfos(String instance) {
		List<MonitorInfo> list = new ArrayList<>();
		File directory = new File(logPath, instance);
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
	}
}
