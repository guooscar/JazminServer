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


/**
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 * @param <T>
 *            The type of SDP {@link SdpField} the parser is related to.
 */
public interface SdpParser<T extends SdpField> {

	String PARSE_ERROR = "Could not parse SDP: ";
	
	/**
	 * Checks whether the parse is capable of parsing a specific SDP line.
	 * 
	 * @param sdp
	 *            The SDP line to be parsed
	 * @return Returns <code>true</code> if capable of parsing. Returns
	 *         <code>false</code> otherwise.
	 */
	boolean canParse(String sdp);

	/**
	 * Parses the SDP originating a new {@link SdpField} object.
	 * <p>
	 * <b>Careful!</b> The parser will blindly attempt to parse the SDP text.<br>
	 * Users should invoke the {@link #canParse(String)} method beforehand to
	 * make sure the parsers is able to parse the SDP text.
	 * </p>
	 * 
	 * @param sdp
	 *            The SDP to be parsed
	 * @return An {@link SdpField} object based on the SDP line
	 * @throws SdpException
	 *             In case the parser cannot parse the SDP line.
	 */
	T parse(String sdp) throws SdpException;

	/**
	 * Parses the SDP to override the values of an existing {@link SdpField}
	 * object.
	 * <p>
	 * <b>Careful!</b> The parser will blindly attempt to parse the SDP text.<br>
	 * Users should invoke the {@link #canParse(String)} method beforehand to
	 * make sure the parsers is able to parse the SDP text.
	 * </p>
	 * 
	 * @param field
	 *            The {@link SdpField} to be overwritten
	 * @param sdp
	 *            The SDP to be parsed
	 * @throws SdpException
	 *             In case the parser cannot parse the SDP line.
	 */
	void parse(T field, String sdp) throws SdpException;
	
}
