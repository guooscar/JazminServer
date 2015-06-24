/**
 * 
 */
package jazmin.util;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.fastjson.serializer.PropertyFilter;

/**
 * @author yama
 *
 */
public class ByteArrayProperityFilter implements PropertyFilter{
	private final Set<String> excludes = new HashSet<String>();
    public ByteArrayProperityFilter(Class<?> clazz){
        super();
        for(Field ff:clazz.getFields()){
        	if(ff.getType().equals(byte[].class)){
        		excludes.add(ff.getName());
        	}
        	if(ff.isAnnotationPresent(DumpIgnore.class)){
        		excludes.add(ff.getName());
        	}
        }
    }
	@Override
	public boolean apply(Object arg0, String name, Object arg2) {
		return !excludes.contains(name);
	}
	
}
