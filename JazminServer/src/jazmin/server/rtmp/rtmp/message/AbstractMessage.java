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

package jazmin.server.rtmp.rtmp.message;

import java.util.LinkedHashMap;
import java.util.Map;

import jazmin.server.rtmp.amf.Amf0Object;
import jazmin.server.rtmp.rtmp.RtmpHeader;
import jazmin.server.rtmp.rtmp.RtmpMessage;

import org.jboss.netty.buffer.ChannelBuffer;

public abstract class AbstractMessage implements RtmpMessage {
    
    protected final RtmpHeader header;

    public AbstractMessage() {
        header = new RtmpHeader(getMessageType());
    }

    public AbstractMessage(RtmpHeader header, ChannelBuffer in) {
        this.header = header;
        decode(in);
    }

    @Override
    public RtmpHeader getHeader() {
        return header;
    }

    abstract MessageType getMessageType();

    @Override
    public String toString() {
        return header.toString() + ' ';
    }

    //==========================================================================

    public static Amf0Object object(Amf0Object object, Pair ... pairs) {
        if(pairs != null) {
            for(Pair pair : pairs) {
                object.put(pair.name, pair.value);
            }
        }
        return object;
    }

    public static Amf0Object object(Pair ... pairs) {
        return object(new Amf0Object(), pairs);
    }

    public static Map<String, Object> map(Map<String, Object> map, Pair ... pairs) {
        if(pairs != null) {
            for(Pair pair : pairs) {
                map.put(pair.name, pair.value);
            }
        }
        return map;
    }

    public static Map<String, Object> map(Pair ... pairs) {
        return map(new LinkedHashMap<String, Object>(), pairs);
    }

    public static class Pair {
        String name;
        Object value;
    }

    public static Pair pair(String name, Object value) {
        Pair pair = new Pair();
        pair.name = name;
        pair.value = value;
        return pair;
    }



}
