/*
 * 
 * Code derived and adapted from the Jitsi client side STUN framework.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package jazmin.codec.stun.messages.attributes.general;

import jazmin.codec.stun.StunException;
import jazmin.codec.stun.messages.attributes.StunAttribute;

/**
 * The ERROR-CODE attribute is present in the Binding Error Response and
 * Shared Secret Error Response.  It is a numeric value in the range of
 * 100 to 699 plus a textual reason phrase encoded in UTF-8, and is
 * consistent in its code assignments and semantics with SIP [10] and
 * HTTP [15].  The reason phrase is meant for user consumption, and can
 * be anything appropriate for the response code.  The lengths of the
 * reason phrases MUST be a multiple of 4 (measured in bytes).  This can
 * be accomplished by added spaces to the end of the text, if necessary.
 * Recommended reason phrases for the defined response codes are
 * presented below.
 *
 * To facilitate processing, the class of the error code (the hundreds
 * digit) is encoded separately from the rest of the code.
 *
 *   0                   1                   2                   3
 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |                   0                     |Class|     Number    |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |      Reason Phrase (variable)                                ..
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * The class represents the hundreds digit of the response code.  The
 * value MUST be between 1 and 6.  The number represents the response
 * code modulo 100, and its value MUST be between 0 and 99.
 *
 * The following response codes, along with their recommended reason
 * phrases (in brackets) are defined at this time:
 *
 * 400 (Bad Request): The request was malformed.  The client should not
 *      retry the request without modification from the previous
 *      attempt.
 *
 * 401 (Unauthorized): The Binding Request did not contain a MESSAGE-
 *      INTEGRITY attribute.
 *
 * 420 (Unknown Attribute): The server did not understand a mandatory
 *      attribute in the request.
 *
 * 430 (Stale Credentials): The Binding Request did contain a MESSAGE-
 *      INTEGRITY attribute, but it used a shared secret that has
 *      expired.  The client should obtain a new shared secret and try
 *      again.
 *
 * 431 (Integrity Check Failure): The Binding Request contained a
 *      MESSAGE-INTEGRITY attribute, but the HMAC failed verification.
 *      This could be a sign of a potential attack, or client
 *      implementation error.
 *
 * 432 (Missing Username): The Binding Request contained a MESSAGE-
 *      INTEGRITY attribute, but not a USERNAME attribute.  Both must be
 *      present for integrity checks.
 *
 * 433 (Use TLS): The Shared Secret request has to be sent over TLS, but
 *      was not received over TLS.
 *
 * 500 (Server Error): The server has suffered a temporary error. The
 *      client should try again.
 *
 * 600 (Global Failure:) The server is refusing to fulfill the request.
 *      The client should not retry.
 */
public class ErrorCodeAttribute extends StunAttribute {

	public static final String NAME = "ERROR-CODE";

	// Common error codes
	public static final char BAD_REQUEST = 400;
	public static final char UNAUTHORIZED = 401;
	public static final char UNKNOWN_ATTRIBUTE = 420;
	public static final char STALE_CREDENTIALS = 430;
	public static final char INTEGRITY_CHECK_FAILURE = 431;
	public static final char MISSING_USERNAME = 432;
	public static final char USE_TLS = 433;
	public static final char ROLE_CONFLICT = 487;
	public static final char SERVER_ERROR = 500;
	public static final char GLOBAL_FAILURE = 600;

	private static final int ERROR_CODE_DIGITS = 4;

	private byte errorClass;
	private byte errorNumber;
	private byte[] reasonPhrase;

	public ErrorCodeAttribute() {
		super(StunAttribute.ERROR_CODE);
		this.errorClass = 0;
		this.errorNumber = 0;
		this.reasonPhrase = null;
	}

	public byte getErrorClass() {
		return errorClass;
	}

	/**
	 * Sets the class of the error.
	 * 
	 * @param errorClass
	 *            The error class.
	 * @throws IllegalArgumentException
	 *             Only error classes between 0 and 99 are valid.
	 */
	public void setErrorClass(byte errorClass) throws IllegalArgumentException {
		if (errorClass < 0 || errorClass > 99) {
			throw new IllegalArgumentException(
					errorClass
							+ "Only error classes between 0 and 99 are valid. Current class: "
							+ errorClass);
		}
		this.errorClass = errorClass;
	}

	public byte getErrorNumber() {
		return errorNumber;
	}

	public void setErrorNumber(byte errorNumber) {
		this.errorNumber = errorNumber;
	}

	/**
	 * A convenience method that sets error class and number according to the
	 * specified error code.
	 * <p>
	 * The class represents the hundreds digit of the error code.<br>
	 * The value MUST be between 1 and 6.<br>
	 * The number represents the response code modulo 100, and its value MUST be
	 * between 0 and 99.
	 * </p>
	 * 
	 * @param errorCode
	 *            the errorCode that this class encapsulates.
	 */
	public void setErrorCode(char code) {
		setErrorClass((byte) (code / 100));
		setErrorNumber((byte) (code % 100));
	}

