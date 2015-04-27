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

package jazmin.server.sip.io.sdp.ice.attributes.parser;

import java.util.regex.Pattern;

import jazmin.server.sip.io.sdp.SdpException;
import jazmin.server.sip.io.sdp.SdpParser;
import jazmin.server.sip.io.sdp.ice.attributes.CandidateAttribute;


/**
 * Parses SDP text to construct {@link CandidateAttribute} objects.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class CandidateAttributeParser implements SdpParser<CandidateAttribute> {
	
	// TODO use proper IP address regex instead of [0-9\\.]+
	private static final String REGEX = "^a=candidate:\\w+\\s\\d\\s\\w+\\s\\d+\\s[0-9\\.]+\\s\\d+\\s(typ)\\s\\w+(\\stcptype\\s\\w+)?(\\s(raddr)\\s[0-9\\.]+\\s(rport)\\s\\d+)?\\s(generation)\\s\\d+$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	@Override
	public boolean canParse(String sdp) {
		if(sdp == null || sdp.isEmpty()) {
			return false;
		}
		return PATTERN.matcher(sdp.trim()).matches();
	}

	@Override
	public CandidateAttribute parse(String sdp) throws SdpException {
		try {
			String[] values = sdp.trim().substring(12).split(" ");
			int index = 0;

			// extract data from SDP
			String foundation = values[index++];
			short componentId = Short.parseShort(values[index++]);
			String protocol = values[index++];
			long priority = Long.parseLong(values[index++]);
			String address = values[index++];
			int port = Integer.parseInt(values[index++]);
			index++; // TYP
			String type = values[index++];
			
			if(!CandidateAttribute.isCandidateTypeValid(type)) {
				throw new IllegalArgumentException("Unrecognized candidate type: " + type);
			}
			
			String relatedAddress = null;
			int relatedPort = 0;
			if(!CandidateAttribute.TYP_HOST.equals(type)) {
				index++; // RADDR
				relatedAddress = values[index++];
				index++; // RPORT
				relatedPort = Integer.parseInt(values[index++]);
			}
			
			String tcptype = null;
			if(protocol.equals("tcp")) {
				index++; // TCPTYPE
				tcptype = values[index++];
			}
			
			index++; // GENERATION which is optional
			int generation = 0;
			if(index == values.length - 1) {
				generation = Integer.parseInt(values[index]);
			}
			
			// Create object from extracted data
			CandidateAttribute candidate = new CandidateAttribute();
			candidate.setFoundation(foundation);
			candidate.setComponentId(componentId);
			candidate.setProtocol(protocol);
			candidate.setPriority(priority);
			candidate.setAddress(address);
			candidate.setPort(port);
			candidate.setCandidateType(type);
			candidate.setRelatedAddress(relatedAddress);
			candidate.setRelatedPort(relatedPort);
			candidate.setTcpType(tcptype);
			candidate.setGeneration(generation);
			return candidate;
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

	@Override
	public void parse(CandidateAttribute field, String sdp) throws SdpException {
		try {
			String[] values = sdp.trim().substring(12).split(" ");
			int index = 0;

			// extract data from SDP
			String foundation = values[index++];
			short componentId = Short.parseShort(values[index++]);
			String protocol = values[index++];
			long priority = Long.parseLong(values[index++]);
			String address = values[index++];
			int port = Integer.parseInt(values[index++]);
			index++; // TYP
			String type = values[index++];
			
			if(!CandidateAttribute.isCandidateTypeValid(type)) {
				throw new IllegalArgumentException("Unrecognized candidate type: " + type);
			}
			
			String relatedAddress = null;
			int relatedPort = 0;
			if(!CandidateAttribute.TYP_HOST.equals(type)) {
				index++; // RADDR
				relatedAddress = values[index++];
				index++; // RPORT
				relatedPort = Integer.parseInt(values[index++]);
			}
			
			String tcptype = null;
			if(protocol.equals("tcp")) {
				index++; // TCPTYPE
				tcptype = values[index++];
			}
			
			index++; // GENERATION which is optional
			int generation = 0;
			if(index == values.length - 1) {
				generation = Integer.parseInt(values[index]);
			}
			
			// Create object from extracted data
			field.setFoundation(foundation);
			field.setComponentId(componentId);
			field.setProtocol(protocol);
			field.setPriority(priority);
			field.setAddress(address);
			field.setPort(port);
			field.setCandidateType(type);
			field.setRelatedAddress(relatedAddress);
			field.setRelatedPort(relatedPort);
			field.setTcpType(tcptype);
			field.setGeneration(generation);
		} catch (Exception e) {
			throw new SdpException(PARSE_ERROR + sdp, e);
		}
	}

}
