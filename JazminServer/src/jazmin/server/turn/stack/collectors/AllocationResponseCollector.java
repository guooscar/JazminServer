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
import org.ice4j.StunException;
import org.ice4j.StunResponseEvent;
import org.ice4j.StunTimeoutEvent;
import org.ice4j.attribute.Attribute;
import org.ice4j.attribute.AttributeFactory;
import org.ice4j.attribute.ErrorCodeAttribute;
import org.ice4j.attribute.MessageIntegrityAttribute;
import org.ice4j.attribute.NonceAttribute;
import org.ice4j.attribute.RequestedTransportAttribute;
import org.ice4j.attribute.UsernameAttribute;
import org.ice4j.message.Message;
import org.ice4j.message.MessageFactory;
import org.ice4j.message.Request;
import org.ice4j.stack.StunStack;
import org.ice4j.stack.TransactionID;

/**
 * The class that would be handling to incoming Allocation responses.
 * 
 * @author Aakash Garg
 */
public class AllocationResponseCollector implements ResponseCollector {
	/**
	 * The <tt>Logger</tt> used by the <tt>AllocationresponseCollector</tt>
	 * class and its instances for logging output.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(AllocationResponseCollector.class);

	private final StunStack stunStack;

	/**
	 * Creates a new AllocationresponseCollector
	 * 
	 * @param turnStack
	 */
	public AllocationResponseCollector(StunStack stunStack) {
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
			NonceAttribute nonceAttr = (NonceAttribute) message
					.getAttribute(Attribute.NONCE);
			// System.out.println("Nonce : "+new Nonce(nonceAttr.getNonce()));
			Request request = MessageFactory.createAllocateRequest();
			TransactionID tran = TransactionID.createNewTransactionID();
			try {
				request.setTransactionID(tran.getBytes());
			} catch (StunException e1) {
				logger.catching(e1);
			}
			request.putAttribute(nonceAttr);
			String username = "JitsiGsocStudent";
			UsernameAttribute usernameAttr = AttributeFactory
					.createUsernameAttribute(username + ":");
			/*
			 * byte[] key = this.stunStack.getCredentialsManager().getLocalKey(
			 * username); System.out.println("Username found " +
			 * (this.stunStack.getCredentialsManager()
			 * .checkLocalUserName(username))); System.out.println("User " +
			 * username + " found : " + TurnStack.toHexString(key));
			 * 
			 * byte[] messageB = request.encode(stunStack);
			 */MessageIntegrityAttribute msgInt = AttributeFactory
					.createMessageIntegrityAttribute(username);
			RequestedTransportAttribute reqTrans = AttributeFactory
					.createRequestedTransportAttribute(RequestedTransportAttribute.UDP);
			try {
				// msgInt.encode(stunStack, messageB, 0, messageB.length);
			} catch (Exception e) {
				e.printStackTrace();
			}
			request.putAttribute(reqTrans);
			request.putAttribute(usernameAttr);
			request.putAttribute(msgInt);
			try {
				this.stunStack.sendRequest(request, evt.getRemoteAddress(),
						evt.getLocalAddress(), this);
			} catch (Exception e) {
				logger.catching(e);
			}
			if (errorCodeAttribute != null) {
				logger.error("Error Code : "
						+ (int) errorCodeAttribute.getErrorCode());
			}
			switch (errorCodeAttribute.getErrorCode()) {
			case ErrorCodeAttribute.BAD_REQUEST:
				// code for bad response error
				break;
			case ErrorCodeAttribute.UNAUTHORIZED:
				// code for unauthorised error code
				break;
			case ErrorCodeAttribute.FORBIDDEN:
				// code for forbidden error code
				break;
			case ErrorCodeAttribute.UNKNOWN_ATTRIBUTE:
				// code for Unknown Attribute error code
				break;
			case ErrorCodeAttribute.ALLOCATION_MISMATCH:
				// code for Allocation mismatch Error
				break;
			case ErrorCodeAttribute.STALE_NONCE:
				// code for Stale Nonce error code
				break;
			case ErrorCodeAttribute.WRONG_CREDENTIALS:
				// code for wrong credentials error code
				break;
			case ErrorCodeAttribute.UNSUPPORTED_TRANSPORT_PROTOCOL:
				// code for unsupported transport protocol
				break;
			case ErrorCodeAttribute.ALLOCATION_QUOTA_REACHED:
				// code for allocation quota reached
				break;
			case ErrorCodeAttribute.INSUFFICIENT_CAPACITY:
				// code for insufficient capacity
				break;

			}
		} else if (message.getMessageType() == Message.ALLOCATE_RESPONSE) {
			logger.debug("Allocate Sucess Response.");
			// code for doing processing of Allocation success response
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

	}

}
