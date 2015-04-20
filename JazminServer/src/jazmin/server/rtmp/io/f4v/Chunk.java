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
import java.util.ArrayList;
import java.util.List;

public class Chunk implements Comparable<Chunk> {
    
    private int sampleDescIndex;    
    private long fileOffset;
    private List<Sample> samples = new ArrayList<Sample>();

    private TrackInfo track;
    private SampleType sampleType;
    private BigDecimal timeScale;

    public void setTrack(TrackInfo track) {
        this.track = track;
        sampleType = track.getStsd().getSampleType(sampleDescIndex);
        timeScale = new BigDecimal(track.getMdhd().getTimeScale());
    }

    public BigDecimal getTimeScale() {
        return timeScale;
    }

    public SampleType getSampleType() {
        return sampleType;
    }    

    //==========================================================================

    public TrackInfo getTrack() {
        return track;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public void setFileOffset(long fileOffset) {
        this.fileOffset = fileOffset;
    }

    public int getSampleDescIndex() {
        return sampleDescIndex;
    }

    public void setSampleDescIndex(int sampleDescIndex) {
        this.sampleDescIndex = sampleDescIndex;
    }

    public void add(Sample sample) {
        sample.setChunk(this);
        samples.add(sample);
    }

    public int getSampleCount() {
        return samples.size();
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    @Override
    public int compareTo(final Chunk o) {
        return (int) (fileOffset - ((Chunk) o).fileOffset);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Chunk)) {
            return false;
        }
        final Chunk c = (Chunk) o;
        return fileOffset == c.fileOffset;
    }

    @Override
    public int hashCode() {
        return (int) fileOffset;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[type: ").append(sampleType);
        sb.append(" offset: ").append(fileOffset);
        sb.append(" samples: ").append(samples.size());
        for (Sample sample : samples) {
            sb.append(sample.toString());
        }
        sb.append("]");
        return sb.toString();
    }
    
}
