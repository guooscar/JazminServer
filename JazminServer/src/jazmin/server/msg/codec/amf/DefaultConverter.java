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


import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Franck WOLFF
 */
public class DefaultConverter implements Converter {

    public Object convertForSerialization(Object value) {
        return value;
    }

    public Object convertForDeserialization(Object value, Type targetType) {

        final Class<?> targetClass = ClassUtil.classOfType(targetType);

        // Deal with primitive type for null.
        if (value == null) {
            if (targetClass.isPrimitive()) {
                if (targetClass == Integer.TYPE)
                    value = Integer.valueOf(0);
                else if (targetClass == Double.TYPE)
                    value = Double.valueOf(0.0);
                else if (targetClass == Boolean.TYPE)
                    value = Boolean.FALSE;
                else if (targetClass == Character.TYPE)
                    value = Character.valueOf((char) 0);
                else if (targetClass == Long.TYPE)
                    value = Long.valueOf(0);
                else if (targetClass == Byte.TYPE)
                    value = Byte.valueOf((byte) 0);
                else if (targetClass == Float.TYPE)
                    value = Float.valueOf(0);
                else if (targetClass == Short.TYPE)
                    value = Short.valueOf((short) 0);
            }
        } else if (!targetClass.isAssignableFrom(value.getClass())) {

            // Should be an ArrayCollection.
            if (value instanceof Collection) {
                if (Set.class.isAssignableFrom(targetClass)) {
                    if (SortedSet.class.isAssignableFrom(targetClass))
                        value = new TreeSet<Object>((Collection<?>) value);
                    else
                        value = new HashSet<Object>((Collection<?>) value);
                } else if (List.class.isAssignableFrom(targetClass))
                    value = new ArrayList<Object>((Collection<?>) value);
            }
            // Should only be an Integer or Double (after AMF3 deserialization).
            else if (value instanceof Number) {
                Number number = (Number) value;
                if (targetClass == Double.class || targetClass == Double.TYPE)
                    value = Double.valueOf(number.doubleValue());
                else if (targetClass == Integer.class || targetClass == Integer.TYPE)
                    value = Integer.valueOf(number.intValue());
                else if (targetClass == Long.class || targetClass == Long.TYPE)
                    value = Long.valueOf(number.longValue());
                else if (targetClass == Float.class || targetClass == Float.TYPE)
                    value = Float.valueOf(number.floatValue());
                else if (targetClass == Byte.class || targetClass == Byte.TYPE)
                    value = Byte.valueOf(number.byteValue());
                else if (targetClass == Short.class || targetClass == Short.TYPE)
                    value = Short.valueOf(number.shortValue());
                else if (targetClass == BigDecimal.class)
                    value = BigDecimal.valueOf(number.doubleValue());
                else if (targetClass == BigInteger.class)
                    value = BigInteger.valueOf(number.longValue());
            }
            // Java Character -[AMF3]-> AS3 String[1] -[AMF3]-> java Character.
            else
            if (targetClass == Character.class && value.getClass() == String.class && ((String) value).length() == 1) {
                value = Character.valueOf(((String) value).charAt(0));
            }
            // Various date conversion.
            else if (value instanceof Date) {
                Date date = (Date) value;
                if (targetClass.isAssignableFrom(Calendar.class)) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    value = cal;
                } else if (targetClass.isAssignableFrom(java.sql.Timestamp.class))
                    value = new java.sql.Timestamp(date.getTime());
                else if (targetClass.isAssignableFrom(java.sql.Date.class))
                    value = new java.sql.Date(date.getTime());
                else if (targetClass.isAssignableFrom(java.sql.Time.class))
                    value = new java.sql.Time(date.getTime());
            }
            // Array to List conversion (assume compatible component type).
            else if (value.getClass().isArray() && List.class.isAssignableFrom(targetClass)) {
                final int arrayLength = Array.getLength(value);
                final List<Object> list = new ArrayList<Object>(arrayLength);
                for (int j = 0; j < arrayLength; j++)
                    list.add(Array.get(value, j));
                value = list;
            } else
            if (value instanceof Map && SortedMap.class.isAssignableFrom(targetClass) && !(value instanceof SortedMap)) {
                value = new TreeMap<Object, Object>((Map<?, ?>) value);
            }
        }

