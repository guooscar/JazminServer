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

package jazmin.server.rtmp.io.flv;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.rtmp.io.BufferReader;
import jazmin.server.rtmp.io.FileChannelReader;
import jazmin.server.rtmp.rtmp.RtmpMessage;
import jazmin.server.rtmp.rtmp.RtmpReader;
import jazmin.server.rtmp.rtmp.message.Aggregate;
import jazmin.server.rtmp.rtmp.message.MessageType;
import jazmin.server.rtmp.rtmp.message.Metadata;
import jazmin.server.rtmp.rtmp.message.MetadataAmf0;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class FlvReader implements RtmpReader {

    private static final Logger logger = LoggerFactory.getLogger(FlvReader.class);
    
    private final BufferReader in;
    private final long mediaStartPosition;
    private final Metadata metadata;
    private int aggregateDuration;    

    public FlvReader(final String path) {
        in = new FileChannelReader(path);
        in.position(13); // skip flv header
        final RtmpMessage metadataAtom = next();
        final RtmpMessage metadataTemp = 
                MessageType.decode(metadataAtom.getHeader(), metadataAtom.encode());
        if(metadataTemp.getHeader().isMetadata()) {
            metadata = (Metadata) metadataTemp;
            mediaStartPosition = in.position();
        } else {
            logger.warn("flv file does not start with 'onMetaData', using empty one");
            metadata = new MetadataAmf0("onMetaData");
            in.position(13);
            mediaStartPosition = 13;
        }
        logger.debug("flv file metadata: {}", metadata);
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public RtmpMessage[] getStartMessages() {
        return new RtmpMessage[] { metadata };
    }

    @Override
    public void setAggregateDuration(int targetDuration) {
        this.aggregateDuration = targetDuration;
    }

    @Override
    public long getTimePosition() {
        final int time;
        if(hasNext()) {
            time = next().getHeader().getTime();
            prev();
        } else if(hasPrev()) {
            time = prev().getHeader().getTime();
            next();
        } else {
            throw new RuntimeException("not seekable");
        }
        return time;
    }

    private static boolean isSyncFrame(final RtmpMessage message) {
        final byte firstByte = message.encode().getByte(0);
        if((firstByte & 0xF0) == 0x10) {
            return true;
        }
        return false;
    }

    @Override
    public long seek(final long time) {
        logger.debug("trying to seek to: {}", time);
        if(time == 0) { // special case
            try {
                in.position(mediaStartPosition);
                return 0;
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
        final long start = getTimePosition();        
        if(time > start) {
            while(hasNext()) {
                final RtmpMessage cursor = next();
                if(cursor.getHeader().getTime() >= time) {                    
                    break;
                }
            }
        } else {
            while(hasPrev()) {
                final RtmpMessage cursor = prev();
                if(cursor.getHeader().getTime() <= time) {
                    next();
                    break;
                }
            }
        }
        // find the closest sync frame prior
        try {
            final long checkPoint = in.position();
            while(hasPrev()) {
                final RtmpMessage cursor = prev();
                if(cursor.getHeader().isVideo() && isSyncFrame(cursor)) {
                    logger.debug("returned seek frame / position: {}", cursor);
                    return cursor.getHeader().getTime();
                }
            }
            // could not find a sync frame !
            // TODO better handling, what if file is audio only
            in.position(checkPoint);
            return getTimePosition();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() {        
        return in.position() < in.size();
    }


    protected boolean hasPrev() {        
        return in.position() > mediaStartPosition;
    }

    protected RtmpMessage prev() {        
        final long oldPos = in.position();
        in.position(oldPos - 4);
        final long newPos = oldPos - 4 - in.readInt();
        in.position(newPos);
        final FlvAtom flvAtom = new FlvAtom(in);
        in.position(newPos);
        return flvAtom;
    }

    private static final int AGGREGATE_SIZE_LIMIT = 65536;

    @Override
    public RtmpMessage next() {
        if(aggregateDuration <= 0) {
            return new FlvAtom(in);
        }
        final ChannelBuffer out = ChannelBuffers.dynamicBuffer();
        int firstAtomTime = -1;
        while(hasNext()) {
            final FlvAtom flvAtom = new FlvAtom(in);
            final int currentAtomTime = flvAtom.getHeader().getTime();
            if(firstAtomTime == -1) {
                firstAtomTime = currentAtomTime;
            }
            final ChannelBuffer temp = flvAtom.write();
            if(out.readableBytes() + temp.readableBytes() > AGGREGATE_SIZE_LIMIT) {
                prev();
                break;
            }
            out.writeBytes(temp);
            if(currentAtomTime - firstAtomTime > aggregateDuration) {
                break;
            }
        }
        return new Aggregate(firstAtomTime, out);
    }

    @Override
    public void close() {
        in.close();
    }

    public static void main(String[] args) {
        FlvReader reader = new FlvReader("home/apps/vod/IronMan.flv");
        while(reader.hasNext()) {
            RtmpMessage message = reader.next();
            logger.debug("{} {}", message, ChannelBuffers.hexDump(message.encode()));
        }
        reader.close();
    }

}
