/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.listeners;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.turn.stack.stack.Allocation;
import jazmin.server.turn.stack.stack.FiveTuple;
import jazmin.server.turn.stack.stack.Permission;
import jazmin.server.turn.stack.stack.TurnStack;

import org.ice4j.StunMessageEvent;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.attribute.Attribute;
import org.ice4j.attribute.ErrorCodeAttribute;
import org.ice4j.attribute.LifetimeAttribute;
import org.ice4j.attribute.XorPeerAddressAttribute;
import org.ice4j.message.Message;
import org.ice4j.message.MessageFactory;
import org.ice4j.message.Response;
import org.ice4j.stack.RequestListener;
import org.ice4j.stack.StunStack;

/**
 * The class that would be handling and responding to incoming CreatePermission
 * requests that are CreatePermission and sends a success or error response
 * 
 * @author Aakash Garg
 */
public class CreatePermissionRequestListener implements RequestListener {
	/**
	 * The <tt>Logger</tt> used by the <tt>CreatePermissionRequestListener</tt>
	 * class and its instances for logging output.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(CreatePermissionRequestListener.class);

	private final TurnStack turnStack;

	/**
	 * The indicator which determines whether this
	 * <tt>CreatePermissionrequestListener</tt> is currently started.
	 */
	private boolean started = false;

	/**
	 * Creates a new CreatePermissionRequestListener
	 * 
	 * @param turnStack
	 */
	public CreatePermissionRequestListener(StunStack turnStack) {
		if (turnStack instanceof TurnStack) {
			this.turnStack = (TurnStack) turnStack;
		} else {
			throw new IllegalArgumentException("This is not a TurnStack!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ice4j.stack.RequestListener#processRequest(org.ice4j.StunMessageEvent
	 * )
	 */
	@Override
	public void processRequest(StunMessageEvent evt)
			throws IllegalArgumentException {

		Message message = evt.getMessage();
		if (message.getMessageType() == Message.CREATEPERMISSION_REQUEST) {
			logger.debug("Received create permission request ");
			logger.debug("Event tran : " + evt.getTransactionID());

			XorPeerAddressAttribute xorPeerAddressAttribute = (XorPeerAddressAttribute) message
					.getAttribute(Attribute.XOR_PEER_ADDRESS);
			if (xorPeerAddressAttribute != null) {
				xorPeerAddressAttribute.setAddress(xorPeerAddressAttribute
						.getAddress(), evt.getTransactionID().getBytes());
			}
			// we should get multiple xor peer address attributes here.
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

			Character errorCode = null;
			if (xorPeerAddressAttribute == null || allocation == null) {
				errorCode = ErrorCodeAttribute.BAD_REQUEST;
			} else if (!TurnStack.isIPAllowed(xorPeerAddressAttribute
					.getAddress())) {
				logger.debug("Peer Address requested "
						+ xorPeerAddressAttribute.getAddress()
						+ " "
						+ TurnStack.isIPAllowed(xorPeerAddressAttribute
								.getAddress()));
				errorCode = ErrorCodeAttribute.FORBIDDEN;
			} else if (!allocation.canHaveMorePermisions()) {
				errorCode = ErrorCodeAttribute.INSUFFICIENT_CAPACITY;
			}
			if (errorCode != null) {
				logger.debug("Creating error response : " + (int) errorCode);
				response = MessageFactory
						.createCreatePermissionErrorResponse(errorCode);
			} else {
				logger.debug("Creating success response.");
				TransportAddress peerAddress = xorPeerAddressAttribute
						.getAddress();
				Permission permission = null;
				if (lifetimeAttribute != null) {
					permission = new Permission(peerAddress,
							lifetimeAttribute.getLifetime());
				} else {
					permission = new Permission(peerAddress);
				}

				logger.debug("Peer Address requested "
						+ xorPeerAddressAttribute.getAddress()
						+ " "
						+ TurnStack.isIPAllowed(xorPeerAddressAttribute
								.getAddress()));
				allocation.addNewPermission(permission);
				logger.debug("Added permission to allocation.");
				response = MessageFactory.createCreatePermissionResponse();
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
	 * Starts this <tt>CreatePermissionRequestListener</tt>. If it is not
	 * currently running, does nothing.
	 */
	public void start() {
		if (!started) {
			turnStack.addRequestListener(this);
			started = true;
		}
	}

	/**
	 * Stops this <tt>CreatePermissionRequestListenerr</tt>. A stopped
	 * <tt>CreatePermissionRequestListenerr</tt> can be restarted by calling
	 * {@link #start()} on it.
	 */
	public void stop() {
		turnStack.removeRequestListener(this);
		started = false;
	}

}
