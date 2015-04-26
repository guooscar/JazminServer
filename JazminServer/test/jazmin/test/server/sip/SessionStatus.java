/**
 * 
 */
package jazmin.test.server.sip;

import jazmin.server.sip.io.pkts.packet.sip.SipRequest;

/**
 * @author yama
 *
 */
public class SessionStatus {
	public SipRequest originalRequest;
	public String remoteAddress;
	public int remotePort;
}
