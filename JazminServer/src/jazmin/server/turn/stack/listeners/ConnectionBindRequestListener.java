/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.listeners;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.turn.stack.stack.FiveTuple;
import jazmin.server.turn.stack.stack.TurnStack;

import org.ice4j.StunException;
import org.ice4j.StunMessageEvent;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.attribute.Attribute;
import org.ice4j.attribute.ConnectionIdAttribute;
import org.ice4j.attribute.ErrorCodeAttribute;
import org.ice4j.message.Message;
import org.ice4j.message.MessageFactory;
import org.ice4j.message.Response;
import org.ice4j.stack.RequestListener;
import org.ice4j.stack.StunStack;

/**
 * The class that would be handling and responding to incoming ConnectionBind
 * requests that are validated and sends a success or error response
 * 
 * @author Aakash Garg
 */
public class ConnectionBindRequestListener implements RequestListener {

	/**
	 * The <tt>Logger</tt> used by the <tt>ConnectionBindRequestListener</tt>
	 * class and its instances for logging output.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(ConnectionBindRequestListener.class);

	private final TurnStack turnStack;

	/**
	 * The indicator which determines whether this
	 * <tt>ConnectionBindrequestListener</tt> is currently started.
	 */
	private boolean started = false;

	/**
	 * Creates a new ConnectionBindRequestListener
	 * 
	 * @param turnStack
	 */
	public ConnectionBindRequestListener(StunStack stunStack) {
		this.turnStack = (TurnStack) stunStack;
	}

	@Override
	public void processRequest(StunMessageEvent evt)
			throws IllegalArgumentException {
		Message message = evt.getMessage();
		if (message.getMessageType() == Message.CONNECTION_BIND_REQUEST) {
			Response response = null;
			Character errorCode = null;

			TransportAddress clientAddress = evt.getRemoteAddress();
			TransportAddress serverAddress = evt.getLocalAddress();
			Transport transport = evt.getLocalAddress().getTransport();
			logger.debug("Received ConnectBind request " + evt + ", from "
					+ clientAddress + ", at " + serverAddress + " over "
					+ transport);
			ConnectionIdAttribute connectionId = null;
			if (transport != Transport.TCP) {
				errorCode = ErrorCodeAttribute.BAD_REQUEST;
				logger.debug("Transport is not TCP.");
			} else if (!message.containsAttribute(Attribute.CONNECTION_ID)) {
				errorCode = ErrorCodeAttribute.BAD_REQUEST;
				logger.debug("ConnectionID not found");
			} else {
				connectionId = (ConnectionIdAttribute) message
						.getAttribute(Attribute.CONNECTION_ID);
				logger.debug("Requested ConnectionId - "
						+ connectionId.getConnectionIdValue());
				if (!this.turnStack.isUnacknowledged(connectionId
						.getConnectionIdValue())) {
					errorCode = ErrorCodeAttribute.BAD_REQUEST;
					logger.debug("ConnectionId-"
							+ connectionId.getConnectionIdValue()
							+ " not present.");
				}
			}

			if (errorCode != null) {
				logger.debug("Creating Connection Bind Error Response, errorCode:"
						+ (int) errorCode);
				response = MessageFactory
						.createConnectionBindErrorResponse(ErrorCodeAttribute.BAD_REQUEST);
			} else {
				// processing logic.
				FiveTuple clientDataConnTuple = new FiveTuple(clientAddress,
						serverAddress, transport);
				this.turnStack.acknowledgeConnectionId(
						connectionId.getConnectionIdValue(),
						clientDataConnTuple);

				logger.debug("Creating ConnectionBind Success Response");
				response = MessageFactory.createConnectionBindResponse();
			}
			try {
				logger.debug("Sending ConnectionBind Response to "
						+ clientAddress + " through " + serverAddress);
				this.turnStack.sendResponse(evt.getTransactionID().getBytes(),
						response, serverAddress, clientAddress);
			} catch (StunException e) {
				logger.catching(e);
			} catch (Exception e) {
				logger.catching(e);
			}
		} else {
			return;
		}
	}

	/**
	 * Starts this <tt>ConnectionBindRequestListener</tt>. If it is not
	 * currently running, does nothing.
	 */
	public void start() {
		if (!started) {
			turnStack.addRequestListener(this);
			started = true;
		}
	}

	/**
	 * Stops this <tt>ConnectionBindRequestListenerr</tt>. A stopped
	 * <tt>ConnectionBindRequestListenerr</tt> can be restarted by calling
	 * {@link #start()} on it.
	 */
	public void stop() {
		turnStack.removeRequestListener(this);
		started = false;
	}
}
