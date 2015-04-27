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

import jazmin.server.sip.io.sdp.fields.AttributeField;

/**
 * a=rtpmap:[payload type][encoding name]/[clock rate]/[encoding parameters*]<br>
 * 
 * <p>
 * m=audio 49230 RTP/AVP 96 97 98<br>
 * a=rtpmap:96 L8/8000<br>
 * a=rtpmap:97 L16/8000<br>
 * a=rtpmap:98 L16/11025/2
 * </p>
 * 
 * <p>
 * This attribute maps from an RTP payload type number (as used in an "m=" line)
 * to an encoding name denoting the payload format to be used.<br>
 * It also provides information on the clock rate and encoding parameters. It is
 * a media-level attribute that is not dependent on charset.
 * </p>
 * 
 * <p>
 * Although an RTP profile may make static assignments of payload type numbers
 * to payload formats, it is more common for that assignment to be done
 * dynamically using "a=rtpmap:" attributes.<br>
 * As an example of a static payload type, consider u-law PCM coded
 * single-channel audio sampled at 8 kHz. This is completely defined in the RTP
 * Audio/Video profile as payload type 0, so there is no need for an "a=rtpmap:"
 * attribute, and the media for such a stream sent to UDP port 49232 can be
 * specified as:<br>
 * <br>
 * 
 * m=audio 49232 RTP/AVP 0<br>
 * <br>
 * An example of a dynamic payload type is 16-bit linear encoded stereo audio
 * sampled at 16 kHz.<br>
 * If we wish to use the dynamic RTP/AVP payload type 98 for this stream,
 * additional information is required to decode it:<br>
 * <br>
 * m=audio 49232 RTP/AVP 98<br>
 * a=rtpmap:98 L16/16000/2
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class RtpMapAttribute extends AttributeField {
	
	public static final String ATTRIBUTE_TYPE = "rtpmap";
	public static final short DEFAULT_CODEC_PARAMS = 1;
	
	private int payloadType;
	private String codec;
	private int clockRate;
	private int codecParams;
	
	private FormatParameterAttribute parameters;
	
	public RtpMapAttribute() {
		super(ATTRIBUTE_TYPE);
		this.codecParams = DEFAULT_CODEC_PARAMS;
	}
	
	public RtpMapAttribute(int payloadType, String codec, int clockRate, int codecParams) {
		super(ATTRIBUTE_TYPE);
		this.payloadType = payloadType;
		this.codec = codec;
		this.clockRate = clockRate;
		this.codecParams = codecParams;
	}

	public int getPayloadType() {
		return payloadType;
	}

	public void setPayloadType(int payloadType) {
		this.payloadType = payloadType;
	}

	public String getCodec() {
		return codec;
	}

	public void setCodec(String codec) {
		this.codec = codec;
	}

	public int getClockRate() {
		return clockRate;
	}

	public void setClockRate(int clockRate) {
		this.clockRate = clockRate;
	}

	public int getCodecParams() {
		return codecParams;
	}

	public void setCodecParams(int codecParams) {
		this.codecParams = codecParams;
	}

	public FormatParameterAttribute getParameters() {
		return parameters;
	}

	public void setParameters(FormatParameterAttribute parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		// clear builder
		super.builder.setLength(0);
		super.builder.append(BEGIN).append(ATTRIBUTE_TYPE).append(ATTRIBUTE_SEPARATOR)
		        .append(this.payloadType).append(" ")
		        .append(this.codec).append("/")
				.append(this.clockRate);
		if (this.codecParams != DEFAULT_CODEC_PARAMS) {
			super.builder.append("/").append(this.codecParams);
		}
		return builder.toString();
	}

}
