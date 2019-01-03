package jazmin.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jazmin.core.app.AppException;

/**
 * 
 * @author skydu
 *
 */
public class ClassUtils {

	/**
	 * 
	 * @return
	 */
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = ClassUtils.class.getClassLoader();
			if (cl == null) {
				// getClassLoader() returning null indicates the bootstrap ClassLoader
				try {
					cl = ClassLoader.getSystemClassLoader();
				}
				catch (Throwable ex) {
					// Cannot access system ClassLoader - oh well, maybe the caller can live with null...
				}
			}
		}
		return cl;
	}
	
	/**
	 * 
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static boolean containField(Class<?> clazz,String fieldName) {
		Field[] fields=clazz.getFields();
		for (Field field : fields) {
			if(field.getName().equals(fieldName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Field> getFieldList(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		Set<String> filedNames = new HashSet<>();
		for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
			try {
				Field[] list = c.getDeclaredFields();
				for (Field field : list) {
					String name = field.getName();
					if (filedNames.contains(name)) {
						continue;
					}
					filedNames.add(field.getName());
					fields.add(field);
				}
			} catch (Exception e) {
				throw new AppException(e);
			}
		}
		return fields;
	}
}
