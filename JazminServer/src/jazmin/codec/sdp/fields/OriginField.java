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

package jazmin.codec.sdp.fields;

import jazmin.codec.sdp.SdpField;

/**
 * o=[username] [sess-id] [sess-version] [nettype] [addrtype] [unicast-address]
 * <p>
 * The "o=" field gives the originator of the session (her username and the
 * address of the user's host) plus a session identifier and version number.
 * </p>
 * <p>
 * In general, the "o=" field serves as a globally unique identifier for this
 * version of this session description, and the subfields excepting the version
 * taken together identify the session irrespective of any modifications.
 * </p>
 * <p>
 * For privacy reasons, it is sometimes desirable to obfuscate the username and
 * IP address of the session originator. If this is a concern, an arbitrary
 * <username> and private <unicast-address> MAY be chosen to populate the "o="
 * field, provided that these are selected in a manner that does not affect the
 * global uniqueness of the field.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class OriginField implements SdpField {

	// Parsing
	public static final char FIELD_TYPE = 'o';
	private static final String BEGIN = "o=";
	private static final int BEGIN_LEN = BEGIN.length();
	
	// Default values
	private static final String DEFAULT_USERNAME = "-";
	private static final String DEFAULT_SESSION_ID = "0";
	private static final String DEFAULT_SESSION_VERSION = "1";
	private static final String DEFAULT_NET_TYPE = "IN";
	private static final String DEFAULT_ADDRESS_TYPE = "IP4";
	private static final String DEFAULT_ADDRESS = "0.0.0.0";
	
	private final StringBuilder builder;
	
	private String username;
	private String sessionId;
	private String sessionVersion;
	private String netType;
	private String addressType;
	private String address;
	
	public OriginField(String username, String sessionId, String sessionVersion, String netType, String addressType, String address) {
		this.builder = new StringBuilder(BEGIN);
		this.username = username;
		this.sessionId = sessionId;
		this.sessionVersion = sessionVersion;
		this.netType = netType;
		this.addressType = addressType;
		this.address = address;
	}
	
	public OriginField(String sessionId, String address) {
		this(DEFAULT_USERNAME, sessionId, DEFAULT_SESSION_VERSION, DEFAULT_NET_TYPE, DEFAULT_ADDRESS_TYPE, address);
	}
	
	public OriginField() {
		this(DEFAULT_SESSION_ID, DEFAULT_ADDRESS);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionVersion() {
		return sessionVersion;
	}

	public void setSessionVersion(String sessionVersion) {
		this.sessionVersion = sessionVersion;
	}

	public String getNetType() {
		return netType;
	}

	public void setNetType(String netType) {
		this.netType = netType;
	}

	public String getAddressType() {
		return addressType;
	}

	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public char getFieldType() {
		return FIELD_TYPE;
	}
	
	@Override
	public String toString() {
		// clear builder
		this.builder.setLength(BEGIN_LEN);
		this.builder.append(this.username).append(" ")
		        .append(this.sessionId).append(" ")
		        .append(this.sessionVersion).append(" ")
		        .append(this.netType).append(" ")
				.append(this.addressType).append(" ")
				.append(this.address);
		return this.builder.toString();
	}
	
}
