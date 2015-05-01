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
 * c=[nettype] [addrtype] [connection-address]
 * 
 * <p>
 * The "c=" field contains connection data.<br>
 * A session description MUST contain either at least one "c=" field in each
 * media description or a single "c=" field at the session level. It MAY contain
 * a single session-level "c=" field and additional "c=" field(s) per media
 * description, in which case the per-media values override the session-level
 * settings for the respective media.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class ConnectionField implements SdpField {

	// Parsing
	public static final char FIELD_TYPE = 'c';
	protected static final String BEGIN = "c=";
	private static final int BEGIN_LENGTH = BEGIN.length();
	
	// Default values
	private static final String DEFAULT_NET_TYPE = "IN";
	private static final String DEFAULT_ADDRESS_TYPE = "IP4"; 
	private static final String DEFAULT_ADDRESS = "0.0.0.0";
	
	private final StringBuilder builder;
	
	private String networkType;
	private String addressType;
	private String address;
	
	public ConnectionField() {
		this(DEFAULT_NET_TYPE, DEFAULT_ADDRESS_TYPE, DEFAULT_ADDRESS);
	}
	
	public ConnectionField(String netType, String addressType, String address) {
		this.builder = new StringBuilder(BEGIN);
		this.networkType = netType;
		this.addressType = addressType;
		this.address = address;
	}
	
	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String netType) {
		this.networkType = netType;
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
		// Clean builder
		this.builder.setLength(BEGIN_LENGTH);
		this.builder.append(this.networkType).append(" ")
				.append(this.addressType).append(" ")
				.append(this.address);
		return this.builder.toString();
	}

}
