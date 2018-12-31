package jazmin.server.mysqlproxy.mysql.proto;

import java.util.ArrayList;
import java.util.Base64;
/**
 * 
 * @author yama
 *
 */
public class Proto {
    public byte[] packet = null;
    public int offset = 0;

    public Proto(byte[] packet) {
        this.packet = packet;
    }

    public Proto(byte[] packet, int offset) {
        this.packet = packet;
        this.offset = offset;
    }

    public boolean hasRemainingData() {
        return this.packet.length - this.offset > 0;
    }

    public static byte[] buildFixedInt(int size, long value) {
        byte[] packet = new byte[size];

        if (size == 8) {
            packet[0] = (byte) ((value >>  0) & 0xFF);
            packet[1] = (byte) ((value >>  8) & 0xFF);
            packet[2] = (byte) ((value >> 16) & 0xFF);
            packet[3] = (byte) ((value >> 24) & 0xFF);
            packet[4] = (byte) ((value >> 32) & 0xFF);
            packet[5] = (byte) ((value >> 40) & 0xFF);
            packet[6] = (byte) ((value >> 48) & 0xFF);
            packet[7] = (byte) ((value >> 56) & 0xFF);
        }
        else if (size == 6) {
            packet[0] = (byte) ((value >>  0) & 0xFF);
            packet[1] = (byte) ((value >>  8) & 0xFF);
            packet[2] = (byte) ((value >> 16) & 0xFF);
            packet[3] = (byte) ((value >> 24) & 0xFF);
            packet[4] = (byte) ((value >> 32) & 0xFF);
            packet[5] = (byte) ((value >> 40) & 0xFF);
        }
        else if (size == 4) {
            packet[0] = (byte) ((value >>  0) & 0xFF);
            packet[1] = (byte) ((value >>  8) & 0xFF);
            packet[2] = (byte) ((value >> 16) & 0xFF);
            packet[3] = (byte) ((value >> 24) & 0xFF);
        }
        else if (size == 3) {
            packet[0] = (byte) ((value >>  0) & 0xFF);
            packet[1] = (byte) ((value >>  8) & 0xFF);
            packet[2] = (byte) ((value >> 16) & 0xFF);
        }
        else if (size == 2) {
            packet[0] = (byte) ((value >>  0) & 0xFF);
            packet[1] = (byte) ((value >>  8) & 0xFF);
        }
        else if (size == 1) {
            packet[0] = (byte) ((value >>  0) & 0xFF);
        }
        else {
            return null;
        }
        return packet;
    }

    public static byte[] buildLenencInt(long value) {
        byte[] packet = null;

        if (value < 251) {
            packet = new byte[1];
            packet[0] = (byte) ((value >>  0) & 0xFF);
        }
        else if (value < 65535) {
            packet = new byte[3];
            packet[0] = (byte) 0xFC;
            packet[1] = (byte) ((value >>  0) & 0xFF);
            packet[2] = (byte) ((value >>  8) & 0xFF);
        }
        else if (value < 16777215) {
            packet = new byte[4];
            packet[0] = (byte) 0xFD;
            packet[1] = (byte) ((value >>  0) & 0xFF);
            packet[2] = (byte) ((value >>  8) & 0xFF);
            packet[3] = (byte) ((value >> 16) & 0xFF);
        }
        else {
            packet = new byte[9];
            packet[0] = (byte) 0xFE;
            packet[1] = (byte) ((value >>  0) & 0xFF);
            packet[2] = (byte) ((value >>  8) & 0xFF);
            packet[3] = (byte) ((value >> 16) & 0xFF);
            packet[4] = (byte) ((value >> 24) & 0xFF);
            packet[5] = (byte) ((value >> 32) & 0xFF);
            packet[6] = (byte) ((value >> 40) & 0xFF);
            packet[7] = (byte) ((value >> 48) & 0xFF);
            packet[8] = (byte) ((value >> 56) & 0xFF);
        }

        return packet;
    }

    public static byte[] buildLenencStr(String str) {
        return Proto.buildLenencStr(str, false);
    }

    public static byte[] buildLenencStr(String str, boolean base64) {
        if (str.equals("")) {
            byte[] packet = new byte[1];
            packet[0] = 0x00;
            return packet;
        }

        int strsize = str.length();
        if (base64)
            strsize = Base64.getDecoder().decode(str).length;

        byte[] size = Proto.buildLenencInt(strsize);
        byte[] strByte = Proto.buildFixedStr(strsize, str, base64);
        byte[] packet = new byte[size.length + strByte.length];
        System.arraycopy(size, 0, packet, 0, size.length);
        System.arraycopy(strByte, 0, packet, size.length, strByte.length);
        return packet;
    }

    public static byte[] buildNullStr(String str) {
        return Proto.buildNullStr(str, false);
    }

    public static byte[] buildNullStr(String str, boolean base64) {
        return Proto.buildFixedStr(str.length() + 1, str, base64);
    }

    public static byte[] buildFixedStr(long size, String str) {
        return Proto.buildFixedStr((int)size, str);
    }

