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

package jazmin.server.sip.io.sdp.fields.parser;

import java.util.regex.Pattern;

import jazmin.server.sip.io.sdp.SdpException;
import jazmin.server.sip.io.sdp.SdpParser;
import jazmin.server.sip.io.sdp.fields.VersionField;

/**
 * Parses SDP text to construct {@link VersionField} objects.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class VersionFieldParser implements SdpParser<VersionField> {

	private static final String REGEX = "^v=\\d+$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	@Override
	public boolean canParse(String sdp) {
		if (sdp == null || sdp.isEmpty()) {
			return false;
		}
		return PATTERN.matcher(sdp.trim()).matches();
	}

	@Override
	public VersionField parse(String sdp) throws SdpException {
		try {
			short version = Short.parseShort(sdp.trim().substring(2));
			return new VersionField(version);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

	@Override
	public void parse(VersionField field, String sdp) throws SdpException {
		try {
			short version = Short.parseShort(sdp.trim().substring(2));
			field.setVersion(version);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

}
