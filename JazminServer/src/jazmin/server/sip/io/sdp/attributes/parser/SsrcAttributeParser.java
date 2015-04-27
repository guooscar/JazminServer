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

package jazmin.server.sip.io.sdp.attributes.parser;

import java.util.regex.Pattern;

import jazmin.server.sip.io.sdp.SdpException;
import jazmin.server.sip.io.sdp.SdpParser;
import jazmin.server.sip.io.sdp.attributes.SsrcAttribute;

/**
 * Parses SDP text to construct {@link SsrcAttribute} objects.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class SsrcAttributeParser implements SdpParser<SsrcAttribute> {

	private static final String REGEX = "^a=ssrc:\\S+\\s\\w+(:\\S+)?$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	
	private static final String COLON = ":";
	private static final String WHITESPACE = " ";

	@Override
	public boolean canParse(String sdp) {
		if(sdp == null || sdp.isEmpty()) {
			return false;
		}
		return PATTERN.matcher(sdp.trim()).matches();
	}

	@Override
	public SsrcAttribute parse(String sdp) throws SdpException {
		try {
			String[] values = sdp.trim().substring(7).split(WHITESPACE);
			int separator = values[1].indexOf(COLON);

			String attName = (separator == -1) ? values[1] : values[1].substring(0, separator);
			String attValue = (separator == -1) ? null : values[1].substring(separator + 1);
			
			SsrcAttribute ssrc = new SsrcAttribute(values[0]);
			ssrc.addAttribute(attName, attValue);
			return ssrc;
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

	@Override
	public void parse(SsrcAttribute field, String sdp) throws SdpException {
		try {
			String[] values = sdp.trim().substring(7).split(WHITESPACE);
			int separator = values[1].indexOf(COLON);

			String attName = (separator == -1) ? values[1] : values[1].substring(0, separator);
			String attValue = (separator == -1) ? null : values[1].substring(separator + 1);
			
			// If ssrc-id is different then it means we are processing new sdp
			// Reset original ssrc and start writing new ssrc information
			if(!field.getSsrcId().equals(values[0])) {
				field.reset();
				field.setSsrcId(values[0]);
			}
			
			// Keep adding attributes to the same ssrc
			field.addAttribute(attName, attValue);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR, e);
		}
	}

}
