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
import jazmin.server.sip.io.sdp.fields.ConnectionField;

/**
 * Parses SDP text to construct {@link ConnectionField} objects.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class ConnectionFieldParser implements SdpParser<ConnectionField> {

	// TODO use proper regex for IP address instead of [0-9\\.]+
	private static final String REGEX = "^c=\\w+\\s\\w+\\s[0-9\\.]+$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	@Override
	public boolean canParse(String sdp) {
		if (sdp == null || sdp.isEmpty()) {
			return false;
		}
		return PATTERN.matcher(sdp.trim()).matches();
	}

	@Override
	public ConnectionField parse(String sdp) throws SdpException {
		try {
			String[] values = sdp.trim().substring(2).split(" ");
			String networkType = values[0];
			String addressType = values[1];
			String address = values[2];
			return new ConnectionField(networkType, addressType, address);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

	@Override
	public void parse(ConnectionField field, String sdp) throws SdpException {
		try {
			String[] values = sdp.trim().substring(2).split(" ");
			field.setNetworkType(values[0]);
			field.setAddressType(values[1]);
			field.setAddress(values[2]);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

}
