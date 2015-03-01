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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Franck WOLFF
 */
public class BasicMap<K, V> extends HashMap<K, V> implements Externalizable, Serializable {

	public BasicMap() {
		super();
	}
	
	public BasicMap(Map<K, V> map) {
		super(map);
	}

	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Object[] pairs = (Object[])in.readObject();
        if (pairs != null) {
            for (Object pair : pairs)
            	put((K)((Object[])pair)[0], (V)((Object[])pair)[1]);
        }
	}

	public void writeExternal(ObjectOutput out) throws IOException {
        Object[] outObjectArray = new Object[size()];
        
        int index = 0;
        for (Map.Entry<K, V> entry : entrySet())
            outObjectArray[index++] = new Object[]{entry.getKey(), entry.getValue()};
        
        out.writeObject(outObjectArray);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static BasicMap<?, ?> newInstance(Map<?, ?> map) {
		return new BasicMap(map);
	}

	@Override
	public String toString() {
		return getClass().getName() + " " + super.toString();
	}
}
