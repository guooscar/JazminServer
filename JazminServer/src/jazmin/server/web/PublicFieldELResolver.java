package jazmin.server.web;

import java.beans.FeatureDescriptor;
import java.lang.reflect.Field;
import java.util.Iterator;

import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.PropertyNotFoundException;
/**
 * 
 * @author yama
 * 24 Jan, 2015
 */
public class PublicFieldELResolver extends BeanELResolver {
	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return null;
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context,
			Object base) {
		return null;
	}

	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		return null;
	}

	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		if (base == null) {
			return null;
		}
		try {
			Field field = getDeclaredField(base, (String)property);
			field.setAccessible(true);
			Object value = field.get(base);
			context.setPropertyResolved(true);
			return value;
		} catch (NoSuchFieldException e) {
			return super.getValue(context,base, property);
		}catch (Exception e) {
			throw new PropertyNotFoundException(e);
		}
	}
	
	//
	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return false;
	}

	@Override
	public void setValue(ELContext context, Object base, Object property,
			Object value) {
		if (base == null) {
			return;
		}
		try {
			setFieldValue(base, (String) property, value);
			context.setPropertyResolved(true);
		} catch (NoSuchFieldException e) {
			 super.setValue(context,base, property,value);
		}catch (Exception e) {
			throw new PropertyNotFoundException(e);
		}
	}
	//
	@SuppressWarnings("unused")
	public static Field getDeclaredField(Object object,String fieldName) 
			throws NoSuchFieldException, SecurityException{
		Field field=null;
		Class<?>clazz=object.getClass();
		for(;clazz!=Object.class;clazz=clazz.getSuperclass()){
			try{
				field=clazz.getDeclaredField(fieldName);
				return field;
			}catch(NoSuchFieldException e){}
		}
		if(field==null){
			throw new NoSuchFieldException(clazz+" field:"+fieldName);
		}
		return field;
	}
	//
	public static void setFieldValue(Object object,String fieldName,Object value) 
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		Field field=getDeclaredField(object, fieldName);
		field.setAccessible(true);
		field.set(object, value);
	}
}