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

package jazmin.server.rtmp.io.f4v;

import java.math.BigDecimal;
import java.math.RoundingMode;
/**
 * 
 * @author yama
 *
 */
public class Sample implements Comparable<Sample> {
    private Chunk chunk;
    private int size;
    private int duration;
    private int time;
    private int compositionTimeOffset;
    private boolean syncSample;
    private long fileOffset;

    public int convertFromTimeScale(final long time) {
        final BigDecimal factor = new BigDecimal(time * 1000);
        return factor.divide(chunk.getTimeScale(), RoundingMode.HALF_EVEN).intValue();
    }

    public boolean isVideo() {
        return chunk.getSampleType().isVideo();
    }

    //==========================================================================

    public long getFileOffset() {
        return fileOffset;
    }

    public void setFileOffset(long fileOffset) {
        this.fileOffset = fileOffset;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSyncSample(boolean syncSample) {
        this.syncSample = syncSample;
    }

    public boolean isSyncSample() {
        return syncSample;
    }

    public int getCompositionTimeOffset() {
        return compositionTimeOffset;
    }

    public void setCompositionTimeOffset(int compositionTimeOffset) {
        this.compositionTimeOffset = compositionTimeOffset;
    }

    @Override
    public int compareTo(final Sample o) {
        return time - ((Sample) o).time;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sample)) {
            return false;
        }
        final Sample s = (Sample) o;
        return time == s.time;
    }

    @Override
    public int hashCode() {
        return time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(chunk.getSampleType());
        if (syncSample) {
            sb.append(" (*sync*)");
        }
        sb.append(" fileOffset: ").append(fileOffset);
        sb.append(" size: ").append(size);
        sb.append(" duration: ").append(duration);
        sb.append(" time: ").append(time);
        if (compositionTimeOffset > 0) {
            sb.append(" c-time: ").append(compositionTimeOffset);
        }
        sb.append("]");
        return sb.toString();
    }
    
}
