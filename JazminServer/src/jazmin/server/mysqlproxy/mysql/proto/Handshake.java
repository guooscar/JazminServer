package jazmin.server.mysqlproxy.mysql.proto;

import java.util.ArrayList;

public class Handshake extends Packet {
    public long protocolVersion = 0x0a;
    public String serverVersion = "";
    public long connectionId = 0;
    public String challenge1 = "";
    public long capabilityFlags = Flags.CLIENT_PROTOCOL_41;
    public long characterSet = 0;
    public long statusFlags = 0;
    public String challenge2 = "";
    public long authPluginDataLength = 0;
    public String authPluginName = "";

    public void setCapabilityFlag(long flag) {
        this.capabilityFlags |= flag;
    }

    public void removeCapabilityFlag(long flag) {
        this.capabilityFlags &= ~flag;
    }

    public void toggleCapabilityFlag(long flag) {
        this.capabilityFlags ^= flag;
    }

    public boolean hasCapabilityFlag(long flag) {
        return ((this.capabilityFlags & flag) == flag);
    }

    public void setStatusFlag(long flag) {
        this.statusFlags |= flag;
    }

    public void removeStatusFlag(long flag) {
        this.statusFlags &= ~flag;
    }

    public void toggleStatusFlag(long flag) {
        this.statusFlags ^= flag;
    }

    public boolean hasStatusFlag(long flag) {
        return ((this.statusFlags & flag) == flag);
    }

    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();

        payload.add( Proto.buildFixedInt(1, this.protocolVersion));
        payload.add( Proto.buildNullStr(this.serverVersion));
        payload.add( Proto.buildFixedInt(4, this.connectionId));
        payload.add( Proto.buildFixedStr(8, this.challenge1));
        payload.add( Proto.buildFiller(1));
        payload.add( Proto.buildFixedInt(2, this.capabilityFlags >> 16));
        payload.add( Proto.buildFixedInt(1, this.characterSet));
        payload.add( Proto.buildFixedInt(2, this.statusFlags));
        payload.add( Proto.buildFixedInt(2, this.capabilityFlags & 0xffff));

        if (this.hasCapabilityFlag(Flags.CLIENT_SECURE_CONNECTION)) {
            payload.add( Proto.buildFixedInt(1, this.authPluginDataLength));
        }
        else {
            payload.add( Proto.buildFiller(1));
        }

        payload.add( Proto.buildFixedStr(10, ""));

        if (this.hasCapabilityFlag(Flags.CLIENT_SECURE_CONNECTION)) {
            payload.add( Proto.buildFixedStr(Math.max(13, this.authPluginDataLength - 8), this.challenge2));
        }

        if (this.hasCapabilityFlag(Flags.CLIENT_PLUGIN_AUTH)) {
            payload.add( Proto.buildNullStr(this.authPluginName));
        }

        return payload;
    }

    public static Handshake loadFromPacket(byte[] packet) {
        Handshake obj = new Handshake();
        Proto proto = new Proto(packet, 3);

        obj.sequenceId = proto.getFixedInt(1);
        obj.protocolVersion = proto.getFixedInt(1);
        obj.serverVersion = proto.getNullStr();
        obj.connectionId = proto.getFixedInt(4);
        obj.challenge1 = proto.getFixedStr(8);
        proto.getFiller(1);
        obj.capabilityFlags = proto.getFixedInt(2) << 16;

        if (proto.hasRemainingData()) {
            obj.characterSet = proto.getFixedInt(1);
            obj.statusFlags = proto.getFixedInt(2);
            obj.setCapabilityFlag(proto.getFixedInt(2));

            if (obj.hasCapabilityFlag(Flags.CLIENT_PLUGIN_AUTH)) {
                obj.authPluginDataLength = proto.getFixedInt(1);
            }
            else {
                proto.getFiller(1);
            }

            proto.getFiller(10);

            if (obj.hasCapabilityFlag(Flags.CLIENT_SECURE_CONNECTION)) {
                obj.challenge2 = proto.getFixedStr(Math.max(13, obj.authPluginDataLength - 8));
            }

            if (obj.hasCapabilityFlag(Flags.CLIENT_PLUGIN_AUTH)) {
                obj.authPluginName = proto.getNullStr();
            }
        }

        return obj;
    }
}
