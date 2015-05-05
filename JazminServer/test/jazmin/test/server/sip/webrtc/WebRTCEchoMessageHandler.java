package jazmin.test.server.sip.webrtc;

import jazmin.codec.sdp.SessionDescription;
import jazmin.codec.sdp.SessionDescriptionParser;
import jazmin.codec.sdp.attributes.ConnectionModeAttribute;
import jazmin.codec.sdp.fields.ConnectionField;
import jazmin.codec.sdp.fields.MediaDescriptionField;
import jazmin.codec.sdp.ice.attributes.CandidateAttribute;
import jazmin.core.Jazmin;
import jazmin.server.relay.DtlsRelayChannel;
import jazmin.server.relay.EchoRelayChannel;
import jazmin.server.relay.RelayServer;
import jazmin.server.relay.TransportType;
import jazmin.server.sip.SimpleSipMessageHandler;
import jazmin.server.sip.SipContext;
import jazmin.server.sip.SipSession;
import jazmin.server.sip.SipStatusCode;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipMessage;
import jazmin.server.sip.io.sip.SipRequest;
import jazmin.server.sip.io.sip.SipResponse;
import jazmin.server.sip.io.sip.address.SipURI;
import jazmin.server.sip.io.sip.header.ContactHeader;
import jazmin.server.sip.io.sip.header.ContentLengthHeader;

/**
 * 
 * @author yama
 *
 */
public class WebRTCEchoMessageHandler extends SimpleSipMessageHandler {
	//
	public class SessionStatus {
		public DtlsRelayChannel rtpMuxRelayChannelA;
		public DtlsRelayChannel rtpMuxRelayChannelB;
		
	}
	//
	public void doInvite(SipContext ctx,SipRequest message)throws Exception{
		SipSession session=ctx.getSession();
		synchronized (session) {
			SessionStatus ss=(SessionStatus) session.getUserObject();
			RelayServer relayServer=Jazmin.getServer(RelayServer.class);
			if(ss==null){
				ss=new SessionStatus();
				session.setUserObject(ss);
				//
				ss.rtpMuxRelayChannelA=(DtlsRelayChannel) 
						relayServer.createRelayChannel(TransportType.DTLS,"rtpmuxa");
				ss.rtpMuxRelayChannelB=(DtlsRelayChannel) 
						relayServer.createRelayChannel(TransportType.DTLS,"rtpmuxb");
				//
				EchoRelayChannel echoRelayChannel=new EchoRelayChannel();
				ss.rtpMuxRelayChannelA.relayTo(echoRelayChannel);
				ss.rtpMuxRelayChannelB.relayTo(echoRelayChannel);
				ss.rtpMuxRelayChannelA.relayRtpAndRtcpTo(echoRelayChannel);
				ss.rtpMuxRelayChannelB.relayRtpAndRtcpTo(echoRelayChannel);
				
				relayServer.addChannel(echoRelayChannel);
			}
			changeSDP(message, 
					ctx.getServer().getHostAddress(),
					ss);
		}
		//
		ctx.getConnection().send(message.createResponse(SipStatusCode.SC_TRYING));
		ctx.getConnection().send(message.createResponse(SipStatusCode.SC_RINGING));
		//
		SipResponse rsp=message.createResponse(SipStatusCode.SC_OK);
		//rsp.addHeader(ContentTypeHeader.frame(Buffers.wrap("application/sdp")));
		//
		ContactHeader ch=ContactHeader.with(SipURI.frame(
				Buffers.wrap("sip:1@"+ctx.getServer().getHostAddress()))).build();
		rsp.addHeader(ch);
		rsp.setRawContent(message.getRawContent());
		//
		ctx.getConnection().send(rsp);
	}
	//
	private void changeSDP(SipMessage message,String host,SessionStatus ss)throws Exception{
		//change sdp ip address and media port to relay server
		String sdp=new String(message.getRawContent().getArray(),"utf-8");
		SessionDescription s=SessionDescriptionParser.parse(sdp);
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
				ss.rtpMuxRelayChannelA,
				audioField);
		MediaDescriptionField videoField=s.getMediaDescription("video");
		changeMediaDescriptionField(host,
				ss.rtpMuxRelayChannelB,
				videoField);
		//
		byte newSdpBytes[]=s.toBytes();
		message.setRawContent(Buffers.wrap(newSdpBytes));
		ContentLengthHeader clh=message.getContentLengthHeader();
		clh.setContentLength(newSdpBytes.length);
	}
	//
	private void changeMediaDescriptionField(String host,
			DtlsRelayChannel channel,
			MediaDescriptionField field){
		if(field==null){
			return;
		}
		if(field.getConnectionMode().getKey().equals(ConnectionModeAttribute.RECVONLY)){
			field.getConnectionMode().setMode(ConnectionModeAttribute.SENDONLY);
		}else{
			field.getConnectionMode().setMode(ConnectionModeAttribute.SENDRECV);
		}
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
	//
}