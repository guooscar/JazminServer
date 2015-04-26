/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.listeners;

import jazmin.log.*;

import jazmin.server.turn.stack.stack.Allocation;
import jazmin.server.turn.stack.stack.FiveTuple;
import jazmin.server.turn.stack.stack.TurnStack;

import org.ice4j.StunMessageEvent;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.attribute.Attribute;
import org.ice4j.attribute.ErrorCodeAttribute;
import org.ice4j.attribute.LifetimeAttribute;
import org.ice4j.message.Message;
import org.ice4j.message.MessageFactory;
import org.ice4j.message.Response;
import org.ice4j.stack.RequestListener;
import org.ice4j.stack.StunStack;

/**
 * The class that would be handling and responding to incoming Refresh requests
 * that are validated and sends a success or error response
 * 
 * @author Aakash Garg
 */
public class RefreshRequestListener implements RequestListener {
	/**
	 * The <tt>Logger</tt> used by the <tt>RefreshRequestListener</tt> class and
	 * its instances for logging output.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(RefreshRequestListener.class);

	private final TurnStack turnStack;

	/**
	 * The indicator which determines whether this
	 * <tt>RefreshRequestListener</tt> is currently started.
	 */
	private boolean started = false;

	/**
	 * Creates a new RefreshRequestListener
	 * 
	 * @param turnStack
	 */
	public RefreshRequestListener(StunStack turnStack) {
		if (turnStack instanceof TurnStack) {
			this.turnStack = (TurnStack) turnStack;
		} else {
			throw new IllegalArgumentException("This is not a TurnStack!");
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.ice4j.stack.RequestListener#processRequest(org.ice4j.StunMessageEvent)
	 */
	@Override
	public void processRequest(StunMessageEvent evt)
			throws IllegalArgumentException {
		
		Message message = evt.getMessage();
		if (message.getMessageType() == Message.REFRESH_REQUEST) {
			logger.debug("Received refresh request " + evt);

			LifetimeAttribute lifetimeAttribute = (LifetimeAttribute) message
					.getAttribute(Attribute.LIFETIME);

			Response response = null;
			TransportAddress clientAddress = evt.getRemoteAddress();
			TransportAddress serverAddress = evt.getLocalAddress();
			Transport transport = serverAddress.getTransport();
			FiveTuple fiveTuple = new FiveTuple(clientAddress, serverAddress,
					transport);

			Allocation allocation = this.turnStack
					.getServerAllocation(fiveTuple);
			if (allocation != null) {
				if (lifetimeAttribute != null) {
					logger.debug("Refreshing allocation with relay addr "
							+ allocation.getRelayAddress() + " with lifetime "
							+ lifetimeAttribute.getLifetime());
					allocation.refresh(lifetimeAttribute.getLifetime());
					response = MessageFactory
							.createRefreshResponse((int) allocation
									.getLifetime());
				} else {
					logger.debug("Refreshing allocation with relay addr "
							+ allocation.getRelayAddress()
							+ " with default lifetime");
					allocation.refresh();
					response = MessageFactory
							.createRefreshResponse((int) allocation
									.getLifetime());
				}
			} else {
				logger.debug("Allocation mismatch error");
				response = MessageFactory
						.createRefreshErrorResponse(ErrorCodeAttribute.ALLOCATION_MISMATCH);
			}
			try {
				turnStack
						.sendResponse(evt.getTransactionID().getBytes(),
								response, evt.getLocalAddress(),
								evt.getRemoteAddress());
			} catch (Exception e) {
				logger.catching(e);
				// try to trigger a 500 response although if this one failed,
				throw new RuntimeException("Failed to send a response", e);
			}
		} else {
			return;
		}

	}

	/**
	 * Starts this <tt>RefreshRequestListener</tt>. If it is not currently
	 * running, does nothing.
	 */
	public void start() {
		if (!started) {
			turnStack.addRequestListener(this);
			started = true;
		}
	}

	/**
	 * Stops this <tt>RefreshRequestListenerr</tt>. A stopped
	 * <tt>ValidatedRequestListenerr</tt> can be restarted by calling
	 * {@link #start()} on it.
	 */
	public void stop() {
		turnStack.removeRequestListener(this);
		started = false;
	}
}
