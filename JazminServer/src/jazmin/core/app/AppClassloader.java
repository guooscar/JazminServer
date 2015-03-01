package jazmin.core.app;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 
 * @author yama
 * 4 Jan, 2015
 */
public class AppClassloader extends URLClassLoader{
	//
	public AppClassloader(File workImage) throws MalformedURLException {
		super(new URL[]{workImage.toURI().toURL()});
	}
	
}
