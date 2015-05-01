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

package jazmin.codec.sdp.rtcp.attributes;

import jazmin.codec.sdp.fields.AttributeField;

/**
 * a=rtcp:[port]
 * 
 * <p>
 * The RTCP attribute is used to document the RTCP port used for media stream,
 * when that port is not the next higher (odd) port number following the RTP
 * port described in the media line.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 * @see <a href="https://tools.ietf.org/html/rfc3605">RFC3605</a>
 */
public class RtcpAttribute extends AttributeField {
	
	public static final String ATTRIBUTE_TYPE = "rtcp";
	private static final int DEFAULT_PORT = 0;
	
	private int port;
	private String networkType;
	private String addressType;
	private String address;
	
	public RtcpAttribute() {
		this(DEFAULT_PORT);
	}
	
	public RtcpAttribute(int port) {
		this(port, null, null, null);
	}

	public RtcpAttribute(int port, String networkType, String addressType, String address) {
		super(ATTRIBUTE_TYPE);
		this.port = port;
		this.networkType = networkType;
		this.addressType = addressType;
		this.address = address;
	}

	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
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
	public String toString() {
		super.builder.setLength(0);
		super.builder.append(BEGIN).append(ATTRIBUTE_TYPE).append(ATTRIBUTE_SEPARATOR).append(this.port);
		if (this.networkType != null && !this.networkType.isEmpty()) {
			super.builder.append(" ")
			        .append(this.networkType).append(" ")
					.append(this.addressType).append(" ")
					.append(this.address);
		}
		return super.builder.toString();
	}
	
}
