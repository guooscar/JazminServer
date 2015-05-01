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

package jazmin.codec.sdp.rtcp.attributes.parser;

import java.util.regex.Pattern;

import jazmin.codec.sdp.SdpException;
import jazmin.codec.sdp.SdpParser;
import jazmin.codec.sdp.fields.AttributeField;
import jazmin.codec.sdp.rtcp.attributes.RtcpAttribute;

/**
 * Parses SDP text to construct {@link RtcpAttribute} objects.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class RtcpAttributeParser implements SdpParser<RtcpAttribute> {

	// TODO use appropriate IP pattern instead of [0-9\\.]+
	private static final String REGEX = "^a=rtcp:\\d+(\\s\\w+\\s\\w+\\s[0-9\\.]+)?$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	
	@Override
	public boolean canParse(String sdp) {
		if(sdp == null || sdp.isEmpty()) {
			return false;
		}
		return PATTERN.matcher(sdp.trim()).matches();
	}
	
	@Override
	public RtcpAttribute parse(String sdp) throws SdpException {
		try {
			// Gather values from SDP
			int separator = sdp.indexOf(AttributeField.ATTRIBUTE_SEPARATOR);
			if(separator == -1) {
				throw new IllegalArgumentException("No value found");
			}
			
			String[] values = sdp.trim().substring(separator + 1).split(" ");
			
			// Parse mandatory values
			int port = Integer.parseInt(values[0]);
			
			// Parse optional values if available
			if(values.length > 1) {
				String netType= values[1];
				String addressType = values[2];
				String address = values[3];
				return new RtcpAttribute(port, netType, addressType, address);
			}
			return new RtcpAttribute(port);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}
	
	@Override
	public void parse(RtcpAttribute field, String sdp) throws SdpException {
		try {
			// Gather values from SDP
			int separator = sdp.indexOf(AttributeField.ATTRIBUTE_SEPARATOR);
			if(separator == -1) {
				throw new IllegalArgumentException("No value found");
			}
			String[] values = sdp.trim().substring(separator + 1).split(" ");
			
			// Parse mandatory values
			int port = Integer.parseInt(values[0]);
			String netType= null;
			String addressType = null;
			String address = null;
			
			// Parse optional values if available
			if(values.length > 1) {
				netType= values[1];
				addressType = values[2];
				address = values[3];
			}
			
			// Persist parsed values
			field.setPort(port);
			field.setNetworkType(netType);
			field.setAddressType(addressType);
			field.setAddress(address);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}
	
}
