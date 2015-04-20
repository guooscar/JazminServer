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
/**
 * 
 */
public class CTTS implements Payload {

    private static final Logger logger = LoggerFactory.getLogger(CTTS.class);

    public static class CTTSRecord {

        private int sampleCount;
        private int sampleOffset;

        public int getSampleCount() {
            return sampleCount;
        }

        public int getSampleOffset() {
            return sampleOffset;
        }
        
    }

    private List<CTTSRecord> records;

    public List<CTTSRecord> getRecords() {
        return records;
    }

    public void setRecords(List<CTTSRecord> records) {
        this.records = records;
    }

    public CTTS(ChannelBuffer in) {
        read(in);
    }

    @Override
    public void read(ChannelBuffer in) {
        in.readInt(); // UI8 version + UI24 flags
        final int count = in.readInt();
        logger.debug("no of composition time to sample records: {}", count);
        records = new ArrayList<CTTSRecord>(count);
        for (int i = 0; i < count; i++) {
            CTTSRecord record = new CTTSRecord();
            record.sampleCount = in.readInt();
            record.sampleOffset = in.readInt();
//            logger.debug("#{} sampleCount: {} sampleOffset: {}",
//                    new Object[]{i, record.sampleCount, record.sampleOffset});
            records.add(record);
        }
    }

    @Override
    public ChannelBuffer write() {
        ChannelBuffer out = ChannelBuffers.dynamicBuffer();
        out.writeInt(0); // UI8 version + UI24 flags
        final int count = records.size();
        out.writeInt(count);
        for (int i = 0; i < count; i++) {
            final CTTSRecord record = records.get(i);
            out.writeInt(record.sampleCount);
            out.writeInt(record.sampleOffset);
        }
        return out;
    }

}
