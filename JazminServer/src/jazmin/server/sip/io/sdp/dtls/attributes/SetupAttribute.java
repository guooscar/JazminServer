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

package jazmin.server.sip.io.sdp.dtls.attributes;

import jazmin.server.sip.io.sdp.fields.AttributeField;

/**
 * a=setup:[active|passive|actpass|holdconn]
 * 
 * <p>
 * The 'setup' attribute indicates which of the end points should initiate the
 * connection establishment.<br>
 * It is charset-independent and can be a session-level or a media-level
 * attribute.
 * </p>
 * 
 * <p>
 * Possible Values:
 * 
 * <ul>
 * <li><b>active</b> - The endpoint will initiate an outgoing connection.</li>
 * <li><b>passive</b> - The endpoint will accept an incoming connection.</li>
 * <li><b>actpass</b> - The endpoint is willing to accept an incoming connection
 * or to initiate an outgoing connection.</li>
 * <li><b>holdconn</b> - The endpoint does not want the connection to be
 * established for the time being.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * This parameter was initialing defined in RFC4145, which has been updated by
 * RFC4572.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 * @see <a href="http://tools.ietf.org/html/rfc4145">RFC4145</a>, <a
 *      href="http://tools.ietf.org/html/rfc4572">RFC4572</a>
 */
public class SetupAttribute extends AttributeField {
	
	public static final String ATTRIBUTE_TYPE = "setup";
	
	public static final String ACTIVE = "active";
	public static final String PASSIVE = "passive";
	public static final String ACTPASS = "actpass";
	public static final String HOLDCON = "holdconn";
	
	public SetupAttribute(String value) {
		super(ATTRIBUTE_TYPE);
		setValue(value);
	}
	
	public void setValue(String value) {
		if(!isSetupValid(value)) {
			throw new IllegalArgumentException("Invalid setup: " + value);
		}
		super.value = value;
	}
	
	public static boolean isSetupValid(String value) {
		if(value == null || value.isEmpty()) {
			return false;
		}
		return ACTIVE.equals(value) ||
				PASSIVE.equals(value) ||
				ACTPASS.equals(value) ||
				HOLDCON.equals(value);
	}

}
