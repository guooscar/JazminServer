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

package jazmin.codec.sdp.ice.attributes;

import jazmin.codec.sdp.fields.AttributeField;

/**
 * a=ice-lite
 * 
 * <p>
 * Session-level attribute only, and indicates that an agent is a lite
 * implementation.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5245#section-15.3">RFC5245</a>
 */
public class IceLiteAttribute extends AttributeField {

	public static final String ATTRIBUTE_TYPE = "ice-lite";

	public IceLiteAttribute() {
		super(ATTRIBUTE_TYPE);
	}
	
	@Override
	public String toString() {
		super.builder.setLength(0);
		super.builder.append(BEGIN).append(ATTRIBUTE_TYPE);
		return super.builder.toString();
	}

}