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
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Franck WOLFF
 */
public class StringUtil {
	
	public static final String HEX_CHARS = "0123456789ABCDEF";

	public static String toHexString(Number n) {
		if (n == null)
			return "null";
		
		byte[] bytes = new byte[8];
        ByteBuffer bytesBuffer = ByteBuffer.wrap(bytes);
        LongBuffer longBuffer = bytesBuffer.asLongBuffer();
        longBuffer.put(0, n.longValue());

		StringBuilder sb = new StringBuilder(16);
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i] & 0xFF;
			if (b != 0 || sb.length() > 0 || i == (bytes.length - 1))
				sb.append(HEX_CHARS.charAt(b >> 4)).append(HEX_CHARS.charAt(b & 0x0F));
		}
		return sb.toString();
	}
	
	public static String toString(Object o) {
		return toString(o, -1);
	}
	
	public static String toString(Object o, int maxItems) {
		if (o == null)
			return "null";
		
		if (o instanceof String)
			return ("\"" + o + "\"");
		
		if (o instanceof Character || o.getClass() == Character.TYPE)
			return ("'" + o + "'");
		
		if (o instanceof Number) {
			if (o instanceof Byte || o instanceof Short || o instanceof Integer || o instanceof Long)
				return o + " <0x" + toHexString((Number)o) + ">";
			return String.valueOf(o);
		}
		
		if (o.getClass().isArray()) {
			Class<?> type = o.getClass().getComponentType();
			
			if (maxItems < 0) {
				if (type.isPrimitive()) {
					if (type == Byte.TYPE)
						return Arrays.toString((byte[])o);
					if (type == Character.TYPE)
						return Arrays.toString((char[])o);
					if (type == Integer.TYPE)
						 return Arrays.toString((int[])o);
					if (type == Double.TYPE)
						return Arrays.toString((double[])o);
					if (type == Long.TYPE)
						return Arrays.toString((long[])o);
					if (type == Float.TYPE)
						return Arrays.toString((float[])o);
					if (type == Short.TYPE)
						return Arrays.toString((short[])o);
					if (type == Boolean.TYPE)
						return Arrays.toString((boolean[])o);
					return "[Array of unknown primitive type: " + type + "]"; // Should never append...
				}
				return Arrays.toString((Object[])o);
			}

			final int max = Math.min(maxItems, Array.getLength(o));
			List<Object> list = new ArrayList<Object>(max);
			
			for (int i = 0; i < max; i++)
				list.add(Array.get(o, i));
			if (max < Array.getLength(o))
				list.add("(first " + max + '/' + Array.getLength(o) + " elements only...)");
			
			o = list;
		}
		else if (o instanceof Collection && maxItems >= 0) {
			
			Collection<?> coll = (Collection<?>)o;
			final int max = Math.min(maxItems, coll.size());
			List<Object> list = new ArrayList<Object>(max);
			
			int i = 0;
			for (Object item : coll) {
				if (i >= max) {
					list.add("(first " + max + '/' + coll.size() + " elements only...)");
					break;
				}
				list.add(item);
				i++;
			}
			
			o = list;
		}
		else if (o instanceof Map && maxItems >= 0) {
			Map<?, ?> map = (Map<?, ?>)o;
			final int max = Math.min(maxItems, map.size());
			Map<Object, Object> copy = new HashMap<Object, Object>(max);
			
			int i = 0;
			for (Map.Entry<?, ?> item : map.entrySet()) {
				if (i >= max) {
					copy.put("(first " + max + '/' + map.size() + " elements only...)", "...");
					break;
				}
				copy.put(item.getKey(), item.getValue());
				i++;
			}
			
			o = copy;
		}
		
		return String.valueOf(o);
	}
}
