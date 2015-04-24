/**
 * 
 */
package jazmin.server.sip.io.pkts;

/**
 * The wall clock whose time is driven by the timestamps of the captured packets
 * in the pcap file.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Clock {

    long currentTimeMillis();

}
