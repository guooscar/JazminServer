/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
/*
  Exadel AMF-serializer
  Copyright (C) 2008 Exadel, Inc.

  AMF-serializer is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation

  ObjectUtil.java
  Last modified by: $Author$
  $Revision$   $Date$
*/

package jazmin.server.msg.codec.amf;


import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
/**
 * 
 * @author yama
 *
 */
public class ObjectUtil {

    public static Method findServiceMethod(final Class<?> objectClass, final String methodName, final Object[] params)
        throws NoSuchMethodException {

        Method serviceMethod = null;
        if (params == null || params.length == 0)
            serviceMethod = objectClass.getMethod(methodName, (Class[]) null);
        else {
            for (Method method : objectClass.getMethods()) {

                if (!methodName.equals(method.getName()))
                    continue;

                Type[] paramTypes = method.getGenericParameterTypes();
                if (paramTypes.length != params.length)
                    continue;

                if (canConvertForMethodInvocation(params, paramTypes)) {
                    serviceMethod = method;
                    break;
                }
            }
        }

        if (serviceMethod == null)
            throw new NoSuchMethodException(objectClass.getName() + '.' + methodName + StringUtil.toString(params));

        return serviceMethod;
    }

    protected static boolean canConvertForMethodInvocation(Object[] values, Type[] targetTypes) {
        boolean canConvert = true;
        for (int i = 0; i < targetTypes.length && canConvert; i++)
            canConvert = canConvertForMethodInvocation(values[i], targetTypes[i]);
        return canConvert;
    }

    protected static boolean canConvertForMethodInvocation(Object value, Type targetType) {
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

}
