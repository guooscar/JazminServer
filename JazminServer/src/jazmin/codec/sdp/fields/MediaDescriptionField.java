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

package jazmin.codec.sdp.fields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jazmin.codec.sdp.MediaProfile;
import jazmin.codec.sdp.OtherField;
import jazmin.codec.sdp.SdpField;
import jazmin.codec.sdp.SessionLevelAccessor;
import jazmin.codec.sdp.attributes.ConnectionModeAttribute;
import jazmin.codec.sdp.attributes.FormatParameterAttribute;
import jazmin.codec.sdp.attributes.MaxPacketTimeAttribute;
import jazmin.codec.sdp.attributes.PacketTimeAttribute;
import jazmin.codec.sdp.attributes.RtpMapAttribute;
import jazmin.codec.sdp.attributes.SsrcAttribute;
import jazmin.codec.sdp.dtls.attributes.FingerprintAttribute;
import jazmin.codec.sdp.dtls.attributes.SetupAttribute;
import jazmin.codec.sdp.ice.attributes.CandidateAttribute;
import jazmin.codec.sdp.ice.attributes.IcePwdAttribute;
import jazmin.codec.sdp.ice.attributes.IceUfragAttribute;
import jazmin.codec.sdp.rtcp.attributes.RtcpAttribute;
import jazmin.codec.sdp.rtcp.attributes.RtcpMuxAttribute;

/**
 * m=[media] [port] [proto] [fmt]
 * 
 * <p>
 * A session description may contain a number of media descriptions.<br>
 * Each media description starts with an "m=" field and is terminated by either
 * the next "m=" field or by the end of the session description.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * 
 */
public class MediaDescriptionField implements SdpField {
	
	private static final String NEWLINE = "\r\n";
	public static final char FIELD_TYPE = 'm';
	private static final String BEGIN = "m=";
	
	private SessionLevelAccessor session;

	// SDP fields (media description specific)
	private String media;
	private int port;
	private MediaProfile protocol;
	private final List<Integer> payloadTypes;
	private final Map<Integer, RtpMapAttribute> formats;
	
	// SDP fields and attributes (media-level)
	private ConnectionField connection;
	private ConnectionModeAttribute connectionMode;
	private RtcpAttribute rtcp;
	private RtcpMuxAttribute rtcpMux;
	private SsrcAttribute ssrc;
	private PacketTimeAttribute ptime;
	private MaxPacketTimeAttribute maxptime;

	// ICE attributes (session-level)
	private IcePwdAttribute icePwd;
	private IceUfragAttribute iceUfrag;
	private List<CandidateAttribute> candidates;
	
	// WebRTC attributes (session-level)
	private FingerprintAttribute fingerprint;
	private SetupAttribute setup;
	private List<OtherField>otherFields;

	private final StringBuilder builder;

	public MediaDescriptionField() {
		this(null);
	}
	
	public MediaDescriptionField(final SessionLevelAccessor sessionAccessor) {
		this.session = sessionAccessor;
		this.builder = new StringBuilder(BEGIN);
		this.payloadTypes = new ArrayList<Integer>(10);
		this.formats = new HashMap<Integer, RtpMapAttribute>(10);
		otherFields=new LinkedList<OtherField>();
	}
	
	public void setSession(SessionLevelAccessor session) {
		this.session = session;
	}
	
	public String getMedia() {
		return media;
	}

	public void setMedia(String media) {
		this.media = media;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol.getProfile();
	}

	public void setProtocol(MediaProfile protocol) {
		this.protocol = protocol;
	}
	
	public void addPayloadType(int payloadType) {
		if(!this.payloadTypes.contains(payloadType)) {
			this.payloadTypes.add(payloadType);
		}
	}
	
	public void setPayloadTypes(int... payloadTypes) {
		this.payloadTypes.clear();
		for (int payloadType : payloadTypes) {
			addPayloadType(payloadType);
		}
	}
	
	public boolean containsPayloadType(int payloadType) {
		return this.payloadTypes.contains(payloadType);
	}
	
	public void setFormats(RtpMapAttribute ...formats) {
		this.formats.clear();
		int numFormats = formats.length;
		for (int i = 0; i < numFormats; i++) {
			addFormat(formats[i]);
		}
	}
	
	public void addFormat(RtpMapAttribute format) {
		this.formats.put(format.getPayloadType(), format);
	}
	
	public void addFormats(RtpMapAttribute ...formats) {
		int numFormats = formats.length;
		for (int i = 0; i < numFormats; i++) {
			addFormat(formats[i]);
		}
	}
	
	public boolean containsFormat(int format) {
		return this.formats.containsKey(format);
	}
	
	public void setFormatParameters(short payloadType, FormatParameterAttribute parameters) {
		RtpMapAttribute format = this.formats.get(payloadType);
		if(format != null) {
			format.setParameters(parameters);
		}
	}

	public ConnectionField getConnection() {
		if(this.connection == null && this.session != null) {
			return session.getConnection();
		}
		return this.connection;
	}
	//
	public void addOtherField(OtherField of){
		otherFields.add(of);
	}
	//
	public void setConnection(ConnectionField connection) {
		this.connection = connection;
	}
	
	public ConnectionModeAttribute getConnectionMode() {
		if(this.connectionMode == null && this.session != null) {
			if(session.getConnectionMode() != null) {
				return session.getConnectionMode();
			}
		}
		return this.connectionMode;
	}
	
	public void setConnectionMode(ConnectionModeAttribute connectionMode) {
		this.connectionMode = connectionMode;
	}
	
