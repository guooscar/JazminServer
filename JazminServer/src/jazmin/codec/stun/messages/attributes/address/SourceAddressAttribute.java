/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes.address;

import jazmin.codec.stun.messages.attributes.StunAttribute;

/**
 * The SOURCE-ADDRESS attribute is present in Binding Responses.
 * 
 * It indicates the source IP address and port that the server is sending the
 * response from.<br>
 * A Its syntax is identical to that of MAPPED-ADDRESS.
 */
public class SourceAddressAttribute extends AddressAttribute {

	public static final String NAME = "SOURCE-ADDRESS";

	public SourceAddressAttribute() {
		super(StunAttribute.SOURCE_ADDRESS);
	}

	@Override
	public String getName() {
		return NAME;
	}
}
