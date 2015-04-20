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

import jazmin.server.rtmp.rtmp.RtmpHeader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class WindowAckSize extends AbstractMessage {
    
    private int value;
    
    public WindowAckSize(RtmpHeader header, ChannelBuffer in) {
        super(header, in);
    }
    
    public WindowAckSize(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    MessageType getMessageType() {
        return MessageType.WINDOW_ACK_SIZE;
    }

    @Override
    public ChannelBuffer encode() {
        ChannelBuffer out = ChannelBuffers.buffer(4);
        out.writeInt(value);
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {
        value = in.readInt();
    }

    @Override
    public String toString() {
        return super.toString() + value;
    }

}
