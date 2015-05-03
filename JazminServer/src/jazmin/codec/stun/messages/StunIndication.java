/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages;

/**
 * An indication descendant of the message class.
 * 
 * For example, indication messages is used by TURN protocol to send and receive
 * encapsulated data.
 */
public class StunIndication extends StunMessage {

	public StunIndication() {
		super();
	}

	/**
	 * Checks whether indicationType is a valid indication type and if yes sets
	 * it as the type of this instance.
	 * 
	 * @param indicationType
	 *            the type to set
	 * @throws IllegalArgumentException
	 *             if indicationType is not a valid indication type
	 */
	@Override
	public void setMessageType(char indicationType)
			throws IllegalArgumentException {
		/*
		 * old TURN DATA indication type is an indication despite 0x0115 &
		 * 0x0110 indicates STUN error response type
		 */
		if (!isIndicationType(indicationType)
				&& indicationType != StunMessage.OLD_DATA_INDICATION) {
			throw new IllegalArgumentException((int) (indicationType)
					+ " - is not a valid indication type.");
		}
		super.setMessageType(indicationType);
	}

}