	/**
	 * A convenience method that constructs an error code from this attribute's
	 * class and number.
	 * 
	 * @return the code of the error this attribute represents.
	 */
	public char getErrorCode() {
		return (char) (getErrorClass() * 100 + getErrorNumber());
	}

	/**
	 * Returns a default reason phrase corresponding to the specified error
	 * code, as described by rfc 3489.
	 * 
	 * @param errorCode
	 *            the code of the error that the reason phrase must describe.
	 * @return a default reason phrase corresponding to the specified error
	 *         code, as described by rfc 3489.
	 */
	public static String getDefaultReasonPhrase(char errorCode) {
		switch (errorCode) {
		case BAD_REQUEST:
			return "(Bad Request): The request was malformed.  The client should not "
					+ "retry the request without modification from the previous attempt.";
		case UNAUTHORIZED:
			return "(Unauthorized): The Binding Request did not contain a MESSAGE-"
					+ "INTEGRITY attribute.";
		case UNKNOWN_ATTRIBUTE:
			return "(Unknown Attribute): The server did not understand a mandatory "
					+ "attribute in the request.";
		case STALE_CREDENTIALS:
			return "(Stale Credentials): The Binding Request did contain a MESSAGE-"
					+ "INTEGRITY attribute, but it used a shared secret that has "
					+ "expired.  The client should obtain a new shared secret and try"
					+ "again";
		case INTEGRITY_CHECK_FAILURE:
			return "(Integrity Check Failure): The Binding Request contained a "
					+ "MESSAGE-INTEGRITY attribute, but the HMAC failed verification. "
					+ "This could be a sign of a potential attack, or client "
					+ "implementation error.";
		case MISSING_USERNAME:
			return "(Missing Username): The Binding Request contained a MESSAGE-"
					+ "INTEGRITY attribute, but not a USERNAME attribute.  Both must be"
					+ "present for integrity checks.";
		case USE_TLS:
			return "(Use TLS): The Shared Secret request has to be sent over TLS, but"
					+ "was not received over TLS.";
		case SERVER_ERROR:
			return "(Server Error): The server has suffered a temporary error. The"
					+ "client should try again.";
		case GLOBAL_FAILURE:
			return "(Global Failure:) The server is refusing to fulfill the request."
					+ "The client should not retry.";
		default:
			return "Unknown Error";
		}
	}

	/**
	 * Set's a reason phrase. The reason phrase is meant for user consumption,
	 * and can be anything appropriate for the response code. The lengths of the
	 * reason phrases MUST be a multiple of 4 (measured in bytes).
	 * 
	 * @param reasonPhrase
	 *            a reason phrase that describes this error.
	 */
	public void setReasonPhrase(String reason) {
		this.reasonPhrase = reason.getBytes();
	}

	/**
	 * Returns the reason phrase. The reason phrase is meant for user
	 * consumption, and can be anything appropriate for the response code. The
	 * lengths of the reason phrases MUST be a multiple of 4 (measured in
	 * bytes).
	 * 
	 * @return reasonPhrase a reason phrase that describes this error.
	 */
	public byte[] getReasonPhrase() {
		return reasonPhrase;
	}

	@Override
	public char getDataLength() {
		char len = (char) (ERROR_CODE_DIGITS + (char) (reasonPhrase == null ? 0
				: reasonPhrase.length));
		return len;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof ErrorCodeAttribute)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		ErrorCodeAttribute att = (ErrorCodeAttribute) other;
		if (att.getAttributeType() != this.getAttributeType()
				|| att.getDataLength() != this.getDataLength()
				|| att.getErrorClass() != this.getErrorClass()
				|| att.getErrorNumber() != this.getErrorNumber()
				|| (att.getReasonPhrase() != null && !att.getReasonPhrase()
						.equals(this.getReasonPhrase()))) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] encode() {
		// length with padding
		byte binValue[] = new byte[HEADER_LENGTH + getDataLength()
				+ (4 - getDataLength() % 4) % 4];
		// Type
		binValue[0] = (byte) (getAttributeType() >> 8);
		binValue[1] = (byte) (getAttributeType() & 0x00FF);
		// Length
		binValue[2] = (byte) (getDataLength() >> 8);
		binValue[3] = (byte) (getDataLength() & 0x00FF);
		// Not used
		binValue[4] = 0x00;
		binValue[5] = 0x00;
		// Error code
		binValue[6] = getErrorClass();
		binValue[7] = getErrorNumber();
		if (reasonPhrase != null) {
			System.arraycopy(reasonPhrase, 0, binValue, 8, reasonPhrase.length);
		}
		return binValue;
	}

	@Override
	protected void decodeAttributeBody(byte[] data, char offset, char length)
			throws StunException {
		// skip the zeros
		offset += 2;
		// Error code
		setErrorClass(data[offset++]);
		setErrorNumber(data[offset++]);
		// Reason Phrase
		byte[] reasonBytes = new byte[length - 4];
		System.arraycopy(data, offset, reasonBytes, 0, reasonBytes.length);
		setReasonPhrase(new String(reasonBytes));
	}

}
