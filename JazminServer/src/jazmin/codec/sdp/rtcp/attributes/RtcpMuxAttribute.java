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

package jazmin.codec.sdp.rtcp.attributes;

import jazmin.codec.sdp.fields.AttributeField;

/**
 * a=rtcp-mux
 * 
 * <p>
 * SDP attribute which indicates the desire to multiplex RTP and RTCP onto a
 * single port.<br>
 * The initial SDP offer MUST include this attribute at the media level to
 * request multiplexing of RTP and RTCP on a single port.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5761#section-5.1.1">RFC5761</a>
 */
public class RtcpMuxAttribute extends AttributeField {

	public static final String ATTRIBUTE_TYPE = "rtcp-mux";
	private static final String SDP = "a=rtcp-mux";

	public RtcpMuxAttribute() {
		super(ATTRIBUTE_TYPE);
	}
	
	@Override
	public String toString() {
		return SDP;
	}
	
}
