/**
 * 
 */
package jazmin.server.sip.io.pkts.packet;

import jazmin.server.sip.io.pkts.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface SDPPacket extends Packet {

    Buffer toBuffer();

}
