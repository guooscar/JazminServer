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

package jazmin.server.sip.io.sdp.fields;

import jazmin.server.sip.io.sdp.SdpField;

/**
 * v=[version number]
 * <p>
 * The "v=" field gives the version of the Session Description Protocol.<br>
 * This implementation defines version 0 as written on rfc4566. There is no
 * minor version number.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class VersionField implements SdpField {

	public static final char FIELD_TYPE = 'v';
	public static final String BEGIN = "v=";
	private static final int BEGIN_LENGTH = BEGIN.length();

	private static final short DEFAULT_VERSION = 0;
	
	private final StringBuilder builder;

	private short version;

	public VersionField() {
		this(DEFAULT_VERSION);
	}

	public VersionField(short version) {
		this.builder = new StringBuilder(BEGIN);
		this.version = version;
	}

	@Override
	public char getFieldType() {
		return FIELD_TYPE;
	}

	public short getVersion() {
		return version;
	}

	public void setVersion(short version) {
		this.version = version;
	}

	@Override
	public String toString() {
		// Clean builder
		this.builder.setLength(BEGIN_LENGTH);
		this.builder.append(this.version);
		return this.builder.toString();
	}

}
