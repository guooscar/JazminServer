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
import jazmin.server.rtmp.util.Utils;
import jazmin.server.rtmp.util.ValueToEnum;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
/**
 * 
 */
public class Control extends AbstractMessage {

    public static enum Type implements ValueToEnum.IntValue {
        
        STREAM_BEGIN(0),
        STREAM_EOF(1),
        STREAM_DRY(2),
        SET_BUFFER(3),
        STREAM_IS_RECORDED(4),
        PING_REQUEST(6),
        PING_RESPONSE(7),
        SWFV_REQUEST(26),
        SWFV_RESPONSE(27),
        BUFFER_EMPTY(31),
        BUFFER_FULL(32);

        private final int value;

        private Type(int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value;
        }

        private static final ValueToEnum<Type> converter = new ValueToEnum<Type>(Type.values());

        public static Type valueToEnum(final int value) {
            return converter.valueToEnum(value);
        }

    }

    private Type type;
    private int streamId;
    private int bufferLength;
    private int time;
    private byte[] bytes;

    public Control(RtmpHeader header, ChannelBuffer in) {
        super(header, in);
    }

    private Control(Type type, int time) {
        this.type = type;
        this.time = time;
    }

    private Control(int streamId, Type type) {
        this.streamId = streamId;
        this.type = type;
    }

    @Override
    MessageType getMessageType() {
        return MessageType.CONTROL;
    }

    public static Control setBuffer(int streamId, int bufferLength) {
        Control control = new Control(Type.SET_BUFFER, 0);
        control.bufferLength = bufferLength;
        control.streamId = streamId;
        return control;
    }

    public static Control pingRequest(int time) {
        return new Control(Type.PING_REQUEST, time);
    }

    public static Control pingResponse(int time) {
        return new Control(Type.PING_RESPONSE, time);
    }

    public static Control swfvResponse(byte[] bytes) {
        Control control = new Control(Type.SWFV_RESPONSE, 0);
        control.bytes = bytes;
        return control;
    }

    public static Control streamBegin(int streamId) {
        Control control = new Control(Type.STREAM_BEGIN, 0);
        control.streamId = streamId;
        return control;
    }

    public static Control streamIsRecorded(int streamId) {
        return new Control(streamId, Type.STREAM_IS_RECORDED);
    }

    public static Control streamEof(int streamId) {
        return new Control(streamId, Type.STREAM_EOF);
    }

    public static Control bufferEmpty(int streamId) {
        return new Control(streamId, Type.BUFFER_EMPTY);
    }

    public static Control bufferFull(int streamId) {
        return new Control(streamId, Type.BUFFER_FULL);
    }

    public Type getType() {
        return type;
    }

    public int getTime() {
        return time;
    }

    public int getBufferLength() {
        return bufferLength;
    }

    @Override
    public ChannelBuffer encode() {
        final int size;
        switch(type) {
            case SWFV_RESPONSE: size = 44; break;
            case SET_BUFFER: size = 10; break;
            default: size = 6;
        }
        ChannelBuffer out = ChannelBuffers.buffer(size);
        out.writeShort((short) type.value);
        switch(type) {
            case STREAM_BEGIN:
            case STREAM_EOF:
            case STREAM_DRY:
            case STREAM_IS_RECORDED:
                out.writeInt(streamId);
                break;
            case SET_BUFFER:
                out.writeInt(streamId);
                out.writeInt(bufferLength);
                break;
            case PING_REQUEST:
            case PING_RESPONSE:
                out.writeInt(time);
                break;
            case SWFV_REQUEST:                
                break;
            case SWFV_RESPONSE:
                out.writeBytes(bytes);
                break;
            case BUFFER_EMPTY:
            case BUFFER_FULL:
                out.writeInt(streamId);
                break;
        }
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {
        type = Type.valueToEnum(in.readShort());
        switch(type) {
            case STREAM_BEGIN:
            case STREAM_EOF:
            case STREAM_DRY:
            case STREAM_IS_RECORDED:
                streamId = in.readInt();
                break;
            case SET_BUFFER:
                streamId = in.readInt();
                bufferLength = in.readInt();
                break;
            case PING_REQUEST:
            case PING_RESPONSE:
                time = in.readInt();
                break;
            case SWFV_REQUEST:
                // only type (2 bytes)
                break;
            case SWFV_RESPONSE:
                bytes = new byte[42];
                in.readBytes(bytes);
                break;
            case BUFFER_EMPTY:
            case BUFFER_FULL:
                streamId = in.readInt();
                break;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(type);
        sb.append(" streamId: ").append(streamId);
        switch(type) {
            case SET_BUFFER:
                sb.append(" bufferLength: ").append(bufferLength);
                break;
            case PING_REQUEST:
            case PING_RESPONSE:
                sb.append(" time: ").append(time);
                break;
		default:
			break;
        }
        if(bytes != null) {
            sb.append(" bytes: " + Utils.toHex(bytes));
        }
        return sb.toString();
    }

}
