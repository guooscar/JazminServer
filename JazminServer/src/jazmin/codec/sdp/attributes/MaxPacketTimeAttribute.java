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
 * a=maxptime:[maximum packet time]
 * 
 * <p>
 * This gives the maximum amount of media that can be encapsulated in each
 * packet, expressed as time in milliseconds.<br>
 * The time SHALL be calculated as the sum of the time the media present in the
 * packet represents. For frame-based codecs, the time SHOULD be an integer
 * multiple of the frame size. This attribute is probably only meaningful for
 * audio data, but may be used with other media types if it makes sense. It is a
 * media-level attribute, and it is not dependent on charset.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class MaxPacketTimeAttribute extends AttributeField {
	
	public static final String ATTRIBUTE_TYPE = "maxptime";
	private static final int DEFAULT_TIME = 0;

	private int time;
	
	public MaxPacketTimeAttribute() {
		this(DEFAULT_TIME);
	}
	
	public MaxPacketTimeAttribute(int time) {
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