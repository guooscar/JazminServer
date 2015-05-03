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

package jazmin.codec.stun;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * A combination of IP address and port for a particular transport protocol.
 * 
 * @author Henrique Rosa
 * 
 */
public class TransportAddress extends InetSocketAddress {

	private static final long serialVersionUID = -4227068181007895183L;

	public enum TransportProtocol {
		UDP("udp");
		
		private final String description;
		
		private TransportProtocol(String description) {
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}
		
	}

	protected TransportProtocol protocol;
	protected boolean virtual;

	public TransportAddress(InetAddress addr, int port,
			TransportProtocol protocol) {
		super(addr, port);
		this.protocol = protocol;
		this.virtual = false;
	}

	public TransportAddress(String hostname, int port,
			TransportProtocol protocol) {
		super(hostname, port);
		this.protocol = protocol;
		this.virtual = false;
	}

	/**
	 * Verifies whether the address bound to this candidate is IPv6.
	 * 
	 * @return<code>true</code> if address is IPv6. Returns <code>false</code>
	 *                          otherwise.
	 */
	public boolean isIPv6() {
		return getAddress() instanceof Inet6Address;
	}

	/**
	 * Verifies whether the address bound to this candidate is IPv4.
	 * 
	 * @return<code>true</code> if address is IPv4. Returns <code>false</code>
	 *                          otherwise.
	 */
	public boolean isIPv4() {
		return getAddress() instanceof Inet4Address;
	}

	/**
	 * Gets the protocol to be used for transportation means.
	 * 
	 * @return The transport protocol
	 */
	public TransportProtocol getProtocol() {
		return protocol;
	}

	public boolean isVirtual() {
		return this.virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	public byte[] getAddressBytes() {
		return this.getAddress().getAddress();
	}

}
