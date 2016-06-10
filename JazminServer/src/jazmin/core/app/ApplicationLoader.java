/**
 * 
 */
package jazmin.core.app;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.IOUtil;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class ApplicationLoader {
	private static Logger logger=LoggerFactory.get(ApplicationLoader.class);
	private File workDir;
	private File applicationPackage;
	private File workImage;
	
	public ApplicationLoader(File workDir,File applicationPackage) {
		this.workDir=workDir;
		this.applicationPackage=applicationPackage;
	}
	//
	public Application load(){
		if(!workDir.exists()){
			if(!workDir.mkdir()){
				throw new IllegalArgumentException("can not create work dir:"+workDir);
			}
		}
		//
		if(!applicationPackage.exists()){
			throw new IllegalArgumentException("can not find application image:"
					+applicationPackage);
		}
		try {
			workImage=new File(workDir,applicationPackage.getName());
			IOUtil.copyFile(applicationPackage, workImage);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
		//
		String applicationName=applicationPackage.getName();
		//extra .jar
		applicationName=applicationName.substring(0,applicationName.length()-4);
		//
		for(Class<?>clz:getApplicationClasses()){
			if(clz.getSimpleName().equals(applicationName)){
				try {
					return (Application) clz.newInstance();
				} catch (Throwable e) {
					logger.warn(e.getMessage());
				} 
			}
		}
		//if no appclass found return first one
		for(Class<?>clz:getApplicationClasses()){
			if(clz.isAssignableFrom(Application.class)){
				try {
					return (Application) clz.newInstance();
				} catch (Throwable e) {
					logger.error(e.getMessage());
				}  
			}
		}
		logger.warn("no application class found");
		return null;
	}
	//
	public List<Class<?>>getApplicationClasses(){
		List<Class<?>>appClass=new ArrayList<Class<?>>();
		if(workImage==null){
			return appClass;
		}
		try (JarInputStream jis=new JarInputStream(
					new FileInputStream(workImage))){
			JarEntry entry;
			AppClassloader appClassLoader=new AppClassloader(workImage);
			while((entry=jis.getNextJarEntry())!=null){
				String name=entry.getName();
				if(name.endsWith(".class")){
					String className=name.replace('/', '.');
					className=className.substring(0,className.length()-6);
					try{
						appClass.add(Class.forName(className,false,appClassLoader));
					}catch(Throwable e){
						logger.error(e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return appClass;
	}
}
