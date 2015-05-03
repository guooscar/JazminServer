/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;

import jazmin.codec.stun.StunException;
import jazmin.codec.stun.TransportAddress;
import jazmin.codec.stun.messages.attributes.address.ChangedAddressAttribute;
import jazmin.codec.stun.messages.attributes.address.DestinationAddressAttribute;
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
import jazmin.codec.stun.messages.attributes.general.ErrorCodeAttribute;
import jazmin.codec.stun.messages.attributes.general.EvenPortAttribute;
import jazmin.codec.stun.messages.attributes.general.FingerprintAttribute;
import jazmin.codec.stun.messages.attributes.general.LifetimeAttribute;
import jazmin.codec.stun.messages.attributes.general.MagicCookieAttribute;
import jazmin.codec.stun.messages.attributes.general.MappedAddressAttribute;
import jazmin.codec.stun.messages.attributes.general.MessageIntegrityAttribute;
import jazmin.codec.stun.messages.attributes.general.NonceAttribute;
import jazmin.codec.stun.messages.attributes.general.PriorityAttribute;
import jazmin.codec.stun.messages.attributes.general.RealmAttribute;
import jazmin.codec.stun.messages.attributes.general.RequestedTransportAttribute;
import jazmin.codec.stun.messages.attributes.general.ReservationTokenAttribute;
import jazmin.codec.stun.messages.attributes.general.SoftwareAttribute;
import jazmin.codec.stun.messages.attributes.general.UnknownAttributesAttribute;
import jazmin.codec.stun.messages.attributes.general.UseCandidateAttribute;
import jazmin.codec.stun.messages.attributes.general.UsernameAttribute;

/**
 * Factory that provides STUN/TURN/ICE attributes.
 */
public class StunAttributeFactory {

	/**
	 * Creates a ChangeRequestAttribute with "false" values for the changeIP and
	 * changePort flags.
	 * 
	 * @return the newly created ChangeRequestAttribute.
	 */
	public static ChangeRequestAttribute createChangeRequestAttribute() {
		return createChangeRequestAttribute(false, false);
	}

	/**
	 * Creates a ChangeRequestAttribute with the specified flag values.
	 * 
	 * @param changeIP
	 *            the value of the changeIP flag.
	 * @param changePort
	 *            the value of the changePort flag.
	 * @return the newly created ChangeRequestAttribute.
	 */
	public static ChangeRequestAttribute createChangeRequestAttribute(
			boolean changeIP, boolean changePort) {
		ChangeRequestAttribute attribute = new ChangeRequestAttribute();

		attribute.setAddressChanging(changeIP);
		attribute.setPortChanging(changePort);

		return attribute;
	}

	/**
	 * Creates a changedAddressAttribute of the specified type and with the
	 * specified address and port
	 * 
	 * @param address
	 *            the address value of the address attribute
	 * @return the newly created address attribute.
	 */
	public static ChangedAddressAttribute createChangedAddressAttribute(
			TransportAddress address) {
		ChangedAddressAttribute attribute = new ChangedAddressAttribute();

		attribute.setAddress(address);

		return attribute;
	}

	/**
	 * Creates an ErrorCodeAttribute with the specified error class and number
	 * and a default reason phrase.
	 * 
	 * @param errorClass
	 *            a valid error class.
	 * @param errorNumber
	 *            a valid error number.
	 * @return the newly created attribute.
	 * @throws StunException
	 *             if the error class or number have invalid values according to
	 *             rfc3489.
	 */
	public static ErrorCodeAttribute createErrorCodeAttribute(byte errorClass,
			byte errorNumber) throws StunException {
		return createErrorCodeAttribute(errorClass, errorNumber, null);
	}

	/**
	 * Creates an ErrorCodeAttribute with the specified error class, number and
	 * reason phrase.
	 * 
	 * @param errorClass
	 *            a valid error class.
	 * @param errorNumber
	 *            a valid error number.
	 * @param reasonPhrase
	 *            a human readable reason phrase. A null reason phrase would be
	 *            replaced (if possible) by a default one as defined byte the
	 *            rfc3489.
	 * @return the newly created attribute.
	 * @throws StunException
	 *             if the error class or number have invalid values according to
	 *             rfc3489.
	 */
	public static ErrorCodeAttribute createErrorCodeAttribute(byte errorClass,
			byte errorNumber, String reasonPhrase) throws StunException {
		ErrorCodeAttribute attribute = new ErrorCodeAttribute();

		attribute.setErrorClass(errorClass);
		attribute.setErrorNumber(errorNumber);

		attribute.setReasonPhrase(reasonPhrase == null ? ErrorCodeAttribute
				.getDefaultReasonPhrase(attribute.getErrorCode())
				: reasonPhrase);

		return attribute;
	}

