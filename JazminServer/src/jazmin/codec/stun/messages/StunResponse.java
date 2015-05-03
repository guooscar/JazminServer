/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages;

/**
 * Represents a STUN Response message. <br>
 * A response message can be successful or erroneous.
 */
public class StunResponse extends StunMessage {

	public StunResponse() {
		super();
	}

	/**
	 * Determines whether this instance represents a STUN error response.
	 * 
	 * @return <tt>true</tt> if this instance represents a STUN error response;
	 *         otherwise, <tt>false</tt>
	 */
	public boolean isErrorResponse() {
		return isErrorResponseType(getMessageType());
	}

	/**
	 * Determines whether this instance represents a STUN success response.
	 * 
	 * @return <tt>true</tt> if this instance represents a STUN success
	 *         response; otherwise, <tt>false</tt>
	 */
	public boolean isSuccessResponse() {
		return isSuccessResponseType(getMessageType());
	}

	public void setMessageType(char responseType)
			throws IllegalArgumentException {
		if (!isResponseType(responseType)) {
			throw new IllegalArgumentException(Integer.toString(responseType)
					+ " is not a valid response type.");
		}
		super.setMessageType(responseType);
	}
}
