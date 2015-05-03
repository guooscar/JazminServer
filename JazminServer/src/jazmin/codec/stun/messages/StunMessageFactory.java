/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages;

import jazmin.codec.stun.TransportAddress;
import jazmin.codec.stun.messages.attributes.StunAttributeFactory;
import jazmin.codec.stun.messages.attributes.address.XorMappedAddressAttribute;

/**
 * Factory that provides STUN Messages.
 */
public class StunMessageFactory {

	/**
	 * Creates a BindingResponse in a RFC5389 compliant manner containing a
	 * single <tt>XOR-MAPPED-ADDRESS</tt> attribute
	 * 
	 * @param request
	 *            the request that created the transaction that this response
	 *            will belong to.
	 * @param mappedAddress
	 *            the address to assign the mappedAddressAttribute
	 * @return a BindingResponse assigning the specified values to mandatory
	 *         headers.
	 * @throws IllegalArgumentException
	 *             if there was something wrong with the way we are trying to
	 *             create the response.
	 */
	public static StunResponse createBindingResponse(StunRequest request,
			TransportAddress mappedAddress) throws IllegalArgumentException {
		StunResponse bindingResponse = new StunResponse();
		bindingResponse.setMessageType(StunMessage.BINDING_SUCCESS_RESPONSE);

		// XOR mapped address
		XorMappedAddressAttribute xorMappedAddressAttribute = StunAttributeFactory
				.createXorMappedAddressAttribute(mappedAddress,
						request.getTransactionId());

		bindingResponse.addAttribute(xorMappedAddressAttribute);

		return bindingResponse;
	}
	
}
