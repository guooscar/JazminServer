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


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Franck WOLFF
 */
public class DefaultExternalizer implements Externalizer {

    private static final Object[] ZERO_ARGS = new Object[0];

    private final ReentrantLock lock = new ReentrantLock();
    private final ConcurrentHashMap<Class<?>, List<Field>> orderedFields =
        new ConcurrentHashMap<Class<?>, List<Field>>();
    private final ConcurrentHashMap<String, Constructor<?>> constructors =
        new ConcurrentHashMap<String, Constructor<?>>();

    public Object newInstance(final String type, ObjectInput in)
        throws IOException, ClassNotFoundException, InstantiationException,
        InvocationTargetException, IllegalAccessException {

        Constructor<?> constructor = constructors.get(type);

        if (constructor == null) {
            Class<?> clazz = ClassUtil.forName(type);
            constructor = findDefaultConstructor(clazz);
            Constructor<?> previousConstructor = constructors.putIfAbsent(type, constructor);
            if (previousConstructor != null)
                constructor = previousConstructor; // Should be the same instance, anyway...
        }

        return constructor.newInstance(ZERO_ARGS);
    }

    public void readExternal(Object o, ObjectInput in)
        throws IOException, ClassNotFoundException, IllegalAccessException {

        if (o instanceof AbstractInstanciator) {
            AbstractInstanciator<?> instanciator = (AbstractInstanciator<?>) o;
            for (String fieldName : instanciator.getOrderedFieldNames())
                instanciator.put(fieldName, in.readObject());
        } else {
            Converter converter = new DefaultConverter();

            List<Field> fields = findOrderedFields(o.getClass());
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = converter.convertForDeserialization(in.readObject(), field.getType());
                field.set(o, value);
            }
        }
    }

    @SuppressWarnings("unused")
	public void writeExternal(Object o, ObjectOutput out)
        throws IOException, IllegalAccessException {

        String instanciatorType = null; //TODO getInstanciator(o.getClass().getName());
        if (instanciatorType != null) {
            try {
                AbstractInstanciator<?> instanciator =
                    (AbstractInstanciator<?>) ClassUtil.newInstance(instanciatorType);
                for (String fieldName : instanciator.getOrderedFieldNames()) {
                    Field field = o.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(o);
                    if (value instanceof Map)
                        value = BasicMap.newInstance((Map<?, ?>) value);
                    out.writeObject(value);
                }
            } catch (Exception e) {
                throw new RuntimeException("", e);
            }
        } else {
            List<Field> fields = findOrderedFields(o.getClass());
            for (Field field : fields) {
                field.setAccessible(true);
                out.writeObject(field.get(o));
            }
        }
    }

    public List<Field> findOrderedFields(final Class<?> clazz) {
        List<Field> fields = orderedFields.get(clazz);

        if (fields == null) {
            fields = new ArrayList<Field>();

            Set<String> allFieldNames = new HashSet<String>();
            for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {

                List<Field> newFields = new ArrayList<Field>();

                for (Field field : c.getDeclaredFields()) {
                    if (!allFieldNames.contains(field.getName()) &&
                        !Modifier.isTransient(field.getModifiers()) &&
                        !Modifier.isStatic(field.getModifiers()))
                        newFields.add(field);
                    allFieldNames.add(field.getName());
                }

                Collections.sort(newFields, new Comparator<Field>() {
                    public int compare(Field o1, Field o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                fields.addAll(0, newFields);
            }

            List<Field> previousFields = orderedFields.putIfAbsent(clazz, fields);
            if (previousFields != null)
                fields = previousFields;
        }

        return fields;
    }

    @SuppressWarnings("unused")
	protected <T> Constructor<T> findDefaultConstructor(Class<T> clazz) {
        Constructor<T> constructor = null;

        String instanciator = null; //TODO getInstanciator(clazz.getName());
        if (instanciator != null) {
            try {
                Class<T> instanciatorClass = ClassUtil.forName(instanciator, clazz);
                constructor = instanciatorClass.getConstructor(new Class[0]);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                    "Could not load instanciator class: " + instanciator + " for: " + clazz.getName(), e
                );
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(
                    "Could not find default constructor in instanciator class: " + instanciator, e
                );
            }
        } else {
            try {
                constructor = clazz.getConstructor(new Class[0]);
            } catch (NoSuchMethodException e) {
                // fall down...
            }

            if (constructor == null) {
                String key = DefaultConstructorFactory.class.getName();
                DefaultConstructorFactory factory = getDefaultConstructorFactory();
                constructor = factory.findDefaultConstructor(clazz);
            }
        }

        return constructor;
    }

    private DefaultConstructorFactory getDefaultConstructorFactory() {

        lock.lock();
        try {
            DefaultConstructorFactory factory = null;
            try {
                factory = new SunDefaultConstructorFactory();
            } catch (Exception e) {
                // fall down...
            }
            if (factory == null)
                factory = new NoDefaultConstructorFactory();
            return factory;
        } finally {
            lock.unlock();
        }
    }
}

interface DefaultConstructorFactory {
    public <T> Constructor<T> findDefaultConstructor(Class<T> clazz);
}

class NoDefaultConstructorFactory implements DefaultConstructorFactory {

    public <T> Constructor<T> findDefaultConstructor(Class<T> clazz) {
        throw new RuntimeException("Could not find default constructor in class: " + clazz);
    }
}

class SunDefaultConstructorFactory implements DefaultConstructorFactory {

    private final Object reflectionFactory;
    private final Method newConstructorForSerialization;

    public SunDefaultConstructorFactory() {
        try {
            Class<?> factoryClass = ClassUtil.forName("sun.reflect.ReflectionFactory");
            Method getReflectionFactory = factoryClass.getDeclaredMethod(
                "getReflectionFactory",
                new Class[0]
            );
            reflectionFactory = getReflectionFactory.invoke(null, new Object[0]);
            newConstructorForSerialization = factoryClass.getDeclaredMethod(
                "newConstructorForSerialization",
                new Class[]{Class.class, Constructor.class}
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not create Sun Factory", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Constructor<T> findDefaultConstructor(Class<T> clazz) {
        try {
            Constructor<?> constructor = Object.class.getDeclaredConstructor(new Class[0]);
            constructor = (Constructor<?>) newConstructorForSerialization.invoke(
                reflectionFactory,
                new Object[]{clazz, constructor}
            );
            constructor.setAccessible(true);
            return (Constructor<T>) constructor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
