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
 * a=recvonly
 * 
 * <p>
 * This specifies that the tools should be started in receive-only mode where
 * applicable.
 * </p>
 * <p>
 * It can be either a session- or media- level attribute, and it is not
 * dependent on charset. Note that recvonly applies to the media only, not to
 * any associated control protocol (e.g., an RTP-based system in recvonly mode
 * SHOULD still send RTCP packets).
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class RecvOnlyAttribute extends AbstractConnectionModeAttribute {
	
	public static final String ATTRIBUTE_TYPE = "recvonly";
	private static final String FULL = "a=recvonly";
	
	public RecvOnlyAttribute() {
		super(ATTRIBUTE_TYPE);
	}

	@Override
	public String toString() {
		return FULL;
	}
}