	/**
	 * Creates an ErrorCodeAttribute with the specified error code and a default
	 * reason phrase.
	 * 
	 * @param errorCode
	 *            a valid error code.
	 * @return the newly created attribute.
	 * @throws StunException
	 *             if errorCode is not a valid error code as defined by rfc3489
	 */
	public static ErrorCodeAttribute createErrorCodeAttribute(char errorCode)
			throws StunException {
		return createErrorCodeAttribute(errorCode, null);
	}

	/**
	 * Creates an ErrorCodeAttribute with the specified error code and reason
	 * phrase.
	 * 
	 * @param errorCode
	 *            a valid error code.
	 * @param reasonPhrase
	 *            a human readable reason phrase. A null reason phrase would be
	 *            replaced (if possible) by a default one as defined byte the
	 *            rfc3489.
	 * 
	 * @return the newly created attribute.
	 * @throws IllegalArgumentException
	 *             if errorCode is not a valid error code as defined by rfc3489
	 */
	public static ErrorCodeAttribute createErrorCodeAttribute(char errorCode,
			String reasonPhrase) throws IllegalArgumentException {
		ErrorCodeAttribute attribute = new ErrorCodeAttribute();

		attribute.setErrorCode(errorCode);
		attribute.setReasonPhrase(reasonPhrase == null ? ErrorCodeAttribute
				.getDefaultReasonPhrase(attribute.getErrorCode())
				: reasonPhrase);

		return attribute;
	}

	/**
	 * Creates a MappedAddressAttribute of the specified type and with the
	 * specified address and port
	 * 
	 * @param address
	 *            the address value of the address attribute
	 * @return the newly created address attribute.
	 */
	public static MappedAddressAttribute createMappedAddressAttribute(
			TransportAddress address) {
		MappedAddressAttribute attribute = new MappedAddressAttribute();

		attribute.setAddress(address);

		return attribute;
	}

	/**
	 * Creates a ReflectedFromAddressAttribute of the specified type and with
	 * the specified address and port
	 * 
	 * @param address
	 *            the address value of the address attribute
	 * @return the newly created address attribute.
	 */
	public static ReflectedFromAttribute createReflectedFromAttribute(
			TransportAddress address) {
		ReflectedFromAttribute attribute = new ReflectedFromAttribute();

		attribute.setAddress(address);

		return attribute;
	}

	/**
	 * Creates a ResponseFromAddressAttribute of the specified type and with the
	 * specified address and port
	 * 
	 * @param address
	 *            the address value of the address attribute
	 * @return the newly created address attribute.
	 */
	public static ResponseAddressAttribute createResponseAddressAttribute(
			TransportAddress address) {
		ResponseAddressAttribute attribute = new ResponseAddressAttribute();

		attribute.setAddress(address);

		return attribute;
	}

	/**
	 * Creates a SourceFromAddressAttribute of the specified type and with the
	 * specified address and port
	 * 
	 * @param address
	 *            the address value of the address attribute
	 * @return the newly created address attribute.
	 */
	public static SourceAddressAttribute createSourceAddressAttribute(
			TransportAddress address) {
		SourceAddressAttribute attribute = new SourceAddressAttribute();

		attribute.setAddress(address);

		return attribute;
	}

	/**
	 * Creates an empty UnknownAttributesAttribute.
	 * 
	 * @return the newly created UnknownAttributesAttribute
	 */
	public static UnknownAttributesAttribute createUnknownAttributesAttribute() {
		UnknownAttributesAttribute attribute = new UnknownAttributesAttribute();

		return attribute;
	}

	/**
	 * Creates a XorRelayedAddressAttribute of the specified type and with the
	 * specified address and port.
	 * 
	 * @param address
	 *            the address value of the address attribute
	 * @param tranID
	 *            the ID of the transaction that we will be using for the XOR
	 *            mask.
	 * 
	 * @return the newly created address attribute.
	 */
	public static XorRelayedAddressAttribute createXorRelayedAddressAttribute(
			TransportAddress address, byte[] tranID) {
		XorRelayedAddressAttribute attribute = new XorRelayedAddressAttribute();

		// TODO shouldn't we be XORing the address before setting it?
		attribute.setAddress(address, tranID);
		return attribute;
	}

