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
 * a=fmtp:[format] [format specific parameters]<br>
 * 
 * <p>
 * This attribute allows parameters that are specific to a particular format to
 * be conveyed in a way that SDP does not have to understand them.<br>
 * The format must be one of the formats specified for the media.
 * Format-specific parameters may be any set of parameters required to be
 * conveyed by SDP and given unchanged to the media tool that will use this
 * format. At most one instance of this attribute is allowed for each format.<br>
 * It is a media-level attribute, and it is not dependent on charset.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class FormatParameterAttribute extends AttributeField {
	
	public static final String ATTRIBUTE_TYPE = "fmtp";
	private static final short DEFAULT_FORMAT = -1; 

	private int format;
	private String[] params;
	
	public FormatParameterAttribute(int format, String[] params) {
		super(ATTRIBUTE_TYPE);
		this.format = format;
		this.params = params;
	}

	public FormatParameterAttribute() {
		this(DEFAULT_FORMAT, null);
	}
	
	public int getFormat() {
		return format;
	}
	
	public void setFormat(int format) {
		this.format = format;
	}
	
	public String[] getParams() {
		return params;
	}
	
	public void setParams(String[] params) {
		this.params = params;
	}
	
	@Override
	public String toString() {
		super.builder.setLength(0);
		super.builder.append(BEGIN).append(ATTRIBUTE_TYPE).append(ATTRIBUTE_SEPARATOR);
		super.builder.append(this.format).append(" ");
		for(String p:params){
			super.builder.append(p).append(" ");
		}
		super.builder.deleteCharAt(builder.length()-1);
		return super.builder.toString();
	}

}
