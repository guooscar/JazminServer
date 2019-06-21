/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
/*
  GRANITE DATA SERVICES
  Copyright (C) 2007 ADEQUATE SYSTEMS SARL

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 3 of the License, or (at your
  option) any later version.
 
  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package jazmin.server.msg.codec.amf;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jazmin.core.app.AppException;

/**
 * @author Franck WOLFF
 */
public abstract class ClassUtil {

    public static Object newInstance(String type)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return forName(type).newInstance();
    }

    public static <T> T newInstance(String type, Class<T> cast)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return forName(type, cast).newInstance();
    }

    public static Object newInstance(String type, Class<?>[] argsClass, Object[] argsValues)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return newInstance(forName(type), argsClass, argsValues);
    }

    public static <T> T newInstance(Class<T> type, Class<?>[] argsClass, Object[] argsValues)
        throws InstantiationException, IllegalAccessException {
        T instance = null;
        try {
            Constructor<T> constructorDef = type.getConstructor(argsClass);
            instance = constructorDef.newInstance(argsValues);
        } catch (SecurityException e) {
            throw new InstantiationException(e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new InstantiationException(e.getMessage());            
        } catch (IllegalArgumentException e) {
            throw new InstantiationException(e.getMessage());            
        } catch (InvocationTargetException e) {
            throw new InstantiationException(e.getMessage());            
        }
        return instance;
    }

    public static Class<?> forName(String type) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> forName(String type, Class<T> cast) throws ClassNotFoundException {
        return (Class<T>)Thread.currentThread().getContextClassLoader().loadClass(type);
    }
    
    public static Constructor<?> getConstructor(String type, Class<?>[] paramTypes)
        throws ClassNotFoundException, NoSuchMethodException {
        return getConstructor(forName(type), paramTypes);
    }
    
    public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>[] paramTypes)
        throws NoSuchMethodException {
        return type.getConstructor(paramTypes);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> emptyList(Class<T> type) {
        return Collections.EMPTY_LIST;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Set<T> emptySet(Class<T> type) {
        return Collections.EMPTY_SET;
    }
    
    @SuppressWarnings("unchecked")
    public static <T, U> Map<T, U> emptyMap(Class<T> keyType, Class<U> valueType) {
        return Collections.EMPTY_MAP;
    }
    
    public static Class<?> classOfType(Type type) {
        if (type instanceof Class<?>)
            return (Class<?>)type;
        if (type instanceof ParameterizedType)
            return (Class<?>)((ParameterizedType)type).getRawType();
        if (type instanceof WildcardType) {
            // Forget lower bounds and only deal with first upper bound...
            Type[] ubs = ((WildcardType)type).getUpperBounds();
            if (ubs.length > 0)
                return classOfType(ubs[0]);
        }
        if (type instanceof GenericArrayType) {
            Class<?> ct = classOfType(((GenericArrayType)type).getGenericComponentType());
            return (ct != null ? Array.newInstance(ct, 0).getClass() : Object[].class);
        }
        if (type instanceof TypeVariable) {
            // Only deal with first (upper) bound...
            Type[] ubs = ((TypeVariable<?>)type).getBounds();
            if (ubs.length > 0)
                return classOfType(ubs[0]);
        }
        // Should never append...
        return Object.class;
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
