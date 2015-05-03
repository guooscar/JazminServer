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
 * The XOR-RELAYED-ADDRESS attribute is given by a TURN server to indicates the
 * client its relayed address.
 * 
 * It has the same format as XOR-MAPPED-ADDRESS.
 */
public class XorRelayedAddressAttribute extends XorMappedAddressAttribute {
	public static final String NAME = "XOR-RELAYED-ADDRESS";

	public XorRelayedAddressAttribute() {
		super(StunAttribute.XOR_RELAYED_ADDRESS);
	}

	public String getName() {
		return NAME;
	}
}
