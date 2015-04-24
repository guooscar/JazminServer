/**
 * 
 */
package jazmin.server.sip.io.pkts.packet;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface TCPPacket extends TransportPacket {

    boolean isFIN();

    boolean isSYN();

    boolean isRST();

    /**
     * Check whether the psh (push) flag is turned on
     * 
     * @return
     */
    boolean isPSH();

    boolean isACK();

    boolean isURG();

    boolean isECE();

    boolean isCWR();

}
