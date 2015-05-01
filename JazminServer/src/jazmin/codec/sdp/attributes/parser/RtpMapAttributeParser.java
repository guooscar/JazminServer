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

package jazmin.codec.sdp.attributes.parser;

import java.util.regex.Pattern;

import jazmin.codec.sdp.SdpException;
import jazmin.codec.sdp.SdpParser;
import jazmin.codec.sdp.attributes.RtpMapAttribute;

/**
 * Parses SDP text to construct {@link RtpMapAttribute} objects.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class RtpMapAttributeParser implements SdpParser<RtpMapAttribute> {

	private static final String REGEX = "^a=rtpmap:\\d+\\s\\w+/\\d+(/\\d+)?$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	@Override
	public boolean canParse(String sdp) {
		if(sdp == null || sdp.isEmpty()) {
			return false;
		}
		return PATTERN.matcher(sdp.trim()).matches();
	}

	@Override
	public RtpMapAttribute parse(String sdp) throws SdpException {
		try {
			// Extract data from SDP
			int index = 0;
			String[] values = sdp.trim().substring(9).split("\\s|/");
			
			int payloadType = Integer.parseInt(values[index++]);
			String codec = values[index++];
			int clockRate = Integer.parseInt(values[index++]);
			int codecParams = 1;
			if(index == values.length - 1) {
				codecParams = Integer.parseInt(values[index]);
			}
			
			// Build object from extracted data
			return new RtpMapAttribute(payloadType, codec, clockRate, codecParams);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

	@Override
	public void parse(RtpMapAttribute field, String sdp) throws SdpException {
		try {
			// Extract data from SDP
			int index = 0;
			String[] values = sdp.trim().substring(9).split("\\s|/");
			
			int payloadType = Integer.parseInt(values[index++]);
			String codec = values[index++];
			int clockRate = Integer.parseInt(values[index++]);
			int codecParams = 1;
			if(index == values.length - 1) {
				codecParams = Integer.parseInt(values[index]);
			}
			
			// Build object from extracted data
			field.setPayloadType(payloadType);
			field.setCodec(codec);
			field.setClockRate(clockRate);
			field.setCodecParams(codecParams);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}
}
