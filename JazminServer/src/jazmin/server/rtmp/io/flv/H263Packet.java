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

import org.jboss.netty.buffer.ChannelBuffer;

public class H263Packet {

    private final int width;
    private final int height;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public H263Packet(final ChannelBuffer in, final int offset) {
        // 0000 0000 | 0000 0000 | 1---
        // picture start code (0, 17)
        // ---- ---- | ---- ---- | -000 00-- |
        //                          version (17, 5)
        //                         ---- --00 | 0000 00--
        //                                temporal ref (22, 8)
        //                                   | ---- --00 | 0--- ---- |
        //                                            picture size (30, 3)
        //                                               | -000 0000 | 0--- (width 33, 8)
        //                                                 -000 0000 | 0000 0000 | 0--- (width 33, 16)
        //                                                           | -000 0000 | 0--- (height 41, 8)
        //                                                       (height 48, 16) | -000 0000 | 0000 0000 | 0---

        final short bitPos24to39 = in.getShort(offset + 3);
        final int pictureSize = 0x07 & (bitPos24to39 >> 7);
        switch(pictureSize) {
            case 0 :
                final short bitPos32to47 = in.getShort(offset + 4);
                width = 0x007F & (bitPos32to47 >> 7);
                final short bitPos40to55 = in.getShort(offset + 5);
                height = 0x007F & (bitPos40to55 >> 7);
                break;
            case 1 :
                final int bitPos32to55 = in.getMedium(offset + 4);
                width = 0x007FFF & (bitPos32to55 >> 7);
                final int bitPos49to72 = in.getMedium(offset + 6);
                height = 0x007FFF & (bitPos49to72 >> 7);
                break;
            case 2 :
                width = 352;
                height = 288;
                break;
            case 3:
                width = 176;
                height = 144;
                break;
            case 4:
                width = 128;
                height = 96;
                break;
            case 5:
                width = 320;
                height = 240;
                break;
            case 6:
                width = 160;
                height = 120;
                break;
            default:
                throw new RuntimeException("unsupported size marker: " + pictureSize);
        }
    }

    @Override
    public String toString() {
        return "[width: " + width + ", height: " + height + "]";
    }

}
