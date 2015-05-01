/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package jazmin.codec.sdp.dtls.attributes;

import jazmin.codec.sdp.fields.AttributeField;

/**
 * a=fingerprint:[hash-function][fingerprint]<br>
 * 
 * <p>
 * Example:<br>
 * a=fingerprint:sha-256 D1:2C:BE:AD:C4:F6:64:5C:25:16:11:9C:AF:E7:0F:73:79:36:
 * 4E:9C:1E:15:54:39:0C:06:8B:ED:96:86:00:39
 * </p>
 * 
 * <p>
 * The fingerprint is the result of a hash function of the certificates used in
 * the DTLS-SRTP negotiation. This line creates a binding between the signaling
 * (which is supposed to be trusted) and the certificates used in DTLS, if the
 * fingerprint doesn’t match, then the session should be rejected.
 * </p>
 * 
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 * @see <a href="https://tools.ietf.org/html/rfc4572#section-5">RFC4572</a>
 */
public class FingerprintAttribute extends AttributeField {

	public static final String ATTRIBUTE_TYPE = "fingerprint";

	private String hashFunction;
	private String fingerprint;

	public FingerprintAttribute() {
		this(null, null);
	}

	public FingerprintAttribute(String hashFunction, String fingerprint) {
		super(ATTRIBUTE_TYPE);
		this.hashFunction = hashFunction;
		this.fingerprint = fingerprint;
	}

	public String getHashFunction() {
		return hashFunction;
	}

	public void setHashFunction(String hashFunction) {
		this.hashFunction = hashFunction;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	@Override
	public String toString() {
		super.builder.setLength(0);
		super.builder.append(BEGIN).append(ATTRIBUTE_TYPE).append(ATTRIBUTE_SEPARATOR);
		super.builder.append(this.hashFunction).append(" ").append(this.fingerprint);
		return super.builder.toString();
	}

}
