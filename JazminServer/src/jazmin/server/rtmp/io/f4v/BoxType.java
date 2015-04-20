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

import jazmin.server.rtmp.io.f4v.box.CTTS;
import jazmin.server.rtmp.io.f4v.box.FTYP;
import jazmin.server.rtmp.io.f4v.box.MDHD;
import jazmin.server.rtmp.io.f4v.box.MVHD;
import jazmin.server.rtmp.io.f4v.box.STCO;
import jazmin.server.rtmp.io.f4v.box.STSC;
import jazmin.server.rtmp.io.f4v.box.STSD;
import jazmin.server.rtmp.io.f4v.box.STSS;
import jazmin.server.rtmp.io.f4v.box.STSZ;
import jazmin.server.rtmp.io.f4v.box.STTS;
import jazmin.server.rtmp.io.f4v.box.TKHD;

import org.jboss.netty.buffer.ChannelBuffer;

public enum BoxType {

    FTYP,
    MOOV,
    /**/ MVHD,
    /**/ TRAK,
    /*    */ TKHD,
    /*    */ MDIA,
    /*        */ MDHD,
    /*        */ HDLR,
    /*        */ MINF,
    /*           */ VMHD,
    /*           */ SMHD,
    /*           */ DINF,
    /*           */ STBL,
    /*               */ STSD,
    /*               */ STTS,
    /*               */ CTTS,
    /*               */ STSC,
    /*               */ STSZ,
    /*               */ STCO,
    /*               */ CO64,
    /*               */ STSS,
    MDAT,           //======
    UNKNOWN;

    private String typeString;

    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }

    public BoxType[] getChildren() {
        switch(this) {            
            case MOOV: return array(MVHD, TRAK);
            case TRAK: return array(TKHD, MDIA);
            case MDIA: return array(MDHD, HDLR, MINF);
            case MINF: return array(VMHD, SMHD, DINF, STBL);
            case STBL: return array(STSD, STTS, CTTS, STSC, STSZ, STCO, CO64, STSS);
            default: return null;
        }
    }

    public Payload read(ChannelBuffer in) {
        switch(this) {
            case FTYP: return new FTYP(in);
            case MVHD: return new MVHD(in);
            case TKHD: return new TKHD(in);
            case MDHD: return new MDHD(in);
            case STSD: return new STSD(in);
            case STTS: return new STTS(in);
            case CTTS: return new CTTS(in);
            case STSC: return new STSC(in);
            case STSZ: return new STSZ(in);
            case STCO: return new STCO(in);
            case CO64: return new STCO(in, true);
            case STSS: return new STSS(in);            
            default: return new UnknownPayload(in, this);
        }
    }

    private static BoxType[] array(BoxType ... types) {
        return types;
    }

    public static BoxType parse(String typeString) {
        BoxType type;
        try {
            type = BoxType.valueOf(typeString.toUpperCase());
        } catch(Exception e) {
            type = UNKNOWN;
        }
        type.setTypeString(typeString);
        return type;
    }

    @Override
    public String toString() {
        if(this != UNKNOWN) {
            return super.toString();
        }
        return "(" + typeString + ")";
    }
    
}
