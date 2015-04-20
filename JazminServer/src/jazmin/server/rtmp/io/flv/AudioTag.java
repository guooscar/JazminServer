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

import jazmin.server.rtmp.util.ValueToEnum;

public class AudioTag {

    private final CodecType codecType;
    private final SampleRate sampleRate;
    private final boolean sampleSize16Bit;
    private final boolean stereo;

    public AudioTag(final byte byteValue) {
        final int unsigned = 0xFF & byteValue;
        codecType = CodecType.valueToEnum(unsigned >> 4);
        sampleSize16Bit = (0x02 & unsigned) > 0;
        if(codecType == CodecType.AAC) {
            sampleRate = SampleRate.KHZ_44;
            stereo = true;
            return;
        }
        sampleRate = SampleRate.valueToEnum((0x0F & unsigned) >> 2);
        stereo = (0x01 & unsigned) > 0;
    }

    public CodecType getCodecType() {
        return codecType;
    }

    public SampleRate getSampleRate() {
        return sampleRate;
    }

    public boolean isSampleSize16Bit() {
        return sampleSize16Bit;
    }

    public boolean isStereo() {
        return stereo;
    }

    public static enum CodecType implements ValueToEnum.IntValue {

        ADPCM(1),
        MP3(2),
        PCM(3),
        NELLY_16(4),
        NELLY_8(5),
        NELLY(6),
        G711_A(7),
        G711_U(8),
        RESERVED(9),
        AAC(10),
        SPEEX(11),
        MP3_8(14),
        DEVICE_SPECIFIC(15);

        private final int value;

        CodecType(final int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value;
        }

        private static final ValueToEnum<CodecType> converter = new ValueToEnum<CodecType>(CodecType.values());

        public static CodecType valueToEnum(final int value) {
            return converter.valueToEnum(value);
        }

    }

    public static enum SampleRate implements ValueToEnum.IntValue {

        KHZ_5(0),
        KHZ_11(1),
        KHZ_22(2),
        KHZ_44(3);

        private final int value;

        SampleRate(final int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value;
        }

        private static final ValueToEnum<SampleRate> converter = new ValueToEnum<SampleRate>(SampleRate.values());

        public static SampleRate valueToEnum(final int value) {
            return converter.valueToEnum(value);
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[format: ").append(codecType);
        sb.append(", sampleRate: ").append(sampleRate);
        sb.append(", sampleSize16bit: ").append(sampleSize16Bit);
        sb.append(", stereo: ").append(stereo);
        sb.append(']');
        return sb.toString();
    }

}
