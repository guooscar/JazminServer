/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package jazmin.server.rtmp.util;

import java.util.Arrays;

import jazmin.server.rtmp.amf.IntValue;

/**
 * a little bit of code reuse, would have been cleaner if enum types
 * could extend some other class - we implement an interface instead
 * and have to construct a static instance in each enum type we use
 */
public class ValueToEnum<T extends Enum<T> & IntValue> {

   
	
    private final Enum<?>[] lookupArray;
    private final int maxIndex;

    public ValueToEnum(final T[] enumValues) {
        final int[] lookupIndexes = new int[enumValues.length];
        for(int i = 0; i < enumValues.length; i++) {
            lookupIndexes[i] = enumValues[i].intValue();
        }
        Arrays.sort(lookupIndexes);
        maxIndex = lookupIndexes[lookupIndexes.length - 1];        
        lookupArray = new Enum[maxIndex + 1]; // use 1 based index
        for (final T t : enumValues) {
            lookupArray[t.intValue()] = t;
        }        
    }

    @SuppressWarnings("unchecked")
	public T valueToEnum(final int i) {
        final T t;
        try {
            t = (T) lookupArray[i];
        } catch(Exception e) { // index out of bounds
            throw new RuntimeException(getErrorLogMessage(i) + ", " + e);
        }
        if (t == null) {
            throw new RuntimeException(getErrorLogMessage(i) + ", no match found in lookup");
        }
        return t;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    private String getErrorLogMessage(final int i) {
        return "bad value / byte: " + i + " (hex: " + Utils.toHex((byte) i) + ")";
    }

}
