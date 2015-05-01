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

package jazmin.codec.sdp.ice.attributes.parser;

import java.util.regex.Pattern;

import jazmin.codec.sdp.SdpException;
import jazmin.codec.sdp.SdpParser;
import jazmin.codec.sdp.fields.AttributeField;
import jazmin.codec.sdp.ice.attributes.IcePwdAttribute;

/**
 * Parses SDP text to construct {@link IcePwdAttribute} objects.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class IcePwdAttributeParser implements SdpParser<IcePwdAttribute> {

	private static final String REGEX = "^a=ice-pwd\\S+$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	
	@Override
	public boolean canParse(String sdp) {
		if(sdp == null || sdp.isEmpty()) {
			return false;
		}
		return PATTERN.matcher(sdp.trim()).matches();
	}

	@Override
	public IcePwdAttribute parse(String sdp) throws SdpException {
		try {
			// extract data from sdp
			int separator = sdp.indexOf(AttributeField.ATTRIBUTE_SEPARATOR);
			if(separator == -1) {
				throw new IllegalArgumentException("Attribute has no value");
			}
			
			String password = sdp.trim().substring(separator + 1);
			if(password.isEmpty()) {
				throw new IllegalArgumentException("Value is empty");
			}
			
			// Build object
			return new IcePwdAttribute(password);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

	@Override
	public void parse(IcePwdAttribute field, String sdp) throws SdpException {
		try {
			// extract data from sdp
			int separator = sdp.indexOf(AttributeField.ATTRIBUTE_SEPARATOR);
			if(separator == -1) {
				throw new IllegalArgumentException("Attribute has no value");
			}
			
			String password = sdp.trim().substring(separator + 1);
			if(password.isEmpty()) {
				throw new IllegalArgumentException("Value is empty");
			}
			
			// Build object
			field.setPassword(password);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

}