        return value;
    }

    protected Object[] convertForDeserialization(Object[] values, Type[] targetTypes) {
        if (values == null)
            return values;

        Object[] convertedArgs = new Object[values.length];
        for (int i = 0; i < convertedArgs.length; i++)
            convertedArgs[i] = convertForDeserialization(values[i], targetTypes[i]);
        return convertedArgs;
    }

    protected boolean canConvertForMethodInvocation(Object value, Type targetType) {
        if (value == null)
            return true;

        Class<?> valueClass = value.getClass();
        if (valueClass == targetType)
            return true;

        Class<?> targetClass = ClassUtil.classOfType(targetType);
        if (targetClass.isAssignableFrom(valueClass))
            return true;

        // Class/Primitive Identity & Widening Primitive Conversion.
        if (targetClass.isPrimitive()) {
            if (
                (valueClass == Integer.class && (
                    targetClass == Integer.TYPE || targetClass == Long.TYPE ||
                        targetClass == Float.TYPE || targetClass == Double.TYPE)) ||
                    (valueClass == Boolean.class && targetClass == Boolean.TYPE) ||
                    (valueClass == Long.class && (
                        targetClass == Long.TYPE || targetClass == Float.TYPE || targetClass == Double.TYPE)) ||
                    (valueClass == Float.class && (targetClass == Float.TYPE || targetClass == Double.TYPE)) ||
                    (valueClass == Byte.class && (
                        targetClass == Byte.TYPE || targetClass == Short.TYPE || targetClass == Integer.TYPE ||
                            targetClass == Long.TYPE || targetClass == Float.TYPE || targetClass == Double.TYPE)) ||
                    (valueClass == Short.class && (
                        targetClass == Short.TYPE || targetClass == Integer.TYPE || targetClass == Long.TYPE ||
                            targetClass == Float.TYPE || targetClass == Double.TYPE)) ||
                    (valueClass == Character.class && (
                        targetClass == Character.TYPE || targetClass == Integer.TYPE || targetClass == Long.TYPE ||
                            targetClass == Float.TYPE || targetClass == Double.TYPE))
                )
                return true;
        }
        // Widening Number Conversion.
        else if (Number.class.isAssignableFrom(targetClass)) {
            if (
                (valueClass == Integer.class && (
                    targetClass == Long.class || targetClass == Float.class || targetClass == Double.class ||
                        targetClass == BigDecimal.class || targetClass == BigInteger.class)) ||
                    (valueClass == Long.class && (
                        targetClass == Float.class || targetClass == Double.class ||
                            targetClass == BigDecimal.class || targetClass == BigInteger.class)) ||
                    (valueClass == Float.class && (
                        targetClass == Double.class || targetClass == BigDecimal.class ||
                            targetClass == BigInteger.class)) ||
                    (valueClass == Byte.class && (
                        targetClass == Short.class || targetClass == Integer.class || targetClass == Long.class ||
                            targetClass == Float.class || targetClass == Double.class ||
                            targetClass == BigDecimal.class || targetClass == BigInteger.class)) ||
                    (valueClass == Short.class && (
                        targetClass == Integer.class || targetClass == Long.class ||
                            targetClass == Float.class || targetClass == Double.class ||
                            targetClass == BigDecimal.class || targetClass == BigInteger.class)) ||
                    (valueClass == Character.class && (
                        targetClass == Integer.class || targetClass == Long.class ||
                            targetClass == Float.class || targetClass == Double.class ||
                            targetClass == BigDecimal.class || targetClass == BigInteger.class))
                )
                return true;
        }
        // If it's an array, can we find a matching list?
        else if (valueClass.isArray() && List.class.isAssignableFrom(targetClass)) {
            // List (not parameterized)
            if (targetType instanceof Class<?>)
                return true;
            // List<...> (parameterized)
            if (targetType instanceof ParameterizedType) {
                Type[] lcts = ((ParameterizedType) targetType).getActualTypeArguments();
                if (lcts.length == 1) { // Should always be true for a list...
                    Class<?> lct = ClassUtil.classOfType(lcts[0]);
                    if (lct.isAssignableFrom(valueClass.getComponentType()))
                        return true;
                }
            }
        }

        return false;
    }

    protected boolean canConvertForMethodInvocation(Object[] values, Type[] targetTypes) {
        boolean canConvert = true;
        for (int i = 0; i < targetTypes.length && canConvert; i++)
            canConvert = canConvertForMethodInvocation(values[i], targetTypes[i]);
        return canConvert;
    }

    /**
 * @author Franck WOLFF
     */
    public abstract static class ActionScriptClassDescriptor {

        protected final String type;
        protected final byte encoding;
        protected final Externalizer externalizer;
        protected final Converter converter;
        protected final List<Property> properties;

        protected ActionScriptClassDescriptor(String type, byte encoding) {
            this.type = (type == null ? "" : type);
            this.encoding = encoding;
            this.externalizer = findExternalizer();
            this.converter = new DefaultConverter();
            this.properties = new ArrayList<Property>();
        }

        private Externalizer findExternalizer() {
            if (encoding != 0x01)
                return null;
            return null;
        }

        public String getType() {
            return type;
        }

        public Externalizer getExternalizer() {
            return externalizer;
        }

        public byte getEncoding() {
            return encoding;
        }

        public boolean isExternalizable() {
            return encoding == 0x01;
        }

        public boolean isDynamic() {
            return encoding == 0x02;
        }

        public abstract void defineProperty(String name);
        public abstract Object newJavaInstance();

        public int getPropertiesCount() {
            return properties.size();
        }
        public String getPropertyName(int index) {
            return properties.get(index).getName();
        }

        public void setPropertyValue(int index, Object instance, Object value) {
            Property prop = properties.get(index);
            if (value instanceof AbstractInstanciator)
                ((AbstractInstanciator<?>)value).addReferer(instance, prop);
            else
                prop.setProperty(instance, value);
        }

        public void setPropertyValue(String name, Object instance, Object value) {
            // instance must be an instance of Map...
            Property prop = new MapProperty(converter, name);
            if (value instanceof AbstractInstanciator)
                ((AbstractInstanciator<?>)value).addReferer(instance, prop);
            else
                prop.setProperty(instance, value);
        }

        @Override
        public String toString() {
            return getClass().getName() + " {\n" +
                "  type=" + type + ",\n" +
                "  encoding=" + encoding + ",\n" +
                "  externalizer=" + externalizer + ",\n" +
                "  converter=" + converter + ",\n" +
                "  properties=" + properties + "\n" +
            "}";
        }
    }

    /**
 * @author Franck WOLFF
     */
    public static class DefaultActionScriptClassDescriptor extends ActionScriptClassDescriptor {

        public DefaultActionScriptClassDescriptor(String type, byte encoding) {
            super(type, encoding);
        }

        @Override
        public void defineProperty(String name) {

            if (type.length() == 0)
                properties.add(new MapProperty(converter, name));
            else if ("uid".equals(name)) // ObjectProxy specific property...
                properties.add(new UIDProperty(converter));
            else {
                try {
                    Class<?> clazz = ClassUtil.forName(type);

                    // Try to find public getter/setter.
                    BeanInfo info = Introspector.getBeanInfo(clazz);
                    PropertyDescriptor[] props = info.getPropertyDescriptors();
                    for (PropertyDescriptor prop : props) {
                        if (name.equals(prop.getName()) && prop.getWriteMethod() != null && prop.getReadMethod() != null) {
                            properties.add(new MethodProperty(converter, name, prop.getWriteMethod(), prop.getReadMethod()));
                            return;
                        }
                    }

                    // Try to find public field.
                    Field field = clazz.getField(name);
                    if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()))
                        properties.add(new FieldProperty(converter, name, field));

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public Object newJavaInstance() {

            if (type.length() == 0)
                return new HashMap<String, Object>();

            String className = type;
            try {
                return ClassUtil.newInstance(className);
            } catch (Exception e) {
                throw new RuntimeException("Could not create instance of: " + className, e);
            }
        }
    }

    /**
 * @author Franck WOLFF
     */
    public static class DefaultJavaClassDescriptor extends JavaClassDescriptor {

        public DefaultJavaClassDescriptor(Class<?> type) {
            super(type);
        }

        @Override
        protected List<Property> introspectProperties() {
            List<Property> properties = new ArrayList<Property>();
            Class<?> type = getType();

            if (!isExternalizable() && !Map.class.isAssignableFrom(type) && !Hashtable.class.isAssignableFrom(type)) {
                try {

                    Set<String> propertyNames = new HashSet<String>();

                    // Add read/write properties (ie: public getter/setter).
                    BeanInfo info = Introspector.getBeanInfo(type);
                    for (PropertyDescriptor property : info.getPropertyDescriptors()) {
                        String propertyName = property.getName();
                        if (property.getWriteMethod() != null && property.getReadMethod() != null) {
                            properties.add(new MethodProperty(converter, propertyName, property.getWriteMethod(), property.getReadMethod()));
                            propertyNames.add(propertyName);
                        }
                    }

                    // Add other public fields.
                    Field[] fields = type.getFields();
                    for (Field field : fields) {
                        String propertyName = field.getName();
                        if (!propertyNames.contains(propertyName) &&
                            !Modifier.isStatic(field.getModifiers()) &&
                            !Modifier.isTransient(field.getModifiers())) {
                            properties.add(new FieldProperty(converter, field.getName(), field));
                            propertyNames.add(propertyName);
                        }
                    }

                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return properties;
        }
    }

    /**
 * @author Franck WOLFF
     */
    public static class IndexedJavaClassDescriptor {

        private final int index;
        private final JavaClassDescriptor descriptor;

        public IndexedJavaClassDescriptor(int index, JavaClassDescriptor descriptor) {
            this.index = index;
            this.descriptor = descriptor;
        }

        public JavaClassDescriptor getDescriptor() {
            return descriptor;
        }

        public int getIndex() {
            return index;
        }
    }
}
