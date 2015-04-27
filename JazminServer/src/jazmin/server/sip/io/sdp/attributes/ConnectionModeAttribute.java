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

package jazmin.server.sip.io.sdp.attributes;


/**
 * a=[inactive|recvonly|sendonly|sendrecv]
 * 
 * <p>
 * Reusable connection mode attribute that allows dynamic overriding of the
 * Connection Mode.
 * </p>
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 * @see {@link InactiveAttribute} {@link RecvOnlyAttribute} {@link SendOnlyAttribute} {@link SendRecvAttribute} 
 */
public class ConnectionModeAttribute extends AbstractConnectionModeAttribute {
	
	public static final String SENDONLY = "sendonly";
	public static final String RECVONLY = "recvonly";
	public static final String SENDRECV = "sendrecv";
	public static final String INACTIVE = "inactive";

	public ConnectionModeAttribute() {
		this(SENDRECV);
	}

	public ConnectionModeAttribute(String mode) {
		super(mode);
	}

	public void setMode(String mode) {
		super.key = mode;
	}
	
}
