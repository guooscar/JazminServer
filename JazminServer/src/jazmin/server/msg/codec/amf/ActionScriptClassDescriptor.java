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


import java.util.ArrayList;
import java.util.List;

/**
 * @author Franck WOLFF
 */
public abstract class ActionScriptClassDescriptor {

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
//        if (encoding != 0x01)
//            return null;
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
