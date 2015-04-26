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
 * The class that would be handling and responding to incoming CreatePermission
 * response.
 * 
 * @author Aakash Garg
 */
public class CreatePermissionResponseCollector implements ResponseCollector {
	/**
	 * The <tt>Logger</tt> used by the
	 * <tt>CreatePermissionResponseCollector</tt> class and its instances for
	 * logging output.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(CreatePermissionResponseCollector.class);

	private final StunStack stunStack;

	/**
	 * Creates a new CreatePermissionResponseCollector
	 * 
	 * @param turnStack
	 */
	public CreatePermissionResponseCollector(StunStack stunStack) {
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
		if (message.getMessageType() == Message.ALLOCATE_ERROR_RESPONSE) {
			ErrorCodeAttribute errorCodeAttribute = (ErrorCodeAttribute) message
					.getAttribute(Attribute.ERROR_CODE);
			switch (errorCodeAttribute.getErrorCode()) {
			case ErrorCodeAttribute.BAD_REQUEST:
				// logic for processing bad request error
				break;
			case ErrorCodeAttribute.INSUFFICIENT_CAPACITY:
				// logic for processing insufficient capacity error
				break;
			case ErrorCodeAttribute.FORBIDDEN:
				// logic for processing forbidden error code
				break;
			default:
				logger.error("error : Received error response with error code "
						+ errorCodeAttribute.getErrorCode() + evt);
			}
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
