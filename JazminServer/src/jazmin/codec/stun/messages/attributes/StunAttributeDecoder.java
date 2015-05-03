/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes;

import jazmin.codec.stun.StunException;
import jazmin.codec.stun.messages.attributes.address.AlternateServerAttribute;
import jazmin.codec.stun.messages.attributes.address.ChangedAddressAttribute;
import jazmin.codec.stun.messages.attributes.address.ReflectedFromAttribute;
import jazmin.codec.stun.messages.attributes.address.ResponseAddressAttribute;
import jazmin.codec.stun.messages.attributes.address.SourceAddressAttribute;
import jazmin.codec.stun.messages.attributes.address.XorMappedAddressAttribute;
import jazmin.codec.stun.messages.attributes.address.XorPeerAddressAttribute;
import jazmin.codec.stun.messages.attributes.address.XorRelayedAddressAttribute;
import jazmin.codec.stun.messages.attributes.control.ControlledAttribute;
import jazmin.codec.stun.messages.attributes.control.ControllingAttribute;
import jazmin.codec.stun.messages.attributes.data.DataAttribute;
import jazmin.codec.stun.messages.attributes.general.ChangeRequestAttribute;
import jazmin.codec.stun.messages.attributes.general.ChannelNumberAttribute;
import jazmin.codec.stun.messages.attributes.general.DontFragmentAttribute;
import jazmin.codec.stun.messages.attributes.general.ErrorCodeAttribute;
import jazmin.codec.stun.messages.attributes.general.EvenPortAttribute;
import jazmin.codec.stun.messages.attributes.general.FingerprintAttribute;
import jazmin.codec.stun.messages.attributes.general.LifetimeAttribute;
import jazmin.codec.stun.messages.attributes.general.MappedAddressAttribute;
import jazmin.codec.stun.messages.attributes.general.MessageIntegrityAttribute;
import jazmin.codec.stun.messages.attributes.general.NonceAttribute;
import jazmin.codec.stun.messages.attributes.general.OptionalAttribute;
import jazmin.codec.stun.messages.attributes.general.PriorityAttribute;
import jazmin.codec.stun.messages.attributes.general.RealmAttribute;
import jazmin.codec.stun.messages.attributes.general.RequestedTransportAttribute;
import jazmin.codec.stun.messages.attributes.general.ReservationTokenAttribute;
import jazmin.codec.stun.messages.attributes.general.SoftwareAttribute;
import jazmin.codec.stun.messages.attributes.general.UnknownAttributesAttribute;
import jazmin.codec.stun.messages.attributes.general.UseCandidateAttribute;
import jazmin.codec.stun.messages.attributes.general.UsernameAttribute;
import jazmin.codec.stun.messages.attributes.general.XorOnlyAttribute;

/**
 * Provides utilities for decoding a binary stream into an Stun Attribute
 * 
 * @see StunAttribute
 */
public class StunAttributeDecoder {

