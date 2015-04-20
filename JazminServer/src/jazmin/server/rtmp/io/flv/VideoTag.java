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

public class VideoTag {

    private final FrameType frameType;
    private final CodecType codecType;

    public VideoTag(final byte byteValue) {
        frameType = FrameType.valueToEnum(byteValue >> 4);
        codecType = CodecType.valueToEnum(0x0F & byteValue);
    }

    public boolean isKeyFrame() {
        return frameType == FrameType.KEY;
    }

    public FrameType getFrameType() {
        return frameType;
    }

    public CodecType getCodecType() {
        return codecType;
    }

    public static enum FrameType implements ValueToEnum.IntValue {
        
        KEY(1),
        INTER(2),
        DISPOSABLE_INTER(3),
        GENERATED_KEY(4),
        COMMAND(5);

        private final int value;

        FrameType(final int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value;
        }

        private static final ValueToEnum<FrameType> converter = new ValueToEnum<FrameType>(FrameType.values());

        public static FrameType valueToEnum(final int value) {
            return converter.valueToEnum(value);
        }

    }

    public static enum CodecType implements ValueToEnum.IntValue {
        
        JPEG(1),
        H263(2),
        SCREEN(3),
        ON2VP6(4),
        ON2VP6_ALPHA(5),
        SCREEN_V2(6),
        AVC(7);

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[frameType: ").append(frameType);
        sb.append(", codecType: ").append(codecType);
        sb.append(']');
        return sb.toString();
    }

}
