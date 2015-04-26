/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.listeners;

import jazmin.log.*;

import jazmin.server.turn.stack.socket.TcpConnectEvent;
import jazmin.server.turn.stack.socket.TcpConnectEventListener;
import jazmin.server.turn.stack.stack.Allocation;
import jazmin.server.turn.stack.stack.TurnStack;

import org.ice4j.StunException;
import org.ice4j.attribute.AttributeFactory;
import org.ice4j.attribute.ConnectionIdAttribute;
import org.ice4j.message.Indication;
import org.ice4j.message.MessageFactory;
import org.ice4j.stack.TransactionID;

/**
 * Class to handle events when Peer tries to establish a TCP connection to a
 * Server Socket (generally Relay Address).
 * 
 * @author Aakash Garg
 * 
 */
public class PeerTcpConnectEventListner implements TcpConnectEventListener {

	/**
	 * The <tt>Logger</tt> used by the <tt>FiveTuple</tt> class and its
	 * instances for logging output.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(PeerTcpConnectEventListner.class);

	private final TurnStack turnStack;

	public PeerTcpConnectEventListner(TurnStack turnStack) {
		this.turnStack = turnStack;
	}

	@Override
	public void onConnect(TcpConnectEvent event) {
		logger.debug("Received a connect event src:" + event.getLocalAdress()
				+ ", dest:" + event.getRemoteAdress());
		Allocation allocation = this.turnStack.getServerAllocation(event
				.getLocalAdress());
		if (allocation == null) {
			logger.debug("Allocation not found for relay : "
					+ event.getLocalAdress());
		} else if (allocation.isPermitted(event.getRemoteAdress())) {
			try {
				ConnectionIdAttribute connectionId = AttributeFactory
						.createConnectionIdAttribute();
				logger.debug("Created ConnectionId - "
						+ connectionId.getConnectionIdValue() + " for client "
						+ allocation.getClientAddress());
				TransactionID tranID = TransactionID.createNewTransactionID();
				Indication connectionAttemptIndication = MessageFactory
						.createConnectionAttemptIndication(
								connectionId.getConnectionIdValue(),
								event.getRemoteAdress(), tranID.getBytes());
				this.turnStack.addUnAcknowlededConnectionId(
						connectionId.getConnectionIdValue(),
						event.getRemoteAdress(), allocation);
				logger.debug("Sending Connection Attempt Indication.");
				this.turnStack.sendIndication(connectionAttemptIndication,
						allocation.getClientAddress(),
						allocation.getServerAddress());
			} catch (StunException e) {
				logger.catching(e);
			}
		} else {
			logger.debug("permission not installed for - "
					+ event.getRemoteAdress());
		}
	}

}
