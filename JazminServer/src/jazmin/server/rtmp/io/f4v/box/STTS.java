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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class STTS implements Payload {

    private static final Logger logger = LoggerFactory.getLogger(STTS.class);

    public static class STTSRecord {

        private int sampleCount;
        private int sampleDuration;

        public int getSampleCount() {
            return sampleCount;
        }

        public int getSampleDuration() {
            return sampleDuration;
        }

    }

    public STTS(ChannelBuffer in) {
        read(in);
    }

    private List<STTSRecord> records;

    public List<STTSRecord> getRecords() {
        return records;
    }

    public void setRecords(List<STTSRecord> records) {
        this.records = records;
    }

    @Override
    public void read(ChannelBuffer in) {
        in.readInt(); // UI8 version + UI24 flags
        final int count = in.readInt();
        logger.debug("no of time to sample records: {}", count);
        records = new ArrayList<STTSRecord>(count);
        for (int i = 0; i < count; i++) {
            final STTSRecord record = new STTSRecord();
            record.sampleCount = in.readInt();
            record.sampleDuration = in.readInt();
            logger.debug("#{} sampleCount: {} sampleDuration: {}",
                    new Object[]{i, record.sampleCount, record.sampleDuration});
            records.add(record);
        }
    }

    @Override
    public ChannelBuffer write() {
        ChannelBuffer out = ChannelBuffers.dynamicBuffer();
        out.writeInt(0); // UI8 version + UI24 flags
        out.writeInt(records.size());
        for (STTSRecord record : records) {
            out.writeInt(record.sampleCount);
            out.writeInt(record.sampleDuration);
        }
        return out;
    }
    
}
