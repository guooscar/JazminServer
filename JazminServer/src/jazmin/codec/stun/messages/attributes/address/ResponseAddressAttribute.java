/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes.address;

/**
 * The RESPONSE-ADDRESS attribute indicates where the response to a Binding
 * Request should be sent.
 * 
 * Its syntax is identical to MAPPED-ADDRESS.
 */
public class ResponseAddressAttribute extends AddressAttribute {

	public static final String NAME = "RESPONSE-ADDRESS";

	public ResponseAddressAttribute() {
		super(RESPONSE_ADDRESS);
	}

	@Override
	public String getName() {
		return NAME;
	}
}
