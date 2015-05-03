/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes.general;

import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jazmin.codec.stun.StunException;
import jazmin.codec.stun.messages.attributes.StunAttribute;

/**
 * The MESSAGE-INTEGRITY attribute contains an HMAC-SHA1 [RFC2104] of the STUN
 * message. The MESSAGE-INTEGRITY attribute can be present in any STUN message
 * type. Since it uses the SHA1 hash, the HMAC will be 20 bytes. The text used
 * as input to HMAC is the STUN message, including the header, up to and
 * including the attribute preceding the MESSAGE-INTEGRITY attribute. With the
 * exception of the FINGERPRINT attribute, which appears after
 * MESSAGE-INTEGRITY, agents MUST ignore all other attributes that follow
 * MESSAGE-INTEGRITY. The key for the HMAC depends on whether long-term or
 * short-term credentials are in use. For long-term credentials, the key is 16
 * bytes:
 * <p>
 * 
 * <pre>
 *          key = MD5(username ":" realm ":" SASLprep(password))
 * </pre>
 * 
 * That is, the 16-byte key is formed by taking the MD5 hash of the result of
 * concatenating the following five fields: (1) the username, with any quotes
 * and trailing nulls removed, as taken from the USERNAME attribute (in which
 * case SASLprep has already been applied); (2) a single colon; (3) the realm,
 * with any quotes and trailing nulls removed; (4) a single colon; and (5) the
 * password, with any trailing nulls removed and after processing using
 * SASLprep. For example, if the username was 'user', the realm was 'realm', and
 * the password was 'pass', then the 16-byte HMAC key would be the result of
 * performing an MD5 hash on the string 'user:realm:pass', the resulting hash
 * being 0x8493fbc53ba582fb4c044c456bdc40eb.
 * <p>
 * For short-term credentials:
 * <p>
 * 
 * <pre>
 * key = SASLprep(password)
 * </pre>
 * 
 * where MD5 is defined in RFC 1321 [RFC1321] and SASLprep() is defined in RFC
 * 4013 [RFC4013].
 * <p>
 * The structure of the key when used with long-term credentials facilitates
 * deployment in systems that also utilize SIP. Typically, SIP systems utilizing
 * SIP's digest authentication mechanism do not actually store the password in
 * the database. Rather, they store a value called H(A1), which is equal to the
 * key defined above.
 * <p>
 * Based on the rules above, the hash used to construct MESSAGE- INTEGRITY
 * includes the length field from the STUN message header. Prior to performing
 * the hash, the MESSAGE-INTEGRITY attribute MUST be inserted into the message
 * (with dummy content). The length MUST then be set to point to the length of
 * the message up to, and including, the MESSAGE-INTEGRITY attribute itself, but
 * excluding any attributes after it. Once the computation is performed, the
 * value of the MESSAGE-INTEGRITY attribute can be filled in, and the value of
 * the length in the STUN header can be set to its correct value -- the length
 * of the entire message. Similarly, when validating the MESSAGE-INTEGRITY, the
 * length field should be adjusted to point to the end of the MESSAGE-INTEGRITY
 * attribute prior to calculating the HMAC. Such adjustment is necessary when
 * attributes, such as FINGERPRINT, appear after MESSAGE-INTEGRITY.
 */
public class MessageIntegrityAttribute extends StunAttribute implements ContextDependentAttribute {

	public static final String NAME = "MESSAGE_INTEGRITY";
	public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	public static final char DATA_LENGTH = 20;

	private byte[] hmacSha1Content;
	private String username;
	private String media;
	/**
	 * If the attribute belongs to a Request Message, then its the remote user
	 * key.<br>
	 * If belongs to a Response Message, then its the local user key.
	 */
	byte[] key;

	public MessageIntegrityAttribute() {
		super(StunAttribute.MESSAGE_INTEGRITY);
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public byte[] getHmacSha1Content() {
		return hmacSha1Content;
	}

	public void setHmacSha1Content(byte[] hmacSha1Content) {
		this.hmacSha1Content = hmacSha1Content;
	}

	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username that we should use to obtain an encryption key
	 * (password) that the {@link #encode()} method should use when creating the
	 * content of this message.
	 * 
	 * @param username
	 *            the username that we should use to obtain an encryption key
	 *            (password) that the {@link #encode()} method should use when
	 *            creating the content of this message.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	public String getMedia() {
		return media;
	}

	/**
	 * Sets the media name that we should use to get the corresponding remote
	 * key (short-term authentication only).
	 * 
	 * @param media
	 *            name
	 */
	public void setMedia(String media) {
		this.media = media;
	}

	/**
	 * Encodes <tt>message</tt> using <tt>key</tt> and the HMAC-SHA1 algorithm
	 * as per RFC 2104 and returns the resulting byte array. This is a utility
	 * method that generates content for the {@link MessageIntegrityAttribute}
	 * regardless of the credentials being used (short or long term).
	 * 
	 * @param message
	 *            the STUN message that the resulting content will need to
	 *            travel in.
	 * @param offset
	 *            the index where data starts in <tt>message</tt>.
	 * @param length
	 *            the length of the data in <tt>message</tt> that the method
	 *            should consider.
	 * @param key
	 *            the key that we should be using for the encoding (which
	 *            depends on whether we are using short or long term
	 *            credentials).
	 * 
	 * @return the HMAC that should be used in a
	 *         <tt>MessageIntegrityAttribute</tt> transported by
	 *         <tt>message</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the encoding fails for some reason.
	 */
	public static byte[] calculateHmacSha1(byte[] message, int offset, int length, byte[] key) throws IllegalArgumentException {
		try {
			// get an HMAC-SHA1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA1_ALGORITHM);

			// get an HMAC-SHA1 Mac instance and initialize it with the key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] macInput = new byte[length];
			System.arraycopy(message, offset, macInput, 0, length);
			return mac.doFinal(macInput);
		} catch (Exception exc) {
			throw new IllegalArgumentException("Could not create HMAC-SHA1 request encoding", exc);
		}
	}

	public byte[] encode(byte[] data, int offset, int length) {
		char type = getAttributeType();
		byte binValue[] = new byte[HEADER_LENGTH + getDataLength()];

		// Type
		binValue[0] = (byte) (type >> 8);
		binValue[1] = (byte) (type & 0x00FF);

		// Length
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);

		char msgType = (char) ((data[0] << 8) + data[1]);

		// now calculate the HMAC-SHA1
		this.hmacSha1Content = calculateHmacSha1(data, offset, length, this.key);

		// username
		System.arraycopy(hmacSha1Content, 0, binValue, 4, getDataLength());
		return binValue;
	}

	@Override
	public char getDataLength() {
		return DATA_LENGTH;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof MessageIntegrityAttribute)) {
			return false;
		}
		
		if (other == this) {
			return true;
		}
		
		MessageIntegrityAttribute att = (MessageIntegrityAttribute) other;
		if (att.getAttributeType() != getAttributeType() || att.getDataLength() != getDataLength() || !Arrays.equals(att.hmacSha1Content, hmacSha1Content)) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] encode() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ContentDependentAttributes should be encoded through the contend-dependent encode method");
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length) throws StunException {
		this.hmacSha1Content = new byte[length];
		System.arraycopy(data, offset, this.hmacSha1Content, 0, length);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(NAME).append(": ");
		builder.append("username=").append(this.username).append(", ");
		builder.append("key=").append(this.key);
		return builder.toString();
	}

}
