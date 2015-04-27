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
 * a=[attribute]<br>
 * a=[attribute]:[value]
 * <p>
 * Attributes are the primary means for extending SDP.<br>
 * Attributes may be defined to be used as "session-level" attributes,
 * "media-level" attributes, or both.
 * </p>
 * <p>
 * A media description may have any number of attributes ("a=" fields) that are
 * media specific. These are referred to as "media-level" attributes and add
 * information about the media stream. Attribute fields can also be added before
 * the first media field; these "session-level" attributes convey additional
 * information that applies to the conference as a whole rather than to
 * individual media.
 * </p>
 * <p>
 * Attribute fields may be of two forms:
 * 
 * <ul>
 * <li>A property attribute is simply of the form "a=<flag>". These are binary
 * attributes, and the presence of the attribute conveys that the attribute is a
 * property of the session. An example might be "a=recvonly".
 * 
 * <li>A value attribute is of the form "a=<attribute>:<value>". For example, a
 * whiteboard could have the value attribute "a=orient: landscape"
 * </ul>
 * </p>
 * 
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class AttributeField implements SdpField {

	// text parsing
	public static final String ATTRIBUTE_SEPARATOR = ":";
	
	public static final char FIELD_TYPE = 'a';
	protected static final String BEGIN = "a=";
	protected static final int BEGIN_LENGTH = BEGIN.length();
	
	protected final StringBuilder builder;
	
	protected String key;
	protected String value;
	
	protected AttributeField() {
		this.builder = new StringBuilder(BEGIN);
	}
	
	protected AttributeField(String key, String value) {
		this();
		this.key = key;
		this.value = value;
	}
	
	protected AttributeField(String key) {
		this(key, null);
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public char getFieldType() {
		return FIELD_TYPE;
	}
	
	@Override
	public String toString() {
		// Clean String Builder
		this.builder.setLength(BEGIN_LENGTH);
		this.builder.append(this.key);
		if(this.value != null && !this.value.isEmpty()) {
			this.builder.append(ATTRIBUTE_SEPARATOR).append(this.value);
		}
		return this.builder.toString();
	}
	
}
