/*
 * Copyright (C) 2006 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package jazmin.misc;

public class HexDump {
    private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String dumpHexString(final byte[] array) {
        return dumpHexString(array, 0, array.length);
    }

    public static String dumpHexString(final byte[] array, final int offset, final int length) {
        final StringBuilder result = new StringBuilder();
        final byte[] line = new byte[16];
        int lineIndex = 0;
        result.append("0x");
        result.append(toHexString(offset));
        for (int i = offset; i < (offset + length); i++) {
            if (lineIndex == 16) {
                result.append(" ");

                for (int j = 0; j < 16; j++) {
                    if ((line[j] > ' ') && (line[j] < '~')) {
                        result.append(new String(line, j, 1));
                    } else {
                        result.append(".");
                    }
                }

                result.append("\n0x");
                result.append(toHexString(i));
                lineIndex = 0;
            }

            final byte b = array[i];
            result.append(" ");
            result.append(HEX_DIGITS[(b >>> 4) & 0x0F]);
            result.append(HEX_DIGITS[b & 0x0F]);

            line[lineIndex++] = b;
        }

        if (lineIndex != 16) {
            int count = (16 - lineIndex) * 3;
            count++;
            for (int i = 0; i < count; i++) {
                result.append(" ");
            }

            for (int i = 0; i < lineIndex; i++) {
                if ((line[i] > ' ') && (line[i] < '~')) {
                    result.append(new String(line, i, 1));
                } else {
                    result.append(".");
                }
            }
        }

        return result.toString();
    }

    public static String toHexString(final byte b) {
        return toHexString(toByteArray(b));
    }

    public static String toHexString(final byte[] array) {
        return toHexString(array, 0, array.length);
    }

    public static String toHexString(final byte[] array, final int offset, final int length) {
        final char[] buf = new char[length * 2];

        int bufIndex = 0;
        for (int i = offset; i < (offset + length); i++) {
            final byte b = array[i];
            buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
            buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
        }

        return new String(buf);
    }

    public static String toHexString(final int i) {
        return toHexString(toByteArray(i));
    }

    public static byte[] toByteArray(final byte b) {
        final byte[] array = new byte[1];
        array[0] = b;
        return array;
    }

    public static byte[] toByteArray(final int i) {
        final byte[] array = new byte[4];

        array[3] = (byte) (i & 0xFF);
        array[2] = (byte) ((i >> 8) & 0xFF);
        array[1] = (byte) ((i >> 16) & 0xFF);
        array[0] = (byte) ((i >> 24) & 0xFF);

        return array;
    }

    private static int toByte(final char c) {
        if ((c >= '0') && (c <= '9')) {
            return (c - '0');
        }
        if ((c >= 'A') && (c <= 'F')) {
            return ((c - 'A') + 10);
        }
        if ((c >= 'a') && (c <= 'f')) {
            return ((c - 'a') + 10);
        }

        throw new RuntimeException("Invalid hex char '" + c + "'");
    }

    public static byte[] hexStringToByteArray(final String hexString) {
        final int length = hexString.length();
        final byte[] buffer = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            buffer[i / 2] = (byte) ((toByte(hexString.charAt(i)) << 4) | toByte(hexString.charAt(i + 1)));
        }

        return buffer;
    }
}