	/**
	 * Creates a XorPeerAddressAttribute of the specified type and with the
	 * specified address and port
	 * 
	 * @param address
	 *            the address value of the address attribute
	 * @param tranID
	 *            the ID of the transaction that we will be using for the XOR
	 *            mask.
	 * @return the newly created address attribute.
	 */
	public static XorPeerAddressAttribute createXorPeerAddressAttribute(
			TransportAddress address, byte[] tranID) {
		XorPeerAddressAttribute attribute = new XorPeerAddressAttribute();

		// TODO shouldn't we be XORing the address before setting it?
		attribute.setAddress(address, tranID);
		return attribute;
	}

	/**
	 * Creates a XorMappedAddressAttribute for the specified <tt>address</tt>.
	 * 
	 * @param address
	 *            the address value of the address attribute
	 * @param tranID
	 *            the ID of the transaction that we will be using for the XOR
	 *            mask.
	 * 
	 * @return the newly created XOR address attribute.
	 */
	public static XorMappedAddressAttribute createXorMappedAddressAttribute(
			TransportAddress address, byte[] tranID) {
		XorMappedAddressAttribute attribute = new XorMappedAddressAttribute();

		attribute.setAddress(address, tranID);

		return attribute;
	}

	/**
	 * Create a UsernameAttribute.
	 * 
	 * @param username
	 *            username value
	 * 
	 * @return newly created UsernameAttribute
	 */
	public static UsernameAttribute createUsernameAttribute(byte username[]) {
		UsernameAttribute attribute = new UsernameAttribute();

		attribute.setUsername(username);
		return attribute;
	}

