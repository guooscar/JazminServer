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

package jazmin.server.rtmp.io;

import java.io.File;
import java.io.RandomAccessFile;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class RandomAccessFileReader implements BufferReader {

    private static final Logger logger = LoggerFactory.getLogger(RandomAccessFileReader.class);

    private final String absolutePath;
    private final RandomAccessFile in;
    private final long fileSize;

    public RandomAccessFileReader(final String path) {
        this(new File(path));
    }

    public RandomAccessFileReader(final File file) {
        absolutePath = file.getAbsolutePath();        
        try {
            in = new RandomAccessFile(file, "r");
            fileSize = in.length();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long size() {
        return fileSize;
    }

    @Override
    public long position() {
        try {
            return in.getFilePointer();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void position(final long position) {
        try {
            in.seek(position);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChannelBuffer read(final int size) {
        return ChannelBuffers.wrappedBuffer(readBytes(size));
    }

    @Override
    public byte[] readBytes(int size) {
        final byte[] bytes = new byte[size];
        try {
            in.readFully(bytes);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    @Override
    public int readInt() {
        return read(4).readInt();
    }

    @Override
    public long readUnsignedInt() {
        return read(4).readUnsignedInt();
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch(Exception e) {
            logger.warn("error closing file {}: {}", absolutePath, e.getMessage());
        }
        logger.info("closed file: {}", absolutePath);
    }

}
