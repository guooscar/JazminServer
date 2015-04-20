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

public abstract class DataMessage extends AbstractMessage {

    private boolean encoded;
    protected ChannelBuffer data;

    public DataMessage() {
        super();
    }

    public DataMessage(final byte[] ... bytes) {
        data = ChannelBuffers.wrappedBuffer(bytes);
        header.setSize(data.readableBytes());
    }

    public DataMessage(final RtmpHeader header, final ChannelBuffer in) {
        super(header, in);
    }

    public DataMessage(final int time, final ChannelBuffer in) {        
        header.setTime(time);
        header.setSize(in.readableBytes());
        data = in;
    }

    @Override
    public ChannelBuffer encode() {
        if(encoded) {
            // in case used multiple times e.g. broadcast
            data.resetReaderIndex();            
        } else {
            encoded = true;
        }
        return data;
    }

    @Override
    public void decode(ChannelBuffer in) {
        data = in;
    }

    @Override
    public String toString() {
        return super.toString() + ChannelBuffers.hexDump(data);
    }

    public abstract boolean isConfig(); // TODO abstraction for audio / video ?

}
