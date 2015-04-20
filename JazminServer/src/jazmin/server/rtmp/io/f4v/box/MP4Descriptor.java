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

package jazmin.server.rtmp.io.f4v.box;

import jazmin.server.rtmp.util.Utils;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * thanks to Paul Kendall for the patch !
 */
public class MP4Descriptor {

    public final static int ES_TAG = 3;
    public final static int DECODER_CONFIG = 4;
    public final static int DECODER_SPECIFIC_CONFIG = 5;
    private byte[] decoderSpecificConfig = Utils.fromHex("af0013100000");

    public MP4Descriptor(ChannelBuffer in) {
        final int size = in.readInt();
        in.readInt(); // check that this is in-fact "esds"
        in.readInt(); // version and flags
        while (in.readable()) {
            readDescriptor(in, size - 12);
        }
    }

    public byte[] getConfigBytes() {
        return decoderSpecificConfig;
    }

    private int readDescriptor(ChannelBuffer bitstream, int length) {
        final int tag = bitstream.readByte();
        int size = 0;
        int b = 0;
        int read = 1;
        do {
            b = bitstream.readByte();
            size <<= 7;
            size |= b & 0x7f;
            read++;
        } while ((b & 0x80) == 0x80);
        switch (tag) {
            case ES_TAG:
                return parseES(bitstream, length - read) + read;
            case DECODER_CONFIG:
                return parseDecoderConfig(bitstream, length - read) + read;
            case DECODER_SPECIFIC_CONFIG:
                return parseDecoderSpecificConfig(bitstream, size, length - read) + read;
            default:
                bitstream.skipBytes(size);
                return size + read;
        }
    }

    private int parseES(ChannelBuffer bitstream, int length) {
        int read = 3;
        bitstream.readShort();//int esid
        int flags = bitstream.readByte();
        boolean streamDependenceFlag = (flags & (1 << 7)) != 0;
        boolean urlFlag = (flags & (1 << 6)) != 0;
        boolean ocrFlag = (flags & (1 << 5)) != 0;
        if (streamDependenceFlag) {
            bitstream.skipBytes(2);
            read += 2;
        }
        if (urlFlag) {
            int str_size = bitstream.readByte();
            bitstream.skipBytes(str_size);
            read += str_size;
        }
        if (ocrFlag) {
            bitstream.skipBytes(2);
            read += 2;
        }
        while (bitstream.readableBytes() > length - read) {
            read += readDescriptor(bitstream, length - read);
        }
        return read;
    }

    private int parseDecoderConfig(ChannelBuffer bitstream, int length) {
        bitstream.readByte();//final int objectTypeIndication = 
        bitstream.readByte();//int value =
        //final boolean upstream = (value & (1 << 1)) > 0;
        //final byte streamType = (byte) (value >> 2);
        bitstream.readShort();//  value =
        //int bufferSizeDB = value << 8;
        bitstream.readByte();//value = 
        // bufferSizeDB |= value & 0xff;
         bitstream.readInt();//final int maxBitRate =
         bitstream.readInt();//final int minBitRate =
        int read = 13;
        while (bitstream.readableBytes() > length - 13) {
            read += readDescriptor(bitstream, length - 13);
        }
        return read;
    }

    private int parseDecoderSpecificConfig(ChannelBuffer bitstream, int size, int length) {
        decoderSpecificConfig = new byte[size];
        bitstream.readBytes(decoderSpecificConfig);
        return size;
    }

}
