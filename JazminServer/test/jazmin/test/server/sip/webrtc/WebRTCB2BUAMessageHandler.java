package jazmin.test.server.sip.webrtc;

import jazmin.codec.sdp.SessionDescription;
import jazmin.codec.sdp.SessionDescriptionParser;
import jazmin.codec.sdp.attributes.ConnectionModeAttribute;
import jazmin.codec.sdp.fields.ConnectionField;
import jazmin.codec.sdp.fields.MediaDescriptionField;
import jazmin.codec.sdp.ice.attributes.CandidateAttribute;
import jazmin.codec.sdp.rtcp.attributes.RtcpAttribute;
import jazmin.core.Jazmin;
import jazmin.server.relay.RelayServer;
import jazmin.server.relay.TransportType;
import jazmin.server.relay.udp.DtlsRelayChannel;
import jazmin.server.relay.udp.UDPUnicastRelayChannel;
import jazmin.server.sip.SipContext;
import jazmin.server.sip.SipLocationBinding;
import jazmin.server.sip.SipSession;
import jazmin.server.sip.SipStatusCode;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipMessage;
import jazmin.server.sip.io.sip.SipRequest;
import jazmin.server.sip.io.sip.SipResponse;
import jazmin.server.sip.io.sip.address.Address;
import jazmin.server.sip.io.sip.address.SipURI;
import jazmin.server.sip.io.sip.address.URI;
import jazmin.server.sip.io.sip.header.ContentLengthHeader;
import jazmin.server.sip.stack.Connection;
import jazmin.test.server.sip.b2bua.B2BUAMessageHandler;
import jazmin.util.IOUtil;

/**
 * 
 * @author yama
 *
 */
public class WebRTCB2BUAMessageHandler extends B2BUAMessageHandler {
	//
	public class SessionStatus {
		public String webRtcSDP;
		public Connection connection;
		public UDPUnicastRelayChannel sipPhoneAudioChannel;
		public UDPUnicastRelayChannel sipPhoneAudioRtcpChannel;
		public UDPUnicastRelayChannel sipPhoneVideoChannel;
		public UDPUnicastRelayChannel sipPhoneVideoRtcpChannel;
		
