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
 * The "proto" field describes the transport protocol used.
 * <p>
 * RFC4566 registers three values:
 * <ul>
 * <li><b>"RTP/AVP"</b> is a reference to RTP used under the RTP Profile for
 * Audio and Video Conferences with Minimal Control running over UDP/IP</li>
 * <li><b>"RTP/SAVP"</b> is a reference to the Secure Real-time Transport
 * Protocol</li>
 * <li><b>"udp"</b> indicates an unspecified protocol over UDP.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * RFC4585 registers two values:
 * <li><b>"RTP/AVPF"</b> is an extension to the Audio-visual Profile (AVP) that
 * enables receivers to provide, statistically, more immediate feedback to the
 * senders and thus allows for short-term adaptation and efficient
 * feedback-based repair mechanisms to be implemented. This early feedback
 * profile (AVPF) maintains the AVP bandwidth constraints for RTCP and preserves
 * scalability to large groups.</li>
 * <li><b>"RTP/SAVPF"</b> refers to the AVPF profile on top of the Secure
 * Real-time Transport Protocol</li>
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public enum MediaProfile {
	UDP("udp"), RTP_AVP("RTP/AVP"), RTP_AVPF("RTP/AVPF"), RTP_SAVP("RTP/SAVP"), RTP_SAVPF("RTP/SAVPF");

	private final String profile;

	private MediaProfile(String profile) {
		this.profile = profile;
	}

	public String getProfile() {
		return profile;
	}

	public static MediaProfile fromProfile(String profile) {
		for (MediaProfile mp : values()) {
			if (mp.profile.equals(profile)) {
				return mp;
			}
		}
		return null;
	}

	public static boolean containsProfile(String profile) {
		return (fromProfile(profile) != null);
	}
}