	/**
	 * Decodes the specified binary array and returns the corresponding
	 * attribute object.
	 * 
	 * @param bytes
	 *            the binary array that should be decoded.
	 * @param offset
	 *            the index where the message starts.
	 * @param length
	 *            the number of bytes that the message is long.
	 * 
	 * @return An object representing the attribute encoded in bytes or null if
	 *         the attribute was not recognized.
	 * 
	 * @throws StunException
	 *             if bytes is not a valid STUN attribute.
	 */
	public static StunAttribute decode(byte[] bytes, char offset, char length)
			throws StunException {
		if (bytes == null || bytes.length < StunAttribute.HEADER_LENGTH) {
			throw new StunException(StunException.ILLEGAL_ARGUMENT,
					"Could not decode the specified binary array.");
		}

		// Discover attribute type
		char attributeType = (char) ((bytes[offset] << 8) | bytes[offset + 1]);
		int len1 = bytes[offset + 2] & 0xff;
		int len2 = bytes[offset + 3] & 0xff;
		char attributeLength = (char) ((len1 << 8) | len2);

		if (attributeLength > bytes.length - offset)
			throw new StunException(StunException.ILLEGAL_ARGUMENT,
					"Could not decode the specified binary array.");

		StunAttribute decodedAttribute = null;

		switch (attributeType) {
		/* STUN attributes */
		case StunAttribute.CHANGE_REQUEST:
			decodedAttribute = new ChangeRequestAttribute();
			break;
		case StunAttribute.CHANGED_ADDRESS:
			decodedAttribute = new ChangedAddressAttribute();
			break;
		case StunAttribute.MAPPED_ADDRESS:
			decodedAttribute = new MappedAddressAttribute();
			break;
		case StunAttribute.ERROR_CODE:
			decodedAttribute = new ErrorCodeAttribute();
			break;
		case StunAttribute.MESSAGE_INTEGRITY:
			decodedAttribute = new MessageIntegrityAttribute();
			break;
		// case StunAttribute.PASSWORD:
		// handle as an unknown attribute
		case StunAttribute.REFLECTED_FROM:
			decodedAttribute = new ReflectedFromAttribute();
			break;
		case StunAttribute.RESPONSE_ADDRESS:
			decodedAttribute = new ResponseAddressAttribute();
			break;
		case StunAttribute.SOURCE_ADDRESS:
			decodedAttribute = new SourceAddressAttribute();
			break;
		case StunAttribute.UNKNOWN_ATTRIBUTES:
			decodedAttribute = new UnknownAttributesAttribute();
			break;
		case StunAttribute.XOR_MAPPED_ADDRESS:
			decodedAttribute = new XorMappedAddressAttribute();
			break;
		case StunAttribute.XOR_ONLY:
			decodedAttribute = new XorOnlyAttribute();
			break;
		case StunAttribute.SOFTWARE:
			decodedAttribute = new SoftwareAttribute();
			break;
		case StunAttribute.USERNAME:
			decodedAttribute = new UsernameAttribute();
			break;
		case StunAttribute.REALM:
			decodedAttribute = new RealmAttribute();
			break;
		case StunAttribute.NONCE:
			decodedAttribute = new NonceAttribute();
			break;
		case StunAttribute.FINGERPRINT:
			decodedAttribute = new FingerprintAttribute();
			break;
		case StunAttribute.ALTERNATE_SERVER:
			decodedAttribute = new AlternateServerAttribute();
			break;
		case StunAttribute.CHANNEL_NUMBER:
			decodedAttribute = new ChannelNumberAttribute();
			break;
		case StunAttribute.LIFETIME:
			decodedAttribute = new LifetimeAttribute();
			break;
		case StunAttribute.XOR_PEER_ADDRESS:
			decodedAttribute = new XorPeerAddressAttribute();
			break;
		case StunAttribute.DATA:
			decodedAttribute = new DataAttribute();
			break;
		case StunAttribute.XOR_RELAYED_ADDRESS:
			decodedAttribute = new XorRelayedAddressAttribute();
			break;
		case StunAttribute.EVEN_PORT:
			decodedAttribute = new EvenPortAttribute();
			break;
		case StunAttribute.REQUESTED_TRANSPORT:
			decodedAttribute = new RequestedTransportAttribute();
			break;
		case StunAttribute.DONT_FRAGMENT:
			decodedAttribute = new DontFragmentAttribute();
			break;
		case StunAttribute.RESERVATION_TOKEN:
			decodedAttribute = new ReservationTokenAttribute();
			break;
		case StunAttribute.PRIORITY:
			decodedAttribute = new PriorityAttribute();
			break;
		case StunAttribute.ICE_CONTROLLING:
			decodedAttribute = new ControllingAttribute();
			break;
		case StunAttribute.ICE_CONTROLLED:
			decodedAttribute = new ControlledAttribute();
			break;
		case StunAttribute.USE_CANDIDATE:
			decodedAttribute = new UseCandidateAttribute();
			break;

		// According to rfc3489 we should silently ignore unknown attributes.
		default:
			decodedAttribute = new OptionalAttribute(
					StunAttribute.UNKNOWN_OPTIONAL_ATTRIBUTE);
			break;
		}

		decodedAttribute.setAttributeType(attributeType);
		decodedAttribute.setLocationInMessage(offset);

		decodedAttribute.decodeAttributeBody(bytes,
				(char) (StunAttribute.HEADER_LENGTH + offset), attributeLength);

		return decodedAttribute;
	}

}