	/**
	 * Creates a new <tt>UsernameAttribute</tt> instance.
	 * 
	 * @param username
	 *            the String value of the username
	 * @return a new <tt>UsernameAttribute</tt> instance
	 */
	public static UsernameAttribute createUsernameAttribute(String username) {
		UsernameAttribute attribute = new UsernameAttribute();

		try {
			attribute.setUsername(username.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException ueex) {
			throw new UndeclaredThrowableException(ueex);
		}
		return attribute;
	}

	/**
	 * Creates an empty <tt>MessageIntegrityAttribute</tt>. When included in a
	 * message the stack would set the body of this attribute so that the the
	 * HMAC-SHA1 (RFC 2104) would correspond to the actual message that's
	 * transporting the attribute.
	 * 
	 * @param username
	 *            the username that we should use to obtain an encryption key
	 *            (password) that the {@link StunAttribute#encode()} method
	 *            should use when creating the content of this message.
	 * 
	 * @return the newly created address attribute.
	 */
	public static MessageIntegrityAttribute createMessageIntegrityAttribute(String username, byte[] key) {
		MessageIntegrityAttribute attribute = new MessageIntegrityAttribute();
		attribute.setKey(key);
		attribute.setUsername(username);
		return attribute;
	}

	/**
	 * Creates an empty <tt>FingerprintAttribute</tt> with a 0 check sum. Once
	 * included in a message, the value of this attribute will be calculated by
	 * the stack just before sending it.
	 * 
	 * @return the newly created <tt>FingerprintAttribute</tt>.
	 */
	public static FingerprintAttribute createFingerprintAttribute() {
		FingerprintAttribute attribute = new FingerprintAttribute();

		return attribute;
	}

	/**
	 * Create a ChannelNumberAttribute.
	 * 
	 * @param channelNumber
	 *            channel number
	 * @return newly created ChannelNumberAttribute
	 */
	public static ChannelNumberAttribute createChannelNumberAttribute(
			char channelNumber) {
		ChannelNumberAttribute attribute = new ChannelNumberAttribute();

		attribute.setChannelNumber(channelNumber);
		return attribute;
	}

	/**
	 * Create a RealmAttribute.
	 * 
	 * @param realm
	 *            realm value
	 * @return newly created RealmAttribute
	 */
	public static RealmAttribute createRealmAttribute(byte realm[]) {
		RealmAttribute attribute = new RealmAttribute();

		attribute.setRealm(realm);
		return attribute;
	}

	/**
	 * Create a NonceAttribute.
	 * 
	 * @param nonce
	 *            nonce value
	 * @return newly created NonceAttribute
	 */
	public static NonceAttribute createNonceAttribute(byte nonce[]) {
		NonceAttribute attribute = new NonceAttribute();

		attribute.setNonce(nonce);
		return attribute;
	}

	/**
	 * Create a SoftwareAttribute.
	 * 
	 * @param software
	 *            software value
	 * @return newly created SoftwareAttribute
	 */
	public static SoftwareAttribute createSoftwareAttribute(byte software[]) {
		SoftwareAttribute attribute = new SoftwareAttribute();

		attribute.setSoftware(software);
		return attribute;
	}

	/**
	 * Create a EventAttribute.
	 * 
	 * @param rFlag
	 *            R flag
	 * @return the newly created EventPortAttribute
	 */
	public static EvenPortAttribute createEvenPortAttribute(boolean rFlag) {
		EvenPortAttribute attribute = new EvenPortAttribute();

		attribute.setRFlag(rFlag);
		return attribute;
	}

	/**
	 * Create a LifetimeAttribute.
	 * 
	 * @param lifetime
	 *            lifetime value
	 * @return newly created LifetimeAttribute
	 */
	public static LifetimeAttribute createLifetimeAttribute(int lifetime) {
		LifetimeAttribute attribute = new LifetimeAttribute();

		attribute.setLifetime(lifetime);
		return attribute;
	}

	/**
	 * Create a RequestedTransportAttribute.
	 * 
	 * @param protocol
	 *            transport protocol requested
	 * @return newly created RequestedTransportAttribute
	 */
	public static RequestedTransportAttribute createRequestedTransportAttribute(
			byte protocol) {
		RequestedTransportAttribute attribute = new RequestedTransportAttribute();

		attribute.setRequestedTransport(protocol);
		return attribute;
	}

	/**
	 * Create a ReservationTokenAttribute.
	 * 
	 * @param token
	 *            the token
	 * @return newly created RequestedTransportAttribute
	 */
	public static ReservationTokenAttribute createReservationTokenAttribute(
			byte token[]) {
		ReservationTokenAttribute attribute = new ReservationTokenAttribute();

		attribute.setReservationToken(token);
		return attribute;
	}

	/**
	 * Create a DataAtttribute.
	 * 
	 * @param data
	 *            the data
	 * @return newly created DataAttribute
	 */
	public static DataAttribute createDataAttribute(byte data[]) {
		DataAttribute attribute = new DataAttribute();

		attribute.setData(data);
		return attribute;
	}

	/**
	 * Create a DataAtttribute.
	 * 
	 * @param data
	 *            the data
	 * @return newly created DataAttribute
	 */
	public static DataAttribute createDataAttributeWithoutPadding(byte data[]) {
		DataAttribute attribute = new DataAttribute(false);

		attribute.setData(data);
		return attribute;
	}

	/**
	 * Creates an Controlled Attribute object with the specified tie-breaker
	 * value
	 * 
	 * @param tieBreaker
	 *            the tie-breaker value to be used
	 * @return the created IceControlledAttribute
	 */
	public static ControlledAttribute createIceControlledAttribute(
			long tieBreaker) {
		ControlledAttribute attribute = new ControlledAttribute();
		attribute.setTieBreaker(tieBreaker);

		return attribute;
	}

	/**
	 * Creates a Priority attribute with the specified priority value
	 * 
	 * @param priority
	 *            the priority value
	 * @return the created PriorityAttribute
	 * @throws IllegalArgumentException
	 *             if priority < 0 or priority > (2^31 - 1)
	 */
	public static PriorityAttribute createPriorityAttribute(long priority)
			throws IllegalArgumentException {
		PriorityAttribute attribute = new PriorityAttribute();

		attribute.setPriority(priority);

		return attribute;
	}

	/**
	 * Creates a UseCandidateAttribute
	 * 
	 * @return the created UseCandidateAttribute
	 */
	public static UseCandidateAttribute createUseCandidateAttribute() {
		UseCandidateAttribute attribute = new UseCandidateAttribute();

		return attribute;
	}

	/**
	 * Creates an Controlling Attribute with the specified tie-breaker value
	 * 
	 * @param tieBreaker
	 *            the tie-breaker value to be used
	 * 
	 * @return the created IceControllingAttribute
	 */
	public static ControllingAttribute createIceControllingAttribute(
			long tieBreaker) {
		ControllingAttribute attribute = new ControllingAttribute();
		attribute.setTieBreaker(tieBreaker);

		return attribute;
	}

	/**
	 * Creates a MagicCookieAttribute.
	 * 
	 * @return the created MagicCookieAttribute
	 */
	public static MagicCookieAttribute createMagicCookieAttribute() {
		MagicCookieAttribute attribute = new MagicCookieAttribute();
		return attribute;
	}

	/**
	 * Creates a DestinationFromAddressAttribute of the specified type and with
	 * the specified address and port
	 * 
	 * @param address
	 *            the address value of the address attribute
	 * @return the newly created address attribute.
	 */
	public static DestinationAddressAttribute createDestinationAddressAttribute(
			TransportAddress address) {
		DestinationAddressAttribute attribute = new DestinationAddressAttribute();

		attribute.setAddress(address);

		return attribute;
	}

}