	public RtpMapAttribute[] getFormats() {
		if(this.formats.isEmpty()) {
			return null;
		}
		return this.formats.values().toArray(new RtpMapAttribute[this.formats.size()]);
	}
	
	public int[] getPayloadTypes() {
		int[] values = new int[this.payloadTypes.size()];
		int index = 0;
		for (Integer value : this.payloadTypes) {
			values[index++] = value;
		}
		return values;
	}
	
	public RtpMapAttribute getFormat(int payloadType) {
		return this.formats.get(payloadType);
	}
	
	public RtcpAttribute getRtcp() {
		return rtcp;
	}
	
	public int getRtcpPort() {
		if(this.rtcp != null) {
			return rtcp.getPort();
		} else if(this.rtcpMux != null) {
			return this.port;
		} else {
			return this.port + 1;
		}
	}
	
	public void setRtcp(RtcpAttribute rtcp) {
		this.rtcp = rtcp;
	}
	
	public RtcpMuxAttribute getRtcpMux() {
		return rtcpMux;
	}
	
	public boolean isRtcpMux() {
		return this.rtcpMux != null;
	}
	
	public void setRtcpMux(RtcpMuxAttribute rtcpMux) {
		this.rtcpMux = rtcpMux;
	}
	
	public PacketTimeAttribute getPtime() {
		return ptime;
	}

	public void setPtime(PacketTimeAttribute ptime) {
		this.ptime = ptime;
	}

	public MaxPacketTimeAttribute getMaxptime() {
		return maxptime;
	}

	public void setMaxptime(MaxPacketTimeAttribute maxptime) {
		this.maxptime = maxptime;
	}
	
	public SsrcAttribute getSsrc() {
		return ssrc;
	}
	
	public void setSsrc(SsrcAttribute ssrc) {
		this.ssrc = ssrc;
	}
	
	public IceUfragAttribute getIceUfrag() {
		if(this.iceUfrag == null && this.session != null) {
			return this.session.getIceUfrag();
		}
		return this.iceUfrag;
	}
	
	public void setIceUfrag(IceUfragAttribute iceUfrag) {
		this.iceUfrag = iceUfrag;
	}
	
	public IcePwdAttribute getIcePwd() {
		if(this.icePwd == null && this.session != null) {
			return this.session.getIcePwd();
		}
		return this.icePwd;
	}
	
	public void setIcePwd(IcePwdAttribute icePwd) {
		this.icePwd = icePwd;
	}
	
	public CandidateAttribute[] getCandidates() {
		if(this.candidates == null || this.candidates.isEmpty()) {
			return null;
		}
		return candidates.toArray(new CandidateAttribute[this.candidates.size()]);
	}
	
	public boolean containsCandidates() {
		return this.candidates != null && !this.candidates.isEmpty();
	}
	
	public void addCandidate(CandidateAttribute candidate) {
		if(this.candidates == null) {
			this.candidates = new ArrayList<CandidateAttribute>(8);
			this.candidates.add(candidate);
		} else if(!this.candidates.contains(candidate)) {
			this.candidates.add(candidate);
		}
	}
	
	public void removeCandidate(CandidateAttribute candidate) {
		if(this.candidates != null) {
			this.candidates.remove(candidate);
		}
	}
	
	public void removeAllCandidates() {
		if(this.candidates != null) {
			this.candidates.clear();
		}
	}
	
	public boolean containsIce() {
		if(this.iceUfrag != null || this.icePwd != null || containsCandidates()) {
			return true;
		}
		return false;
	}
	
	public FingerprintAttribute getFingerprint() {
		if(this.fingerprint == null && this.session != null) {
			return session.getFingerprint();
		}
		return fingerprint;
	}
	
	public void setFingerprint(FingerprintAttribute fingerprint) {
		this.fingerprint = fingerprint;
	}
	
	public boolean containsDtls() {
		return (this.fingerprint != null);
	}
	
	public SetupAttribute getSetup() {
		if(this.setup == null && this.session != null) {
			if(this.session.getSetup() != null) {
				return this.session.getSetup();
			}
		}
		return this.setup;
	}
	
	public void setSetup(SetupAttribute setup) {
		this.setup = setup;
	}

	@Override
	public char getFieldType() {
		return FIELD_TYPE;
	}

	@Override
	public String toString() {
		// Clean builder
		this.builder.setLength(0);
		this.builder.append(BEGIN)
		        .append(this.media).append(" ")
				.append(this.port).append(" ")
				.append(this.protocol.getProfile());
		for (Integer payloadType : this.payloadTypes) {
			this.builder.append(" ").append(payloadType);
		}
		
		appendField(this.connection);
		appendField(this.connectionMode);
		appendField(this.rtcp);
		appendField(this.rtcpMux);
		appendField(this.ptime);
		appendField(this.maxptime);
		appendField(this.iceUfrag);
		appendField(this.icePwd);
		if (this.otherFields != null && !this.otherFields.isEmpty()) {
			for (OtherField of : this.otherFields) {
				appendField(of);
			}
		}
		if (this.candidates != null && !this.candidates.isEmpty()) {
			for (CandidateAttribute candidate : this.candidates) {
				appendField(candidate);
			}
		}

		if (this.formats != null && !this.formats.isEmpty()) {
			for (RtpMapAttribute format : this.formats.values()) {
				appendField(format);
			}
		}
		
		appendField(this.setup);
		appendField(this.fingerprint);
		appendField(this.ssrc);
		return this.builder.toString();
	}
	
	private void appendField(SdpField field) {
		if(field != null) {
			this.builder.append(NEWLINE).append(field.toString());
		}
	}
	
	public static boolean isValidProfile(String profile) {
		return MediaProfile.containsProfile(profile);
	}

}
