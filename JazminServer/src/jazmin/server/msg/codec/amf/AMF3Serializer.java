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

import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * @author Franck WOLFF
 */
public class AMF3Serializer extends DataOutputStream implements ObjectOutput, AMF3Constants {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.
    protected final Map<String, Integer> storedStrings = new HashMap<String, Integer>();
    protected final Map<Object, Integer> storedObjects = new IdentityHashMap<Object, Integer>();
    protected final Map<String, IndexedJavaClassDescriptor> storedClassDescriptors = new HashMap<String, IndexedJavaClassDescriptor>();

    protected final Converter converter = new DefaultConverter();

    protected final XMLUtil xmlUtil = new XMLUtil();

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    public AMF3Serializer(OutputStream out) {
        super(out);
    }

    ///////////////////////////////////////////////////////////////////////////
    // ObjectOutput implementation.

    public void writeObject(Object o) throws IOException {
    	
    	if (o == null)
            write(AMF3_NULL);
        else if (!(o instanceof Externalizable)) {

            o = converter.convertForSerialization(o);

            if (o instanceof String || o instanceof Character)
                writeAMF3String(o.toString());
            else if (o instanceof Boolean)
                write(((Boolean)o).booleanValue() ? AMF3_BOOLEAN_TRUE : AMF3_BOOLEAN_FALSE);
            else if (o instanceof Number) {
                if (o instanceof Integer || o instanceof Short || o instanceof Byte)
                    writeAMF3Integer(((Number)o).intValue());
                else
                    writeAMF3Number(((Number)o).doubleValue());
            }
            else if (o instanceof Date)
                writeAMF3Date((Date)o);
            else if (o instanceof Calendar)
                writeAMF3Date(((Calendar)o).getTime());
            else if (o instanceof Document)
                writeAMF3Xml((Document)o);
            else if (o instanceof Collection)
                writeAMF3Collection((Collection<?>)o);
            else if (o.getClass().isArray()) {
                if (o.getClass().getComponentType() == Byte.TYPE)
                    writeAMF3ByteArray((byte[])o);
                else
                    writeAMF3Array(o);
            }
            else
                writeAMF3Object(o);
        } else
            writeAMF3Object(o);
    }

    ///////////////////////////////////////////////////////////////////////////
    // AMF3 serialization.
    protected void writeAMF3Integer(int i) throws IOException {
    	if (i < AMF3_INTEGER_MIN || i > AMF3_INTEGER_MAX) {
            writeAMF3Number(i);
    	} else {
            write(AMF3_INTEGER);
            writeAMF3IntegerData(i);
        }
    }

    protected void writeAMF3IntegerData(int i) throws IOException {
    	
        if (i < AMF3_INTEGER_MIN || i > AMF3_INTEGER_MAX)
            throw new IllegalArgumentException("Integer out of range: " + i);

        if (i < 0 || i >= 0x200000) {
            write(((i >> 22) & 0x7F) | 0x80);
            write(((i >> 15) & 0x7F) | 0x80);
            write(((i >> 8) & 0x7F) | 0x80);
            write(i & 0xFF);
        } else {
            if (i >= 0x4000)
                write(((i >> 14) & 0x7F) | 0x80);
            if (i >= 0x80)
                write(((i >> 7) & 0x7F) | 0x80);
            write(i & 0x7F);
        }
    }

    protected void writeAMF3Number(double d) throws IOException {
    	
    	write(AMF3_NUMBER);
        writeDouble(d);
    }

    protected void writeAMF3String(String s) throws IOException {
    	
        write(AMF3_STRING);
        writeAMF3StringData(s);
    }

