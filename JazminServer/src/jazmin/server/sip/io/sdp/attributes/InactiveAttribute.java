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

package jazmin.server.sip.io.sdp.attributes;


/**
 * a=inactive
 * <p>
 * This specifies that the tools should be started in inactive mode.
 * </p>
 * <p>
 * This is necessary for interactive conferences where users can put other users
 * on hold. No media is sent over an inactive media stream. Note that an
 * RTP-based system SHOULD still send RTCP, even if started inactive. It can be
 * either a session or media-level attribute, and it is not dependent on
 * charset.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class InactiveAttribute extends AbstractConnectionModeAttribute {

	public static final String ATTRIBUTE_TYPE = "inactive";
	private static final String FULL = "a=inactive";

	public InactiveAttribute() {
		super(ATTRIBUTE_TYPE);
	}

	@Override
	public String toString() {
		return FULL;
	}

}
