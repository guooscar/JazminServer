/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.listeners;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.turn.stack.IndicationListener;
import jazmin.server.turn.stack.stack.Allocation;
import jazmin.server.turn.stack.stack.TurnStack;

import org.ice4j.StunException;
import org.ice4j.TransportAddress;
import org.ice4j.attribute.Attribute;
import org.ice4j.attribute.DataAttribute;
import org.ice4j.attribute.XorPeerAddressAttribute;
import org.ice4j.message.Indication;
import org.ice4j.message.Message;
import org.ice4j.stack.RawMessage;

/**
 * Class to handle the incoming Send indications.
 * 
 * @author Aakash Garg
 * 
 */
public class SendIndicationListener extends IndicationListener {
	/**
	 * The <tt>Logger</tt> used by the <tt>SendIndicationListener</tt> class and
	 * its instances for logging output.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(SendIndicationListener.class);

	/**
	 * parametrised constructor.
	 * 
	 * @param turnStack
	 *            the turnStack to set for this class.
	 */
	public SendIndicationListener(TurnStack turnStack) {
		super(turnStack);
	}

	/**
	 * Handles the incoming send indication.
	 * 
	 * @param ind
	 *            the indication to handle.
	 * @param alloc
	 *            the allocation associated with message.
	 */
	@Override
	public void handleIndication(Indication ind, Allocation alloc) {
		if (ind.getMessageType() == Message.SEND_INDICATION) {
			logger.debug("Received a Send Indication message.");
			byte[] tran = ind.getTransactionID();
			XorPeerAddressAttribute xorPeerAddress = (XorPeerAddressAttribute) ind
					.getAttribute(Attribute.XOR_PEER_ADDRESS);
			xorPeerAddress.setAddress(xorPeerAddress.getAddress(), tran);
			DataAttribute data = (DataAttribute) ind
					.getAttribute(Attribute.DATA);
			TransportAddress peerAddr = xorPeerAddress.getAddress();
			if (alloc != null && alloc.isPermitted(peerAddr)) {
				RawMessage udpMessage = new RawMessage(data.getData(),
						data.getDataLength(), peerAddr, alloc.getRelayAddress());
				try {
					this.getTurnStack().sendUdpMessage(udpMessage, peerAddr,
							alloc.getRelayAddress());
					logger.debug("Sent SendIndiaction to " + peerAddr
							+ " from " + alloc.getRelayAddress());
				} catch (StunException e) {
					logger.catching(e);
				}
			}
		}
	}

}
