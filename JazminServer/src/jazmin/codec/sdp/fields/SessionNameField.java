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
 * s=[session name]
 * <p>
 * The "s=" field is the textual session name.<br>
 * There MUST be one and only one "s=" field per session description.<br>
 * The "s=" field MUST NOT be empty. If a session has no meaningful name, the
 * value "s= " SHOULD be used (i.e., a single space as the session name).
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class SessionNameField implements SdpField {

	public static final char FIELD_TYPE = 's';
	private static final String BEGIN = "s=";
	private static final int BEGIN_LEN = BEGIN.length();
	
	// Default values
	private static final String DEFAULT_NAME = " ";
	
	private final StringBuilder builder;
	
	private String name;
	
	public SessionNameField(String name) {
		this.builder = new StringBuilder(BEGIN);
		this.name = name;
	}
	
	public SessionNameField() {
		this(DEFAULT_NAME);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public char getFieldType() {
		return FIELD_TYPE;
	}

	@Override
	public String toString() {
		// Clear builder
		this.builder.setLength(BEGIN_LEN);
		this.builder.append(this.name);
		return this.builder.toString();
	}
	
}
