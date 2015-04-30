/**
 * 
 */
package jazmin.test.server.sip;

import jazmin.server.relay.RelayChannel;
import jazmin.server.sip.io.sip.SipRequest;
import jazmin.server.sip.stack.Connection;

/**
 * @author yama
 *
 */
public class SessionStatus {
	public SipRequest originalRequest;
	public Connection connection;
	public RelayChannel audioRelayChannelA;
	public RelayChannel audioRelayChannelB;
	public RelayChannel videoRelayChannelA;
	public RelayChannel videoRelayChannelB;
}
