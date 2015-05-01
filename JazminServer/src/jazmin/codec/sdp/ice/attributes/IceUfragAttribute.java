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
 * a=ice-ufrag:[value]
 * 
 * <p>
 * The "ice-pwd" and "ice-ufrag" attributes can appear at either the
 * session-level or media-level. When present in both, the value in the
 * media-level takes precedence. Thus, the value at the session-level is
 * effectively a default that applies to all media streams, unless overridden by
 * a media-level value. Whether present at the session or media-level, there
 * MUST be an ice-pwd and ice-ufrag attribute for each media stream. If two
 * media streams have identical ice-ufrag's, they MUST have identical ice-pwd's.
 * 
 * The ice-ufrag and ice-pwd attributes MUST be chosen randomly at the beginning
 * of a session. The ice-ufrag attribute MUST contain at least 24 bits of
 * randomness, and the ice-pwd attribute MUST contain at least 128 bits of
 * randomness. This means that the ice-ufrag attribute will be at least 4
 * characters long, and the ice-pwd at least 22 characters long, since the
 * grammar for these attributes allows for 6 bits of randomness per character.
 * The attributes MAY be longer than 4 and 22 characters, respectively, of
 * course, up to 256 characters. The upper limit allows for buffer sizing in
 * implementations. Its large upper limit allows for increased amounts of
 * randomness to be added over time.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class IceUfragAttribute extends AttributeField {

	public static final String ATTRIBUTE_TYPE = "ice-ufrag";
	
	private String ufrag;

	public IceUfragAttribute() {
		this(null);
	}
	
	public IceUfragAttribute(String ufrag) {
		super(ATTRIBUTE_TYPE);
		this.ufrag = ufrag;
	}
	
	public void setUfrag(String ufrag) {
		this.ufrag = ufrag;
	}
	
	public String getUfrag() {
		return ufrag;
	}
	
	@Override
	public String toString() {
		super.builder.setLength(0);
		super.builder.append(BEGIN).append(ATTRIBUTE_TYPE).append(ATTRIBUTE_SEPARATOR).append(this.ufrag);
		return super.builder.toString();
	}

}
