package jazmin.server.mysqlproxy.mysql.proto;

import java.util.ArrayList;

public class HandshakeResponse extends Packet {
    public long capabilityFlags = Flags.CLIENT_PROTOCOL_41;
    public long maxPacketSize = 0;
    public long characterSet = 0;
    public String username = "";
    public long authResponseLen = 0;
    public String authResponse = "";
    public String schema = "";
    public String pluginName = "";
    public long clientAttributesLen = 0;
    public String clientAttributes = "";
    
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
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        if ((this.capabilityFlags & Flags.CLIENT_PROTOCOL_41) != 0) {
            payload.add( Proto.buildFixedInt(4, this.capabilityFlags));
            payload.add( Proto.buildFixedInt(4, this.maxPacketSize));
            payload.add( Proto.buildFixedInt(1, this.characterSet));
            payload.add( Proto.buildFiller(23));
            payload.add( Proto.buildNullStr(this.username));
            if (this.hasCapabilityFlag(Flags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA)) {
                payload.add( Proto.buildLenencInt(this.authResponseLen));
                payload.add( Proto.buildFixedStr(this.authResponseLen, this.authResponse, true));
            }
            else {
                if (this.hasCapabilityFlag(Flags.CLIENT_SECURE_CONNECTION)) {
                    payload.add( Proto.buildFixedInt(1, this.authResponseLen));
                    payload.add( Proto.buildFixedStr(this.authResponseLen, this.authResponse, true));
                }
                else
                    payload.add( Proto.buildNullStr(this.authResponse));
            }
                
            if (this.hasCapabilityFlag(Flags.CLIENT_CONNECT_WITH_DB))
                payload.add( Proto.buildNullStr(this.schema));
            
            if (this.hasCapabilityFlag(Flags.CLIENT_PLUGIN_AUTH))
                payload.add( Proto.buildNullStr(this.pluginName));
                
            if (this.hasCapabilityFlag(Flags.CLIENT_CONNECT_ATTRS)) {
                payload.add( Proto.buildLenencInt(this.clientAttributesLen));
                payload.add( Proto.buildEopStr(this.clientAttributes));
            }
        }
        else {
            payload.add( Proto.buildFixedInt(2, this.capabilityFlags));
            payload.add( Proto.buildFixedInt(3, this.maxPacketSize));
            payload.add( Proto.buildNullStr(this.username));
            
            if (this.hasCapabilityFlag(Flags.CLIENT_CONNECT_WITH_DB)) {
                payload.add( Proto.buildNullStr(this.authResponse));   
                payload.add( Proto.buildNullStr(this.schema));
            }
            else
                payload.add( Proto.buildEopStr(this.authResponse));
            
        }
        
        return payload;
    }
    
    public static HandshakeResponse loadFromPacket(byte[] packet) {
        HandshakeResponse obj = new HandshakeResponse();
        Proto proto = new Proto(packet, 3);
        
        obj.sequenceId = proto.getFixedInt(1);
        obj.capabilityFlags = proto.getFixedInt(2);
        proto.offset -= 2;
        
        if (obj.hasCapabilityFlag(Flags.CLIENT_PROTOCOL_41)) {
            obj.capabilityFlags = proto.getFixedInt(4);
            obj.maxPacketSize = proto.getFixedInt(4);
            obj.characterSet = proto.getFixedInt(1);
            proto.getFiller(23);
            obj.username = proto.getNullStr();
            
            if (obj.hasCapabilityFlag(Flags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA)) {
                obj.authResponseLen = proto.getLenencInt();
                obj.authResponse = proto.getFixedStr(obj.authResponseLen, true);
            }
            else  {
                if (obj.hasCapabilityFlag(Flags.CLIENT_SECURE_CONNECTION)) {
                    obj.authResponseLen = proto.getFixedInt(1);
                    obj.authResponse = proto.getFixedStr(obj.authResponseLen, true);
                }
                else {
                    obj.authResponse = proto.getNullStr();
                }
            }
            
            if (obj.hasCapabilityFlag(Flags.CLIENT_CONNECT_WITH_DB))
                obj.schema = proto.getNullStr();
            
            if (obj.hasCapabilityFlag(Flags.CLIENT_PLUGIN_AUTH))
                obj.pluginName = proto.getNullStr();
                
            if (obj.hasCapabilityFlag(Flags.CLIENT_CONNECT_ATTRS)) {
                obj.clientAttributesLen = proto.getLenencInt();
                obj.clientAttributes = proto.getEopStr();
            }
        }
        else {
            obj.capabilityFlags = proto.getFixedInt(2);
            obj.maxPacketSize = proto.getFixedInt(3);
            obj.username = proto.getNullStr();
            
            if (obj.hasCapabilityFlag(Flags.CLIENT_CONNECT_WITH_DB)) {
                obj.authResponse = proto.getNullStr();
                obj.schema = proto.getNullStr();
            }
            else
                obj.authResponse = proto.getEopStr();
        }
        
        return obj;
    }
}
