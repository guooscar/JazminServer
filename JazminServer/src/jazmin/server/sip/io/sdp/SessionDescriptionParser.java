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

package jazmin.server.sip.io.sdp;

import jazmin.server.sip.io.sdp.attributes.ConnectionModeAttribute;
import jazmin.server.sip.io.sdp.attributes.FormatParameterAttribute;
import jazmin.server.sip.io.sdp.attributes.MaxPacketTimeAttribute;
import jazmin.server.sip.io.sdp.attributes.PacketTimeAttribute;
import jazmin.server.sip.io.sdp.attributes.RtpMapAttribute;
import jazmin.server.sip.io.sdp.attributes.SsrcAttribute;
import jazmin.server.sip.io.sdp.dtls.attributes.FingerprintAttribute;
import jazmin.server.sip.io.sdp.dtls.attributes.SetupAttribute;
import jazmin.server.sip.io.sdp.fields.AttributeField;
import jazmin.server.sip.io.sdp.fields.ConnectionField;
import jazmin.server.sip.io.sdp.fields.MediaDescriptionField;
import jazmin.server.sip.io.sdp.fields.OriginField;
import jazmin.server.sip.io.sdp.fields.SessionNameField;
import jazmin.server.sip.io.sdp.fields.TimingField;
import jazmin.server.sip.io.sdp.fields.VersionField;
import jazmin.server.sip.io.sdp.ice.attributes.CandidateAttribute;
import jazmin.server.sip.io.sdp.ice.attributes.IceLiteAttribute;
import jazmin.server.sip.io.sdp.ice.attributes.IcePwdAttribute;
import jazmin.server.sip.io.sdp.ice.attributes.IceUfragAttribute;
import jazmin.server.sip.io.sdp.rtcp.attributes.RtcpAttribute;
import jazmin.server.sip.io.sdp.rtcp.attributes.RtcpMuxAttribute;

