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

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.rtmp.io.f4v.Payload;
import jazmin.server.rtmp.util.Utils;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class MVHD implements Payload {

    private static final Logger logger = LoggerFactory.getLogger(MVHD.class);
    
    private byte version;
    private byte[] flags;
    private long creationTime;
    private long modificationTime;
    private int timeScale;
    private long duration;
    private int playbackRate;
    private short volume;
    private short reserved1;
    private int[] reserved2; // 2
    private int[] transformMatrix; // 9
    private int[] reserved3; // 6
    private int nextTrackId;

    public MVHD(ChannelBuffer in) {
        read(in);
    }

    public int getTimeScale() {
        return timeScale;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public void read(ChannelBuffer in) {
        version = in.readByte();
        logger.debug("version: {}", Utils.toHex(version));
        flags = new byte[3];
        in.readBytes(flags);
        if (version == 0x00) {
            creationTime = in.readInt();
            modificationTime = in.readInt();
        } else {
            creationTime = in.readLong();
            modificationTime = in.readLong();
        }
        timeScale = in.readInt();
        if (version == 0x00) {
            duration = in.readInt();
        } else {
            duration = in.readLong();
        }
        playbackRate = in.readInt();
        volume = in.readShort();
        logger.debug("creationTime {} modificationTime {} timeScale {} duration {} playbackRate {} volume {}",
                new Object[]{creationTime, modificationTime, timeScale, duration, playbackRate, volume});
        reserved1 = in.readShort();
        reserved2 = new int[2];
        reserved2[0] = in.readInt();
        reserved2[1] = in.readInt();
        transformMatrix = new int[9];
        for (int i = 0; i < transformMatrix.length; i++) {
            transformMatrix[i] = in.readInt();
            logger.debug("transform matrix[{}]: {}", new Object[]{i, transformMatrix[i]});
        }
        reserved3 = new int[6];
        for (int i = 0; i < reserved3.length; i++) {
            reserved3[i] = in.readInt();
        }
        nextTrackId = in.readInt();
    }

    @Override
    public ChannelBuffer write() {
        ChannelBuffer out = ChannelBuffers.dynamicBuffer();
        out.writeByte(version);
        out.writeBytes(new byte[3]); // flags
        if (version == 0x00) {
            out.writeInt((int) creationTime);
            out.writeInt((int) modificationTime);
        } else {
            out.writeLong(creationTime);
            out.writeLong(modificationTime);
        }
        out.writeInt(timeScale);
        if (version == 0x00) {
            out.writeInt((int) duration);
        } else {
            out.writeLong(duration);
        }
        out.writeInt(playbackRate);
        out.writeShort(volume);
        out.writeShort(reserved1);
        out.writeInt(reserved2[0]);
        out.writeInt(reserved2[1]);
        for (int i = 0; i < transformMatrix.length; i++) {
            out.writeInt(transformMatrix[i]);
        }
        for (int i = 0; i < reserved3.length; i++) {
            out.writeInt(reserved3[i]);
        }
        out.writeInt(nextTrackId);
        return out;
    }

}
