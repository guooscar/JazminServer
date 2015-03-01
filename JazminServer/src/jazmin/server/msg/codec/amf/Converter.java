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

import java.lang.reflect.Type;

/**
 * This class is used for various type conversions (assignments, serialization and service
 * method calls). Implementations must be thread safe (stateless).
 *
 * @author Franck WOLFF
 */
public interface Converter {

    /**
     * This method is called by the AMF3 serializer for each non null and non
     * {@link java.io.Externalizable} object going to be serialized. You may
     * implement this method in order to add custom type support.
     * 
     * @param value the object to be serialized.
     * @return the (possibly) converted object.
     */
    public Object convertForSerialization(Object value);
    
    /**
     * This method is called for field assignment (either by direct reflection
     * access or via a setter) or method parameters adjustments on service
     * method calls.
     * 
     * This method should conform to the rules defined in the Java Language Specification,
     * Third Edition, <a
     * href="http://java.sun.com/docs/books/jls/third_edition/html/conversions.html#5.3">
     * 5.3 Method Invocation Conversion</a>
     * 
     * @param value the object to be assigned or passed as parameter.
     * @param targetType the target type.
     * @return the (possibly) converted object.
     */
    public Object convertForDeserialization(Object value, Type targetType);

}

    
