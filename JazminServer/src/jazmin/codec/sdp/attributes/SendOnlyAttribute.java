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

package jazmin.codec.sdp.attributes;


/**
 * a=sendonly
 * 
 * <p>
 * This specifies that the tools should be started in send-only mode.
 * </p>
 * <p>
 * An example may be where a different unicast address is to be used for a
 * traffic destination than for a traffic source. In such a case, two media
 * descriptions may be used, one sendonly and one recvonly. It can be either a
 * session- or media-level attribute, but would normally only be used as a media
 * attribute. It is not dependent on charset. Note that sendonly applies only to
 * the media, and any associated control protocol (e.g., RTCP) SHOULD still be
 * received and processed as normal.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class SendOnlyAttribute extends AbstractConnectionModeAttribute {

	public static final String ATTRIBUTE_TYPE = "sendonly";
	private static final String FULL = "a=sendonly";

	public SendOnlyAttribute() {
		super(ATTRIBUTE_TYPE);
	}

	@Override
	public String toString() {
		return FULL;
	}

}
