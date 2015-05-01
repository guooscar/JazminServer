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
import jazmin.codec.sdp.attributes.ConnectionModeAttribute;

/**
 * Parses SDP text to construct {@link ConnectionModeAttribute} objects.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class ConnectionModeAttributeParser implements SdpParser<ConnectionModeAttribute> {

	private static final String REGEX = "^a=(sendonly|recvonly|sendrecv|inactive)$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	private boolean isTypeValid(String type) {
		return ConnectionModeAttribute.SENDONLY.equals(type) ||
				ConnectionModeAttribute.RECVONLY.equals(type) ||
				ConnectionModeAttribute.SENDRECV.equals(type) ||
				ConnectionModeAttribute.INACTIVE.equals(type);
	}
	
	@Override
	public boolean canParse(String sdp) {
		if (sdp == null || sdp.isEmpty()) {
			return false;
		}
		return PATTERN.matcher(sdp.trim()).matches();
	}

	@Override
	public ConnectionModeAttribute parse(String sdp) throws SdpException {
		try {
			String mode = sdp.trim().substring(2);
			if (!isTypeValid(mode)) {
				throw new IllegalArgumentException("Unknown connection mode");
			}
			return new ConnectionModeAttribute(mode);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

	@Override
	public void parse(ConnectionModeAttribute field, String sdp) throws SdpException {
		try {
			String mode = sdp.trim().substring(2);
			if (!isTypeValid(mode)) {
				throw new IllegalArgumentException("Unknown connection mode");
			}
			field.setMode(mode);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

}