		public DtlsRelayChannel webrtcAudioChannel;
		public DtlsRelayChannel webrtcVideoChannel;
	}
	//
	public WebRTCB2BUAMessageHandler(){
		
	}
	//
	@Override
	public void handleResponse(SipContext ctx, SipResponse response)
			throws Exception {
		SessionStatus ss=(SessionStatus) ctx.getSession().getUserObject();
		if(ss==null){
			return;
		}
		response.popViaHeader();
		//
		if(response.isInvite()&&response.hasContent()){
			changeSDPForWebRtc(ctx, response, ss);
		}
		ss.connection.send(response);
	}
	//
	private void changeSDPForWebRtc(
			SipContext ctx,
			SipMessage message,
			SessionStatus ss)throws Exception{
		//change sdp ip address and media port to relay server
		String sdp=ss.webRtcSDP;
		SessionDescription s=SessionDescriptionParser.parse(sdp);
		String host=ctx.getServer().getPublicAddress();
		ConnectionField cf=s.getConnection();
		if(cf!=null){
			cf.setAddress(host);
		}
		ConnectionField audioConneion=s.getConnection("audio");
		if(audioConneion!=null){
			audioConneion.setAddress(host);
		}
		ConnectionField videoConneion=s.getConnection("video");
		if(videoConneion!=null){
			videoConneion.setAddress(host);
		}
		//
		s.getOrigin().setAddress(host);
		MediaDescriptionField audioField=s.getMediaDescription("audio");
		changeMediaDescriptionField(host,
				ss.webrtcAudioChannel,
				audioField);
		MediaDescriptionField videoField=s.getMediaDescription("video");
		changeMediaDescriptionField(host,
				ss.webrtcVideoChannel,
				videoField);
		//
		byte newSdpBytes[]=s.toBytes();
		message.setRawContent(Buffers.wrap(newSdpBytes));
		ContentLengthHeader clh=message.getContentLengthHeader();
		clh.setContentLength(newSdpBytes.length);
	}
	//
	private void changeMediaDescriptionField(
			String host,
			DtlsRelayChannel channel,
			MediaDescriptionField field){
		if(field==null){
			return;
		}
		if(field.getConnectionMode()!=null){
			if(field.getConnectionMode().getKey().equals(ConnectionModeAttribute.RECVONLY)){
				field.getConnectionMode().setMode(ConnectionModeAttribute.SENDONLY);
			}else{
				field.getConnectionMode().setMode(ConnectionModeAttribute.SENDRECV);
			}	
		}
		//
		field.getIceUfrag().setUfrag(channel.getIceUfrag());
		field.getIcePwd().setPassword(channel.getIcePassword());
		field.setPort(channel.getLocalPort());
		CandidateAttribute ca0 = field.getCandidates()[0];
		ca0.setAddress(host);
		ca0.setPort(channel.getLocalPort());
		field.removeAllCandidates();
		field.addCandidate(ca0);
		field.getFingerprint().setFingerprint(channel.getLocalFingerprint());
		field.getFingerprint().setHashFunction("sha-256");
		field.getSetup().setValue("passive");
	}
	//--------------------------------------------------------------------------
	//
	public void doInvite(SipContext ctx,SipRequest message)throws Exception{
		Address toAddress=message.getToHeader().getAddress();
		URI toURI=toAddress.getURI();
		SipLocationBinding toBinding=ctx.getServer().getLocationBinding((SipURI) toURI);
		if(toBinding==null){
			SipMessage notFoundMsg=message.createResponse(SipStatusCode.SC_NOT_FOUND);
			ctx.getConnection().send(notFoundMsg);
			return;
		}
		RelayServer relayServer=Jazmin.getServer(RelayServer.class);
		SipSession session=ctx.getSession();
		synchronized (session) {
			SessionStatus ss=(SessionStatus) session.getUserObject();
			if(ss==null){
				ss=new SessionStatus();
				ss.connection=ctx.getConnection();
				String sdp=new String(message.getRawContent().getArray(),"utf-8");
				ss.webRtcSDP=sdp;
				session.setUserObject(ss);
				//
				ss.webrtcAudioChannel=(DtlsRelayChannel) 
						relayServer.createRelayChannel(TransportType.DTLS,"webrtcaAudio");
				ss.webrtcVideoChannel=(DtlsRelayChannel) 
						relayServer.createRelayChannel(TransportType.DTLS,"webrtcaVideo");
				//
				ss.sipPhoneAudioChannel=(UDPUnicastRelayChannel) 
						relayServer.createRelayChannel(TransportType.UDP_UNICAST,"sipAudio");
				ss.sipPhoneAudioRtcpChannel=(UDPUnicastRelayChannel) 
						relayServer.createRelayChannel(TransportType.UDP_UNICAST,"sipAudioRtcp");
				//
				ss.sipPhoneVideoChannel=(UDPUnicastRelayChannel) 
						relayServer.createRelayChannel(TransportType.UDP_UNICAST,"sipVideo");
				ss.sipPhoneVideoRtcpChannel=(UDPUnicastRelayChannel) 
						relayServer.createRelayChannel(TransportType.UDP_UNICAST,"sipVideoRtcp");
				//
				ss.webrtcAudioChannel.bidiRelay(ss.sipPhoneAudioChannel);
				ss.webrtcAudioChannel.bidiRelay(ss.sipPhoneAudioRtcpChannel);
				ss.webrtcVideoChannel.bidiRelay(ss.sipPhoneVideoChannel);
				ss.webrtcVideoChannel.bidiRelay(ss.sipPhoneVideoRtcpChannel);
				//
				ss.webrtcAudioChannel.relayRtpTo(ss.sipPhoneAudioChannel);
				ss.webrtcAudioChannel.relayRtcpTo(ss.sipPhoneAudioRtcpChannel);
				ss.webrtcVideoChannel.relayRtpTo(ss.sipPhoneVideoChannel);
				ss.webrtcVideoChannel.relayRtcpTo(ss.sipPhoneVideoRtcpChannel);	
			}
			//
			changeSDPForSipPhone(ctx,message,ss);
			ctx.getServer().proxyTo(toBinding.getConnection(),message);
		}
		dumpStore(ctx);
	}
	//
	private void changeSDPForSipPhone(
			SipContext ctx,
			SipRequest message,
			SessionStatus ss) throws Exception{
		
		String sdp=IOUtil.getContent(getClass().getResourceAsStream("sipphone.sdp"));
		//
		SessionDescription s=SessionDescriptionParser.parse(sdp);
		ConnectionField cf=s.getConnection();
		String host=ctx.getServer().getPublicAddress();
		if(cf!=null){
			cf.setAddress(host);
		}
		ConnectionField audioConneion=s.getConnection("audio");
		if(audioConneion!=null){
			audioConneion.setAddress(host);
		}
		ConnectionField videoConneion=s.getConnection("video");
		if(videoConneion!=null){
			videoConneion.setAddress(host);
		}
		//
		s.getOrigin().setAddress(host);
		MediaDescriptionField audioField=s.getMediaDescription("audio");
		if(audioField!=null){
			audioField.setPort(ss.sipPhoneAudioChannel.getLocalPort());
			RtcpAttribute ra=new RtcpAttribute(ss.sipPhoneAudioRtcpChannel.getLocalPort());
			audioField.setRtcp(ra);
		}
		MediaDescriptionField videoField=s.getMediaDescription("video");
		if(videoField!=null){
			videoField.setPort(ss.sipPhoneVideoChannel.getLocalPort());
			RtcpAttribute ra=new RtcpAttribute(ss.sipPhoneVideoRtcpChannel.getLocalPort());
			videoField.setRtcp(ra);
		}
		byte newSdpBytes[]=s.toBytes();
		message.setRawContent(Buffers.wrap(newSdpBytes));
	}
}