    protected void writeAMF3StringData(String s) throws IOException {
    	
    	if (s.length() == 0) {
            write(0x01);
            return;
        }

        int index = indexOfStoredStrings(s);

        if (index >= 0)
            writeAMF3IntegerData(index << 1);
        else {
            addToStoredStrings(s);

            final int sLength = s.length();

            // Compute and write modified UTF-8 string length.
            int uLength = 0;
            for (int i = 0; i < sLength; i++) {
                int c = s.charAt(i);
                if (c <= 0x00007F) {						// 0x00000000-0x0000007F(0xxxxxxx)
                	uLength++;
    			} else if (c <= 0x0007FF) {				// 0x00000080-0x000007FF(110xxxxx 10xxxxxx)
    				 uLength += 2;
    			} else if (c <= 0x00FFFF) {				// 0x00000800-0x0000FFFF(1110xxxx 10xxxxxx 10xxxxxx)
    				 uLength += 3;
    			} else if (c <= 0x10FFFF) {				// 0x00010000-0x0010FFFF(11110xxx 10xxxxxx 10xxxxxx 10xxxxxx)
    				 uLength += 4;
    			} 
            }
            writeAMF3IntegerData((uLength << 1) | 0x01);

            // Write modified UTF-8 bytes.
            for (int i = 0; i < sLength; i++) {
                int c = s.charAt(i);
               // Encode as UTF-8 (According to AMF3 specification.)
    			if (c <= 0x00007F) {						// 0x00000000-0x0000007F(0xxxxxxx)
    				write(c);
    			} else if (c <= 0x0007FF) {				// 0x00000080-0x000007FF(110xxxxx 10xxxxxx)
    				write ( (byte) (0xC0 | (c >>>  6) & 0x1F));
    				write ( (byte) (0x80 |  c         & 0x3F));
    			} else if (c <= 0x00FFFF) {				// 0x00000800-0x0000FFFF(1110xxxx 10xxxxxx 10xxxxxx)
    				write ( (byte) (0xE0 | (c >>> 12) & 0x0F));
    				write ( (byte) (0x80 | (c >>>  6) & 0x3F));
    				write ( (byte) (0x80 |  c         & 0x3F));
    			} else if (c <= 0x10FFFF) {				// 0x00010000-0x0010FFFF(11110xxx 10xxxxxx 10xxxxxx 10xxxxxx)
    				write ( (byte) (0xF0 | (c >>> 18) & 0x07));
    				write ( (byte) (0x80 | (c >>> 12) & 0x3F));
    				write ( (byte) (0x80 | (c >>>  6) & 0x3F));
    				write ( (byte) (0x80 |  c         & 0x3F));
    			} 
            }
        }
    }

    protected void writeAMF3Xml(Document doc) throws IOException {
    	
        write(AMF3_XML);

        int index = indexOfStoredObjects(doc);
        if (index >= 0)
            writeAMF3IntegerData(index << 1);
        else {
            addToStoredObjects(doc);

            byte[] bytes = xmlUtil.toString(doc).getBytes("UTF-8");
            writeAMF3IntegerData((bytes.length << 1) | 0x01);
            write(bytes);
        }
    }

    protected void writeAMF3Date(Date date) throws IOException {
    	writeAMF3Number(date.getTime());
    }

    protected void writeAMF3Array(Object array) throws IOException {
  
        write(AMF3_ARRAY);

        int index = indexOfStoredObjects(array);
        if (index >= 0)
            writeAMF3IntegerData(index << 1);
        else {
            addToStoredObjects(array);

            int length = Array.getLength(array);
            writeAMF3IntegerData(length << 1 | 0x01);
            write(0x01);
            for (int i = 0; i < length; i++)
                writeObject(Array.get(array, i));
        }
    }

    protected void writeAMF3ByteArray(byte[] bytes) throws IOException {
    	
        write(AMF3_BYTEARRAY);

        int index = indexOfStoredObjects(bytes);
        if (index >= 0)
            writeAMF3IntegerData(index << 1);
        else {
            addToStoredObjects(bytes);

            writeAMF3IntegerData(bytes.length << 1 | 0x01);
            write(bytes);
        }
    }

    protected void writeAMF3Collection(Collection<?> c) throws IOException {
    	writeAMF3Array(c.toArray());
     //   ArrayCollection ac = (c instanceof ArrayCollection ? (ArrayCollection)c : new ArrayCollection(c));
      //  writeAMF3Object(ac);
    }

