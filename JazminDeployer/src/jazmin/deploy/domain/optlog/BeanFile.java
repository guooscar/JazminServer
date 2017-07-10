package jazmin.deploy.domain.optlog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ibm.icu.text.SimpleDateFormat;

import jazmin.deploy.util.DateUtil;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.FileUtil;

/**
 * 
 * @author skydu
 *
 * @param <T>
 */
public abstract class BeanFile<T> {
	//
	private static final Logger logger=LoggerFactory.get(BeanFile.class);
	//
	private String logPath;
	private String suffix=".log";
	private Class<?> type;
	private int saveDays=7;//default save 7 days
	private String spilitChar="\t";
	private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//
	public BeanFile(){
		ParameterizedType pt=(ParameterizedType) getClass().getGenericSuperclass();
		type=(Class<?>) pt.getActualTypeArguments()[0];
		logPath = "log" + File.separator +type.getSimpleName();
	}
	//
	private byte[] convertToFile(T t){
		StringBuilder sb=new StringBuilder();
		for (Field f : type.getFields()) {
			Class<?> fileClazz=f.getType();
			if (Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			try {
				Object val=f.get(t);
				if(val!=null){
					if(fileClazz==Date.class){
						sb.append(sdf.format((Date)val));
					}else{
						sb.append((String)val);
					}
				}
			} catch (Exception e) {
				logger.error(e);
				throw new RuntimeException(e);
			}
			sb.append(spilitChar);
		}	
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("\n");
		return sb.toString().getBytes();
	}
	//
	@SuppressWarnings("unchecked")
	protected T convertToBean(String content){
		if(content==null){
			return null;
		}
		String[] values=content.split(spilitChar);
		try{
			Object instance=type.newInstance();
			convertBean(instance,values);
			return (T) instance;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	//
	protected void convertBean(Object o,String[] values)
			throws Exception {
		Class<?> type = o.getClass();
		int index=0;
		for (Field f : type.getFields()) {
			Class<?> fieldType = f.getType();
			if (Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			if(index>=values.length){
				logger.warn("index:{}>=values.length:{}",index,values.length);
				continue;
			}
			String strValue=values[index];
			Object value = null;
			if (fieldType.equals(String.class)) {
				value = strValue;
			} else if (fieldType.equals(Integer.class)
					|| fieldType.equals(int.class)) {
				value = strValue==null?0:Integer.valueOf(strValue);
			} else if (fieldType.equals(Short.class)
					|| fieldType.equals(short.class)) {
				value = strValue==null?0:Short.valueOf(strValue);
			} else if (fieldType.equals(Long.class)
					|| fieldType.equals(long.class)) {
				value =strValue==null?0:Long.valueOf(strValue);
			} else if (fieldType.equals(Double.class)
					|| fieldType.equals(double.class)) {
				value =strValue==null?0:Double.valueOf(strValue);
			} else if (fieldType.equals(Float.class)
					|| fieldType.equals(float.class)) {
				value = strValue==null?0:Float.valueOf(strValue);
			} else if (fieldType.equals(Date.class)) {
				value = strValue==null?null:sdf.parse(strValue);
			} else if (fieldType.equals(Boolean.class)
					|| fieldType.equals(boolean.class)) {
				value = strValue==null?0:Boolean.valueOf(strValue);
			}
			f.setAccessible(true);
			if (value != null) {
				f.set(o, value);
			}
			//
			index++;
		}
	}
	//
	private Path getCurrFilePath(){
		File folder = new File(logPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		try {
			String fileName = type.getSimpleName() + suffix;
			File file = new File(folder, fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			long lastModified = file.lastModified();
			if (!DateUtil.isToday(new Date(lastModified))) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				String date = sdf.format(new Date(lastModified));
				move(file, folder.getAbsolutePath() + File.separator + date);
				file.createNewFile();
			}
			return Paths.get(logPath+File.separator+fileName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	//
	public static void move(File srcFile, String destPath) {
		File dir = new File(destPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		srcFile.renameTo(new File(dir, srcFile.getName()));
	}
	//
	public void add(T t){
		try {
			Files.write(getCurrFilePath(),convertToFile(t),StandardOpenOption.APPEND);
			deleteOld();
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public List<T> getList(){
		try {
			List<T> list=new ArrayList<>();
			List<String> lines=Files.readAllLines(getCurrFilePath());
			for (String content : lines) {
				T bean=convertToBean(content);
				list.add(bean);
			}
			return list;
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	private void deleteOld(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String date = sdf.format(DateUtil.getNextDay(-saveDays));
		File folder = new File(logPath,date);
		if(folder.exists()){
			FileUtil.deleteDirectory(folder);
		}
	}
	//
	/**
	 * @return the logPath
	 */
	public String getLogPath() {
		return logPath;
	}
	/**
	 * @param logPath the logPath to set
	 */
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	/**
	 * @return the suffix
	 */
	public String getSuffix() {
		return suffix;
	}
	/**
	 * @param suffix the suffix to set
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	/**
	 * @return the saveDays
	 */
	public int getSaveDays() {
		return saveDays;
	}
	/**
	 * @param saveDays the saveDays to set
	 */
	public void setSaveDays(int saveDays) {
		this.saveDays = saveDays;
	}
	/**
	 * @return the spilitChar
	 */
	public String getSpilitChar() {
		return spilitChar;
	}
	/**
	 * @param spilitChar the spilitChar to set
	 */
	public void setSpilitChar(String spilitChar) {
		this.spilitChar = spilitChar;
	}
}
