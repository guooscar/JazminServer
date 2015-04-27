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

import jazmin.server.sip.io.sdp.fields.AttributeField;

/**
 * Generic {@link AttributeField} that freely allows overriding of its key and
 * value fields.<br>
 * Should be used when parsing unknown types of attributes.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class GenericAttribute extends AttributeField {

	public GenericAttribute(String key) {
		super();
		if(key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key cannot be empty");
		}
		super.key = key;
	}
	
	public GenericAttribute(String key, String value) {
		super();
		if(key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key cannot be empty");
		}
		super.key = key;
		super.value = value;
	}
	
	public void setKey(String key) {
		if(key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key cannot be empty");
		}
		super.key = key;
	}

	public void setValue(String value) {
		super.value = value;
	}

}
