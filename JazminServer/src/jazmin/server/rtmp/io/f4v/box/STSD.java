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

import java.util.ArrayList;
import java.util.List;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.rtmp.io.f4v.Payload;
import jazmin.server.rtmp.io.f4v.SampleType;
import jazmin.server.rtmp.util.Utils;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class STSD implements Payload {

    private static final Logger logger = LoggerFactory.getLogger(STSD.class);

    public static class STSDRecord {
        private SampleType type;
        private Payload sampleDescription;
    }

    public STSD(ChannelBuffer in) {
        read(in);
    }

    private List<STSDRecord> records;

    public List<STSDRecord> getRecords() {
        return records;
    }

    // TODO model Sample Description better, commonize first 10 bytes
    public Payload getSampleDescription(int index) {
        return records.get(index - 1).sampleDescription;
    }

    public SampleType getSampleType(int index) {
        STSDRecord record = records.get(index - 1);
        return record.type;
    }

    public String getSampleTypeString(int index) {
        return getSampleType(index).name().toLowerCase();
    }

    @Override
    public void read(ChannelBuffer in) {
        in.readInt(); // UI8 version + UI24 flags
        final int count = in.readInt();
        logger.debug("no of sample descripton records: {}", count);
        records = new ArrayList<STSDRecord>(count);
        for (int i = 0; i < count; i++) {
            final int descSize = in.readInt();
            final byte[] typeBytes = new byte[4];
            in.readBytes(typeBytes);
            STSDRecord record = new STSDRecord();
            record.type = SampleType.parse(new String(typeBytes));
            final int payloadSize = descSize - 8;
            if(record.type.isVideo()) {
                record.sampleDescription = new VideoSD(in.readBytes(payloadSize));
            } else {
                record.sampleDescription = new AudioSD(in.readBytes(payloadSize));
            }
            logger.debug("sample description: {}, {}", record.type, record.sampleDescription);
            records.add(record);
        }
    }

    @Override
    public ChannelBuffer write() {
        ChannelBuffer out = ChannelBuffers.dynamicBuffer();
        out.writeInt(0); // UI8 version + UI24 flags
        out.writeInt(records.size());
        for (STSDRecord record : records) {
            ChannelBuffer desc = record.sampleDescription.write();
            out.writeInt(8 + desc.readableBytes());
            out.writeBytes(record.type.name().toLowerCase().getBytes());
            out.writeBytes(desc);
        }
        return out;
    }
    
    //==========================================================================

    public static class AudioSD implements Payload {

    	private short index;
        private short innerVersion;
        private short revisionLevel;
        private int vendor;
        private short channelCount;
        private short sampleSize;
        private short compressionId;
        private short packetSize;
        private int sampleRate;
        private int samplesPerPacket;
        private int bytesPerPacket;
        private int bytesPerFrame;
        private int samplesPerFrame;
        private MP4Descriptor mp4Descriptor;

        public AudioSD(ChannelBuffer in) {
            read(in);
        }

        public byte[] getConfigBytes() {
            return mp4Descriptor.getConfigBytes();
        }
        
        @Override
        public void read(ChannelBuffer in) {
            in.skipBytes(6); // reserved
            index = in.readShort();
            innerVersion = in.readShort();
            revisionLevel = in.readShort();
            vendor = in.readInt();
            channelCount = in.readShort();
            sampleSize = in.readShort();
            compressionId = in.readShort();
            packetSize = in.readShort();
            sampleRate = in.readInt();
            if (innerVersion != 0) {
            	samplesPerPacket = in.readInt();
            	bytesPerPacket = in.readInt();
            	bytesPerFrame = in.readInt();
            	samplesPerFrame = in.readInt();
            }
            mp4Descriptor = new MP4Descriptor(in);
        }

        @Override
        public ChannelBuffer write() {
            ChannelBuffer out = ChannelBuffers.dynamicBuffer();
            out.writeBytes(new byte[6]);
            out.writeShort(index);
            out.writeShort(innerVersion);
            out.writeShort(revisionLevel);
            out.writeInt(vendor);
            out.writeShort(channelCount);
            out.writeShort(sampleSize);
            out.writeShort(compressionId);
            out.writeShort(packetSize);
            out.writeInt(sampleRate);
            if (innerVersion != 0) {
            	out.writeInt(samplesPerPacket);
            	out.writeInt(bytesPerPacket);
            	out.writeInt(bytesPerFrame);
            	out.writeInt(samplesPerFrame);
            }
            return out;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[channelCount: ").append(channelCount);
            sb.append(" sampleSize: ").append(sampleSize);
            sb.append(" sampleRate: ").append(sampleRate);
            sb.append(']');
            return sb.toString();
        }

    }

    //==========================================================================

    public static class VideoSD implements Payload {

        private short index;
        private short preDefined1;
        private short reserved1;
        private int preDefined2;
        private int preDefined3;
        private int preDefined4;
        private short width;
        private short height;
        private int horizontalResolution;
        private int verticalResolution;
        private int reserved2;
        private short frameCount;
        private String compressorName;
        private short depth;
        private short preDefined5;
        private byte[] configType;
        private byte[] configBytes;

        public short getWidth() {
            return width;
        }

        public short getHeight() {
            return height;
        }

        public byte[] getConfigBytes() {
            return configBytes;
        }

        public VideoSD(ChannelBuffer in) {
            read(in);
        }

        @Override
        public void read(ChannelBuffer in) {
            in.skipBytes(6); // reserved
            index = in.readShort();
            preDefined1 = in.readShort();
            reserved1 = in.readShort();
            preDefined2 = in.readInt();
            preDefined3 = in.readInt();
            preDefined4 = in.readInt();
            width = in.readShort();
            height = in.readShort();
            horizontalResolution = in.readInt();
            verticalResolution = in.readInt();
            reserved2 = in.readInt();
            frameCount = in.readShort();
            final int nameSize = in.readByte();
            final byte[] nameBytes = new byte[nameSize];
            in.readBytes(nameBytes);
            compressorName = new String(nameBytes);
            in.skipBytes(31 - nameSize);
            depth = in.readShort();
            preDefined5 = in.readShort();
            final int configSize = in.readInt();
            configType = new byte[4];
            in.readBytes(configType);
            configBytes = new byte[configSize - 8];
            in.readBytes(configBytes);
        }

        @Override
        public ChannelBuffer write() {
            ChannelBuffer out = ChannelBuffers.dynamicBuffer();
            out.writeBytes(new byte[6]);
            out.writeShort(index);
            out.writeShort(preDefined1);
            out.writeShort(reserved1);
            out.writeInt(preDefined2);
            out.writeInt(preDefined3);
            out.writeInt(preDefined4);
            out.writeShort(width);
            out.writeShort(height);
            out.writeInt(horizontalResolution);
            out.writeInt(verticalResolution);
            out.writeInt(reserved2);
            out.writeShort(frameCount);
            //===== compressor name =====
            out.writeByte((byte) compressorName.length());
            byte[] compressorNameBytes = compressorName.getBytes();
            out.writeBytes(compressorNameBytes);
            byte[] padding = new byte[31 - compressorNameBytes.length];
            out.writeBytes(padding);
            //===========================
            out.writeShort(depth);
            out.writeShort(preDefined5);
            out.writeInt(8 + configBytes.length);
            out.writeBytes(configType);
            out.writeBytes(configBytes);
            return out;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[width: ").append(width);
            sb.append(" height: ").append(height);
            sb.append(" h-resolution: ").append(horizontalResolution);
            sb.append(" v-resolution: ").append(verticalResolution);            
            sb.append(" frameCount: ").append(frameCount);
            sb.append(" compressorName: '").append(compressorName);
            sb.append("' depth: ").append(depth);
            sb.append(" configType: '").append(new String(configType));
            sb.append("' configBytes: ").append(Utils.toHex(configBytes));
            sb.append(']');
            return sb.toString();
        }

    }    
    
}
