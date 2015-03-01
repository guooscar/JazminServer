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

/**
 * @author Franck WOLFF
 */
public abstract class Property {
    
    private final String name;
    private final Converter converter;
    
    protected Property(Converter converter, String name) {
        this.name = name;
        this.converter = converter;
        
    }

    public String getName() {
        return name;
    }
    
    protected Converter getConverter() {
        return converter;
    }

    public abstract void setProperty(Object instance, Object value);
    public abstract Object getProperty(Object instance);

    public Object convert(Object value, Class<?> desired) {
        return converter.convertForDeserialization(value, desired);
    }
}
