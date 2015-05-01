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

package jazmin.codec.sdp.attributes;

import jazmin.codec.sdp.fields.AttributeField;

/**
 * a=ptime:[packet time]
 * 
 * <p>
 * This attribute gives the length of time in milliseconds represented by the
 * media in a packet.<br>
 * This is probably only meaningful for audio data, but may be used with other
 * media types if it makes sense. It should not be necessary to know ptime to
 * decode RTP or vat audio, and it is intended as a recommendation for the
 * encoding/packetisation of audio. It is a media-level attribute, and it is not
 * dependent on charset.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class PacketTimeAttribute extends AttributeField {
	
	public static final String ATTRIBUTE_TYPE = "ptime";
	private static final int DEFAULT_TIME = 0;
	
	private int time;

	public PacketTimeAttribute() {
		this(DEFAULT_TIME);
	}

	public PacketTimeAttribute(int time) {
		super(ATTRIBUTE_TYPE);
		this.time = time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	public int getTime() {
		return time;
	}
	
	@Override
	public String toString() {
		super.builder.setLength(0);
		super.builder.append(BEGIN).append(ATTRIBUTE_TYPE).append(ATTRIBUTE_SEPARATOR).append(this.time);
		return super.builder.toString();
	}

}
