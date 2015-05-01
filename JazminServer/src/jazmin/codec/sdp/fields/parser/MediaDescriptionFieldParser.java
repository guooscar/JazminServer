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

package jazmin.codec.sdp.fields.parser;

import java.util.regex.Pattern;

import jazmin.codec.sdp.MediaProfile;
import jazmin.codec.sdp.SdpException;
import jazmin.codec.sdp.SdpParser;
import jazmin.codec.sdp.fields.MediaDescriptionField;

public class MediaDescriptionFieldParser implements SdpParser<MediaDescriptionField> {

	private static final String REGEX = "^m=[a-zA-Z]+\\s\\d+\\s[a-zA-Z/]+(\\s\\d+)*$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	
	@Override
	public boolean canParse(String sdp) {
		if(sdp == null || sdp.isEmpty()) {
			return false;
		}
		return PATTERN.matcher(sdp.trim()).matches();
	}

	@Override
	public MediaDescriptionField parse(String sdp) throws SdpException {
		try {
			// Extract data from SDP
			String[] values = sdp.trim().substring(2).split(" ");
			int index = 0;
			int maxIndex = values.length - 1;

			String media = values[index++];
			int port = Integer.parseInt(values[index++]);
			MediaProfile protocol = MediaProfile.fromProfile(values[index++]);
			if(protocol == null) {
				throw new IllegalArgumentException("Unrecognized media profile");
			}

			int[] formats = null;
			if (maxIndex - index >= 0) {
				int numFormats = maxIndex - index + 1;
				formats = new int[numFormats];
				for (int i = 0; i < numFormats; i++) {
					formats[i] = Integer.parseInt(values[i + index]);
				}
			}

			// Build object
			MediaDescriptionField md = new MediaDescriptionField();
			md.setMedia(media);
			md.setPort(port);
			md.setProtocol(protocol);
			md.setPayloadTypes(formats);
			return md;
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

	@Override
	public void parse(MediaDescriptionField field, String sdp) throws SdpException {
		try {
			// Extract data from SDP
			String[] values = sdp.trim().substring(2).split(" ");
			int index = 0;
			int maxIndex = values.length - 1;

			String media = values[index++];
			int port = Integer.parseInt(values[index++]);
			MediaProfile protocol = MediaProfile.fromProfile(values[index++]);
			if(protocol == null) {
				throw new IllegalArgumentException("Unrecognized media profile");
			}

			int[] payloadTypes = null;
			if (maxIndex - index >= 0) {
				int numFormats = maxIndex - index + 1;
				payloadTypes = new int[numFormats];
				for (int i = 0; i < numFormats; i++) {
					payloadTypes[i] = Integer.parseInt(values[i + index]);
				}
			}

			// Build object
			field.setMedia(media);
			field.setPort(port);
			field.setProtocol(protocol);
			field.setPayloadTypes(payloadTypes);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

}