/**
 * Parses an SDP text description into a {@link SessionDescription} object.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class SessionDescriptionParser {

	private static final String NEWLINE = "\n";
	private static final String PARSE_ERROR = "Cannot parse SDP: ";
	private static final String PARSE_ERROR_EMPTY = PARSE_ERROR + "empty";

	private static final SdpParserPipeline PARSERS = new SdpParserPipeline();

	public static SessionDescription parse(String text) throws SdpException {

		if (text == null || text.isEmpty()) {
			throw new SdpException(PARSE_ERROR_EMPTY);
		}

		SdpParsingInfo info = new SdpParsingInfo();

		// Process each line of SDP
		String[] lines = text.split(NEWLINE);
		for (String line : lines) {
			try {
				// Get type of field to check whether its an sdp attribute or
				// not
				line=line.trim();
				char fieldType = line.charAt(0);
				
				switch (fieldType) {
				case AttributeField.FIELD_TYPE:
					// The field is an attribute
					// Get the type of attribute so we can invoke the right
					// parser
					int separator = line
							.indexOf(AttributeField.ATTRIBUTE_SEPARATOR);
					String attributeType = (separator == -1) ? line
							.substring(2).trim() : line.substring(2, separator);

					// Get the right parser for the attribute and parse the text
					SdpParser<? extends AttributeField> attributeParser = PARSERS
							.getAttributeParser(attributeType);
					if (attributeParser != null) {
						convertAndApplyAttribute(attributeParser.parse(line),
								info);
					}
					break;
				default:
					// Get the right parser for the field and parse the text
					SdpParser<? extends SdpField> fieldParser = PARSERS
							.getFieldParser(fieldType);
					if (fieldParser != null) {
						convertAndApplyField(fieldParser.parse(line), info);
					}
					break;
				}
			} catch (Exception e) {
				throw new SdpException("Could not parse SDP: " + line, e);
			}
		}
		return info.sdp;
	}

	private static void convertAndApplyField(SdpField field, SdpParsingInfo info) {
		switch (field.getFieldType()) {
		case VersionField.FIELD_TYPE:
			info.sdp.setVersion((VersionField) field);
			break;

		case OriginField.FIELD_TYPE:
			info.sdp.setOrigin((OriginField) field);
			break;

		case SessionNameField.FIELD_TYPE:
			info.sdp.setSessionName((SessionNameField) field);
			break;

		case TimingField.FIELD_TYPE:
			info.sdp.setTiming((TimingField) field);
			break;

		case ConnectionField.FIELD_TYPE:
			if (info.media == null) {
				info.sdp.setConnection((ConnectionField) field);
			} else {
				info.media.setConnection((ConnectionField) field);
			}
			break;

		case MediaDescriptionField.FIELD_TYPE:
			info.media = (MediaDescriptionField) field;
			info.sdp.addMediaDescription(info.media);
			info.media.setSession(info.sdp);
			break;

		default:
			// Ignore unsupported type
			break;
		}
	}

	private static void convertAndApplyAttribute(AttributeField attribute,
			SdpParsingInfo info) {
		switch (attribute.getKey()) {
		case RtpMapAttribute.ATTRIBUTE_TYPE:
			info.format = (RtpMapAttribute) attribute;
			info.media.addFormat(info.format);
			break;

		case FormatParameterAttribute.ATTRIBUTE_TYPE:
			info.format.setParameters((FormatParameterAttribute) attribute);
			break;

		case PacketTimeAttribute.ATTRIBUTE_TYPE:
			info.media.setPtime((PacketTimeAttribute) attribute);
			break;

		case MaxPacketTimeAttribute.ATTRIBUTE_TYPE:
			info.media.setMaxptime((MaxPacketTimeAttribute) attribute);
			break;

		case ConnectionModeAttribute.SENDONLY:
		case ConnectionModeAttribute.RECVONLY:
		case ConnectionModeAttribute.SENDRECV:
		case ConnectionModeAttribute.INACTIVE:
			if (info.media == null) {
				info.sdp.setConnectionMode((ConnectionModeAttribute) attribute);
			} else {
				info.media
						.setConnectionMode((ConnectionModeAttribute) attribute);
			}
			break;

		case RtcpAttribute.ATTRIBUTE_TYPE:
			info.media.setRtcp((RtcpAttribute) attribute);
			break;

		case RtcpMuxAttribute.ATTRIBUTE_TYPE:
			info.media.setRtcpMux((RtcpMuxAttribute) attribute);
			break;

		case IceLiteAttribute.ATTRIBUTE_TYPE:
			info.sdp.setIceLite((IceLiteAttribute) attribute);
			break;

		case IceUfragAttribute.ATTRIBUTE_TYPE:
			if (info.media == null) {
				info.sdp.setIceUfrag((IceUfragAttribute) attribute);
			} else {
				info.media.setIceUfrag((IceUfragAttribute) attribute);
			}
			break;

		case IcePwdAttribute.ATTRIBUTE_TYPE:
			if (info.media == null) {
				info.sdp.setIcePwd((IcePwdAttribute) attribute);
			} else {
				info.media.setIcePwd((IcePwdAttribute) attribute);
			}
			break;

		case CandidateAttribute.ATTRIBUTE_TYPE:
			info.media.addCandidate((CandidateAttribute) attribute);
			break;

		case SetupAttribute.ATTRIBUTE_TYPE:
			if (info.media == null) {
				info.sdp.setSetup((SetupAttribute) attribute);
			} else {
				info.media.setSetup((SetupAttribute) attribute);
			}
			break;

		case FingerprintAttribute.ATTRIBUTE_TYPE:
			if (info.media == null) {
				info.sdp.setFingerprint((FingerprintAttribute) attribute);
			} else {
				info.media.setFingerprint((FingerprintAttribute) attribute);
			}
			break;

		case SsrcAttribute.ATTRIBUTE_TYPE:
			SsrcAttribute ssrc = (SsrcAttribute) attribute;
			SsrcAttribute ssrcAttribute = info.media.getSsrc();
			if (ssrcAttribute == null) {
				info.media.setSsrc(ssrc);
			} else {
				ssrcAttribute.addAttribute(ssrc.getLastAttribute(),
						ssrc.getLastValue());
			}
			break;

		default:
			break;
		}
	}

	private static class SdpParsingInfo {

		final SessionDescription sdp;
		MediaDescriptionField media;
		RtpMapAttribute format;

		public SdpParsingInfo() {
			this.sdp = new SessionDescription();
		}

	}

}
