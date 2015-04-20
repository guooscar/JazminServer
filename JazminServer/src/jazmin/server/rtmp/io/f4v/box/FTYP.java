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

import jazmin.server.rtmp.io.f4v.Payload;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class FTYP implements Payload { 
    private byte[] majorBrand;
    private int minorVersion;
    private List<byte[]> compatibleBrands;

    public FTYP(ChannelBuffer in) {
        read(in);
    }

    @Override
    public void read(ChannelBuffer in) {
        majorBrand = new byte[4];
        in.readBytes(majorBrand);        
        minorVersion = in.readInt();        
        compatibleBrands = new ArrayList<byte[]>();
        while (in.readable()) {
            final byte[] bytes = new byte[4];
            in.readBytes(bytes);            
            compatibleBrands.add(bytes);
        }
    }

    @Override
    public ChannelBuffer write() {
        ChannelBuffer out = ChannelBuffers.dynamicBuffer();
        out.writeBytes(majorBrand);
        out.writeInt(minorVersion);
        for (byte[] bytes : compatibleBrands) {
            out.writeBytes(bytes);
        }        
        return out;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[majorBrand: ").append(new String(majorBrand));
        sb.append(" minorVersion: ").append(minorVersion);
        if(compatibleBrands != null) {
            sb.append('[');
            for(byte[] brand : compatibleBrands) {
                sb.append(new String(brand)).append(' ');
            }
            sb.append(']');
        }
        sb.append(']');
        return super.toString();
    }
    
}
