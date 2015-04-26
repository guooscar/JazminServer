/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.collectors;


import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import org.ice4j.ResponseCollector;
import org.ice4j.StunResponseEvent;
import org.ice4j.StunTimeoutEvent;
import org.ice4j.attribute.Attribute;
import org.ice4j.attribute.ErrorCodeAttribute;
import org.ice4j.message.Message;
import org.ice4j.stack.StunStack;

/**
 * The class that would be handling and responding to incoming Connect response.
 * 
 * @author Aakash Garg
 */
public class ConnectResponseCollector implements ResponseCollector {

	/**
	 * The <tt>Logger</tt> used by the <tt>ConnectresponseCollector</tt> class
	 * and its instances for logging output.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(ConnectResponseCollector.class);

	private final StunStack stunStack;

	/**
	 * Creates a new ConnectresponseCollector
	 * 
	 * @param turnStack
	 */
	public ConnectResponseCollector(StunStack stunStack) {
		this.stunStack = stunStack;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ice4j.ResponseCollector#processResponse(org.ice4j.StunResponseEvent)
	 */
	@Override
	public void processResponse(StunResponseEvent evt) {
		if (logger.isDebugEnabled()) {
			logger.debug("Received response " + evt);
		}
		Message message = evt.getMessage();
		if (message.getMessageType() == Message.CONNECT_ERROR_RESPONSE) {
			ErrorCodeAttribute errorCodeAttribute = (ErrorCodeAttribute) message
					.getAttribute(Attribute.ERROR_CODE);
			switch (errorCodeAttribute.getErrorCode()) {
			case ErrorCodeAttribute.ALLOCATION_MISMATCH:
				// code for bad response error
				break;
			case ErrorCodeAttribute.CONNECTION_ALREADY_EXISTS:
				// code for processing connection already exists error
				break;
			case ErrorCodeAttribute.FORBIDDEN:
				// code for processing forbidden error code
				break;
			case ErrorCodeAttribute.CONNECTION_TIMEOUT_OR_FAILURE:
				// code for processing connection timeout or failure error code.
				break;
			}
		} else if (message.getMessageType() == Message.CONNECT_RESPONSE) {
			// code for doing processing of Connect success response
		} else {
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ice4j.ResponseCollector#processTimeout(org.ice4j.StunTimeoutEvent)
	 */
	@Override
	public void processTimeout(StunTimeoutEvent event) {
		// TODO Auto-generated method stub

	}

}
