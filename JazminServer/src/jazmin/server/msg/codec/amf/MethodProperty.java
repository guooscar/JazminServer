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

import java.lang.reflect.Method;

/**
 * @author Franck WOLFF
 */
public class MethodProperty extends Property {

    private final Method setter;
    private final Method getter;
    
    public MethodProperty(Converter converter, String name, Method setter, Method getter) {
        super(converter, name);
        this.setter = setter;
        this.getter = getter;
    }

    @Override
    public void setProperty(Object instance, Object value) {
        try {
            setter.invoke(instance, new Object[]{convert(value, setter.getParameterTypes()[0])});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getProperty(Object instance) {
        try {
            return getter.invoke(instance, new Object[0]);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
