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
import jazmin.server.rtmp.rtmp.RtmpMessage;
import jazmin.server.rtmp.util.ValueToEnum;

import org.jboss.netty.buffer.ChannelBuffer;

public enum MessageType implements ValueToEnum.IntValue {

    CHUNK_SIZE(0x01),
    ABORT(0x02),
    BYTES_READ(0x03),
    CONTROL(0x04),
    WINDOW_ACK_SIZE(0x05),
    SET_PEER_BW(0x06),
    // unknown 0x07
    AUDIO(0x08),
    VIDEO(0x09),
    // unknown 0x0A - 0x0E
    METADATA_AMF3(0x0F),
    SHARED_OBJECT_AMF3(0x10),
    COMMAND_AMF3(0x11),
    METADATA_AMF0(0x12),
    SHARED_OBJECT_AMF0(0x13),
    COMMAND_AMF0(0x14),
    AGGREGATE(0x16);

    private final int value;

    private MessageType(final int value) {
        this.value = value;
    }

    @Override
    public int intValue() {
        return value;
    }
    
    public int getDefaultChannelId() {
        switch(this) {
            case CHUNK_SIZE:
            case CONTROL:
            case ABORT:
            case BYTES_READ:
            case WINDOW_ACK_SIZE:
            case SET_PEER_BW:            
                return 2;
            case COMMAND_AMF0:
            case COMMAND_AMF3: // TODO verify
                return 3;
            case METADATA_AMF0:
            case METADATA_AMF3: // TODO verify
            case AUDIO:
            case VIDEO:
            case AGGREGATE:
            default: // TODO verify
                return 5;
        }
    }

    public static RtmpMessage decode(final RtmpHeader header, final ChannelBuffer in) {
        switch(header.getMessageType()) {
            case ABORT: return new Abort(header, in);
            case BYTES_READ: return new BytesRead(header, in);
            case CHUNK_SIZE: return new ChunkSize(header, in);
            case COMMAND_AMF0: return new CommandAmf0(header, in);
            case METADATA_AMF0: return new MetadataAmf0(header, in);
            case CONTROL: return new Control(header, in);
            case WINDOW_ACK_SIZE: return new WindowAckSize(header, in);
            case SET_PEER_BW: return new SetPeerBw(header, in);
            case AUDIO: return new Audio(header, in);
            case VIDEO: return new Video(header, in);
            case AGGREGATE: return new Aggregate(header, in);
            default: throw new RuntimeException("unable to create message for: " + header);
        }
    }

    private static final ValueToEnum<MessageType> converter = new ValueToEnum<MessageType>(MessageType.values());

    public static MessageType valueToEnum(final int value) {
        return converter.valueToEnum(value);
    }

}
