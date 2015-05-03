/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages;

/**
 * Represents a STUN Request message.
 */
public class StunRequest extends StunMessage {

	public StunRequest() {
		super();
	}

	@Override
	public void setMessageType(char requestType)
			throws IllegalArgumentException {
		if (!isRequestType(requestType)) {
			throw new IllegalArgumentException((int) (requestType)
					+ " - is not a valid request type.");
		}
		super.setMessageType(requestType);
	}
}
