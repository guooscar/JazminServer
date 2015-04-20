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

public class SetPeerBw extends AbstractMessage {

    public static enum LimitType {
        HARD, // 0
        SOFT, // 1
        DYNAMIC // 2
    }

    private int value;
    private LimitType limitType;

    public SetPeerBw(RtmpHeader header, ChannelBuffer in) {
        super(header, in);
    }

    public SetPeerBw(int value, LimitType limitType) {
        this.value = value;
        this.limitType = limitType;
    }

    public static SetPeerBw dynamic(int value) {
        return new SetPeerBw(value, LimitType.DYNAMIC);
    }

    public static SetPeerBw hard(int value) {
        return new SetPeerBw(value, LimitType.HARD);
    }

    public int getValue() {
        return value;
    }

    @Override
    MessageType getMessageType() {
        return MessageType.SET_PEER_BW;
    }

    @Override
    public ChannelBuffer encode() {
        ChannelBuffer out = ChannelBuffers.buffer(5);
        out.writeInt(value);
        out.writeByte((byte) limitType.ordinal());
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {
        value = in.readInt();
        limitType = LimitType.values()[in.readByte()];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("windowSize: ").append(value);
        sb.append(" limitType: ").append(limitType);
        return sb.toString();
    }

}
