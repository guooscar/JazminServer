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
 * The CHANGED-ADDRESS attribute indicates the IP address and port where
 * responses would have been sent from if the "change IP" and "change port"
 * flags had been set in the CHANGE-REQUEST attribute of the Binding Request.
 * <p>
 * The attribute is always present in a Binding Response, independent of the
 * value of the flags. Its syntax is identical to MAPPED-ADDRESS.
 * </p>
 */
public class ChangedAddressAttribute extends AddressAttribute {

	public static final String NAME = "CHANGED-ADDRESS";

	public ChangedAddressAttribute() {
		super(StunAttribute.CHANGED_ADDRESS);
	}

	@Override
	public String getName() {
		return NAME;
	}
}