    public static byte[] buildFixedStr(long size, String str, boolean base64) {
        return Proto.buildFixedStr((int)size, str, base64);
    }

    public static byte[] buildFixedStr(int size, String str) {
        return Proto.buildFixedStr(size, str, false);
    }

    public static byte[] buildFixedStr(int size, String str, boolean base64) {
        byte[] packet = new byte[size];
        byte[] strByte = null;

        if (base64)
            strByte = Base64.getDecoder().decode(str);
        else
            strByte = str.getBytes();

        if (strByte.length < packet.length)
            size = strByte.length;
        System.arraycopy(strByte, 0, packet, 0, size);
        return packet;
    }

    public static byte[] buildEopStr(String str) {
        return Proto.buildEopStr(str, false);
    }

    public static byte[] buildEopStr(String str, boolean base64) {
        int size = str.length();
        if (base64)
            size = Base64.getDecoder().decode(str).length;
        return Proto.buildFixedStr(size, str, base64);
    }

    public static byte[] buildFiller(int len) {
        return Proto.buildFiller(len, (byte)0x00);
    }

    public static byte[] buildFiller(int len, int filler_value) {
        return Proto.buildFiller(len, (byte)filler_value);
    }

    public static byte[] buildFiller(int len, byte filler_value) {
        byte[] filler = new byte[len];
        for (int i = 0; i < len; i++)
            filler[i] = filler_value;
        return filler;
    }

    public static byte[] buildByte(byte value) {
        byte[] field = new byte[1];
        field[0] = value;
        return field;
    }

    public static char int2char(byte i) {
        return (char)i;
    }

    public static byte char2int(char i) {
        return (byte)i;
    }

    public long getFixedInt(int size) {
        byte[] bytes = null;

        if ( this.packet.length < (size + this.offset))
            return -1;

        bytes = new byte[size];
        System.arraycopy(packet, offset, bytes, 0, size);
        this.offset += size;
        return getFixedInt(bytes);
    }

    public static long getFixedInt(byte[] bytes) {
        long value = 0;

        for (int i = bytes.length-1; i > 0; i--) {
            value |= bytes[i] & 0xFF;
            value <<= 8;
        }
        value |= bytes[0] & 0xFF;

        return value;
    }

    public void getFiller(int size) {
        this.offset += size;
    }

    public long getLenencInt() {
        int size = 0;

        // 1 byte int
        if (this.packet[offset] < 251) {
            size = 1;
        }
        // 2 byte int
        else if (this.packet[offset] == 252) {
            this.offset += 1;
            size = 2;
        }
        // 3 byte int
        else if (this.packet[offset] == 253) {
            this.offset += 1;
            size = 3;
        }
        // 8 byte int
        else if (this.packet[offset] == 254) {
            this.offset += 1;
            size = 8;
        }

        if (size == 0) {
           
            return -1;
        }

        return this.getFixedInt(size);
    }

    public String getFixedStr(long len) {
        return this.getFixedStr((int)len);
    }

    public String getFixedStr(long len, boolean base64) {
        return this.getFixedStr((int)len, base64);
    }

    public String getFixedStr(int len) {
        int start = this.offset;
        int end = this.offset+len;

        if (end > this.packet.length) {
            end = this.packet.length;
            len = end - start;
        }

        StringBuilder str = new StringBuilder(len);

        for (int i = start; i < end; i++) {
            str.append(Proto.int2char(packet[i]));
            this.offset += 1;
        }

        return str.toString();
    }

    public String getFixedStr(int len, boolean base64) {
        int start = this.offset;
        int end = this.offset+len;

        if (end > this.packet.length) {
            end = this.packet.length;
            len = end - start;
        }

        if (!base64)
            return this.getFixedStr(len);

        byte[] chunk = new byte[len];
        System.arraycopy(this.packet, start, chunk, 0, len);
        this.offset += len;
        return Base64.getEncoder().encodeToString(chunk);
    }

    public String getNullStr() {
        return this.getNullStr(false);
    }

    public String getNullStr(boolean base64) {
        int len = 0;

        for (int i = this.offset; i < this.packet.length; i++) {
            if (packet[i] == 0x00)
                break;
            len += 1;
        }

        String str = this.getFixedStr(len, base64);
        this.offset += 1;
        return str;
    }

    public String getEopStr() {
        return this.getEopStr(false);
    }

    public String getEopStr(boolean base64) {
        int len = this.packet.length - this.offset;
        return this.getFixedStr(len, base64);
    }

    public String getLenencStr() {
        return this.getLenencStr(false);
    }

    public String getLenencStr(boolean base64) {
        int len = (int)this.getLenencInt();
        return this.getFixedStr(len, base64);
    }

    public static byte[] arraylistToArray(ArrayList<byte[]> input) {
        int size = 0;
        for (byte[] field: input)
            size += field.length;

        byte[] result = new byte[size];

        int offset = 0;
        for (byte[] field: input) {
            System.arraycopy(field, 0, result, offset, field.length);
            offset += field.length;
        }

        return result;
    }

}
