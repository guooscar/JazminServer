/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes.address;

/**
 * The REMOTE-ADDRESS is present in Data Indication of old TURN versions.
 * 
 * It specifies the address and port where the data is sent.<br>
 * It is encoded in the same way as MAPPED-ADDRESS.
 */
public class RemoteAddressAttribute extends AddressAttribute {
	public static final String NAME = "REMOTE-ADDRESS";

	protected RemoteAddressAttribute() {
		super(AddressAttribute.REMOTE_ADDRESS);
	}

	@Override
	public String getName() {
		return NAME;
	}
}