    protected void writeAMF3Object(Object o) throws IOException {
    	
        write(AMF3_OBJECT);

        int index = indexOfStoredObjects(o);
        if (index >= 0)
            writeAMF3IntegerData(index << 1);
        else {
            addToStoredObjects(o);

            //ClassGetter classGetter = context.getGraniteConfig().getClassGetter();
            ClassGetter classGetter = new DefaultClassGetter();

            
            Class<?> oClass = classGetter.getClass(o);
         
            JavaClassDescriptor desc = null;

            // write class description.
            IndexedJavaClassDescriptor iDesc = getFromStoredClassDescriptors(oClass);
            if (iDesc != null) {
                desc = iDesc.getDescriptor();
                writeAMF3IntegerData(iDesc.getIndex() << 2 | 0x01);
            }
            else {
                iDesc = addToStoredClassDescriptors(oClass);
                desc = iDesc.getDescriptor();

                writeAMF3IntegerData((desc.getPropertiesCount() << 4) | (desc.getEncoding() << 2) | 0x03);
                writeAMF3StringData(desc.getName());

                for (int i = 0; i < desc.getPropertiesCount(); i++)
                    writeAMF3StringData(desc.getPropertyName(i));
            }
//            if (debug) debug("writeAMF3Object() - desc=", desc);

            // write object content.
            if (desc.isExternalizable()) {
                Externalizer externalizer = desc.getExternalizer();

                if (externalizer != null) {
//                    if (debug) debug("writeAMF3Object() - using externalizer=", externalizer);
                    try {
                        externalizer.writeExternal(o, this);
                    } catch (IOException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException("Could not externalize object: " + o, e);
                    }
                }
                else {
//                    if (debug) debug("writeAMF3Object() - legacy Externalizable=", o);
                    ((Externalizable)o).writeExternal(this);
                }
            }
            else {
//            	if (debug) debug("writeAMF3Object() - writing defined properties...");
                for (int i = 0; i < desc.getPropertiesCount(); i++) {
                    Object obj = desc.getPropertyValue(i, o);
//                    if (debug) debug(
//                    	"writeAMF3Object() - writing defined property: ", desc.getPropertyName(i),
//                    	"=", obj
//                    );
                    writeObject(obj);
                }

                if (desc.isDynamic()) {
//                	if (debug) debug("writeAMF3Object() - writing dynamic properties...");
                    Map<?, ?> oMap = (Map<?, ?>)o;
                    for (Map.Entry<?, ?> entry : oMap.entrySet()) {
                        Object key = entry.getKey();
                        if (key != null) {
                            String propertyName = key.toString();
                            if (propertyName.length() > 0) {
//                            	if (debug) debug(
//                            		"writeAMF3Object() - writing dynamic property: ", propertyName,
//                            		"=", entry.getValue()
//                            	);
                                writeAMF3StringData(propertyName);
                                writeObject(entry.getValue());
                            }
                        }
                    }
                    writeAMF3StringData("");
                }
            }
        }

//        if (debug) debug("writeAMF3Object(o=", o, ") - Done.");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Cached objects methods.

    protected void addToStoredStrings(String s) {
        if (!storedStrings.containsKey(s)) {
        	Integer index = Integer.valueOf(storedStrings.size());
//        	if (debug) debug(
//        		"addToStoredStrings(s=", StringUtil.toString(s), ") at index=", index.toString()
//        	);
            storedStrings.put(s, index);
        }
    }

    protected int indexOfStoredStrings(String s) {
        Integer index = storedStrings.get(s);
//        if (debug) debug(
//        	"indexOfStoredStrings(s=", StringUtil.toString(s), ") -> ",
//        	String.valueOf((index != null ? index.intValue() : -1))
//        );
        return (index != null ? index.intValue() : -1);
    }

    protected void addToStoredObjects(Object o) {
        if (o != null && !storedObjects.containsKey(o)) {
        	Integer index = Integer.valueOf(storedObjects.size());
//        	if (debug) debug("addToStoredObjects(o=", o, ") at index=", index.toString());
            storedObjects.put(o, index);
        }
    }

    protected int indexOfStoredObjects(Object o) {
        Integer index = storedObjects.get(o);
//        if (debug) debug(
//        	"indexOfStoredObjects(o=", o, ") -> ",
//        	String.valueOf((index != null ? index.intValue() : -1))
//        );
        return (index != null ? index.intValue() : -1);
    }

    protected IndexedJavaClassDescriptor addToStoredClassDescriptors(Class<?> clazz) {
        final String name = JavaClassDescriptor.getClassName(clazz);

//        if (debug) debug("addToStoredClassDescriptors(clazz=", clazz, ")");

        if (storedClassDescriptors.containsKey(name))
            throw new RuntimeException(
            	"Descriptor of \"" + name + "\" is already stored at index: " +
            	getFromStoredClassDescriptors(clazz).getIndex()
            );

        // find custom class descriptor and instantiate if any
        JavaClassDescriptor desc = null;

/*
        Class<? extends JavaClassDescriptor> descriptorType
        	= context.getGraniteConfig().getJavaDescriptor(clazz.getName());
        if (descriptorType != null) {
            Class<?>[] argsDef = new Class[]{Class.class};
            Object[] argsVal = new Object[]{clazz};
            try {
                desc = ClassUtil.newInstance(descriptorType, argsDef, argsVal);
            } catch (Exception e) {
                throw new RuntimeException("Could not instantiate Java descriptor: " + descriptorType);
            }
        }
*/

        if (desc == null)
            desc = new DefaultJavaClassDescriptor(clazz);

        IndexedJavaClassDescriptor iDesc = new IndexedJavaClassDescriptor(storedClassDescriptors.size(), desc);

//        if (debug) debug("addToStoredClassDescriptors() - putting: name=", name, ", iDesc=", iDesc);

        storedClassDescriptors.put(name, iDesc);

        return iDesc;
    }

    protected IndexedJavaClassDescriptor getFromStoredClassDescriptors(Class<?> clazz) {
//    	if (debug) debug("getFromStoredClassDescriptors(clazz=", clazz, ")");

    	String name = JavaClassDescriptor.getClassName(clazz);
    	IndexedJavaClassDescriptor iDesc = storedClassDescriptors.get(name);

//    	if (debug) debug("getFromStoredClassDescriptors() -> ", iDesc);

    	return iDesc;
    }
}
