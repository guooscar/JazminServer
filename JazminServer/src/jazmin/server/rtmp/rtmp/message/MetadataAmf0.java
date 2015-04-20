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

import java.util.ArrayList;
import java.util.List;

import jazmin.server.rtmp.amf.Amf0Value;
import jazmin.server.rtmp.rtmp.RtmpHeader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class MetadataAmf0 extends Metadata {        

    public MetadataAmf0(String name, Object... data) {
        super(name, data);
    }

    public MetadataAmf0(RtmpHeader header, ChannelBuffer in) {
        super(header, in);
    }

    @Override
    MessageType getMessageType() {
        return MessageType.METADATA_AMF0;
    }

    @Override
    public ChannelBuffer encode() {
        ChannelBuffer out = ChannelBuffers.dynamicBuffer();
        Amf0Value.encode(out, name);
        Amf0Value.encode(out, data);
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {
        name = (String) Amf0Value.decode(in);
        List<Object> list = new ArrayList<Object>();
        while(in.readable()) {
            list.add(Amf0Value.decode(in));
        }
        data = list.toArray();
    }

}
