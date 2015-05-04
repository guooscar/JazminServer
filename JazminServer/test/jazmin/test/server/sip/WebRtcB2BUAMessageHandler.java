package jazmin.test.server.sip;

import jazmin.codec.sdp.SessionDescription;
import jazmin.codec.sdp.SessionDescriptionParser;
import jazmin.codec.sdp.fields.ConnectionField;
import jazmin.codec.sdp.fields.MediaDescriptionField;
import jazmin.codec.sdp.ice.attributes.CandidateAttribute;
import jazmin.codec.sdp.rtcp.attributes.RtcpAttribute;
import jazmin.core.Jazmin;
import jazmin.server.relay.DtlsRelayChannel;
import jazmin.server.relay.RelayServer;
import jazmin.server.relay.TransportType;
import jazmin.server.sip.SipContext;
import jazmin.server.sip.SipMessageAdapter;
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
public class WebRtcB2BUAMessageHandler extends SipMessageAdapter {
	//
	public class SessionStatus {
		public DtlsRelayChannel audioRelayChannelA;
		public DtlsRelayChannel audioRtcpRelayChannelA;
		
		public DtlsRelayChannel videoRelayChannelA;
		public DtlsRelayChannel videoRtcpRelayChannelA;
		
	}
	//
	@Override
	public void handleRequest(SipContext ctx, SipRequest message)
			throws Exception {
		if (message.isInvite()) {
			doInvite(ctx,message);
			return;
		}
		//
		ctx.getConnection().send(message.createResponse(SipStatusCode.SC_OK));
	}
	//
	@Override
	public void handleResponse(SipContext ctx, SipResponse response)
			throws Exception {
		
	}
	//
	private void doInvite(SipContext ctx,SipRequest message)throws Exception{
		SipSession session=ctx.getSession();
		synchronized (session) {
			SessionStatus ss=(SessionStatus) session.getUserObject();
			RelayServer relayServer=Jazmin.getServer(RelayServer.class);
			if(ss==null){
				ss=new SessionStatus();
				session.setUserObject(ss);
				//
				ss.audioRelayChannelA=(DtlsRelayChannel) 
						relayServer.createRelayChannel(TransportType.DTLS,"audio");
				ss.audioRtcpRelayChannelA=(DtlsRelayChannel) 
						relayServer.createRelayChannel(TransportType.DTLS,"audiortcp");
				ss.videoRelayChannelA=(DtlsRelayChannel) 
						relayServer.createRelayChannel(TransportType.DTLS,"video");
				ss.videoRtcpRelayChannelA=(DtlsRelayChannel) 
						relayServer.createRelayChannel(TransportType.DTLS,"videortcp");
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
				Buffers.wrap("sip:1@192.168.1.103"))).build();
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
				ss.audioRelayChannelA,
				ss.audioRtcpRelayChannelA,
				audioField);
		MediaDescriptionField videoField=s.getMediaDescription("video");
		changeMediaDescriptionField(host,
				ss.videoRelayChannelA,
				ss.videoRtcpRelayChannelA,
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
			DtlsRelayChannel rtcpChannel,
			MediaDescriptionField field){
		if(field==null){
			return;
		}
		field.getIceUfrag().setUfrag(channel.getIceUfrag());
		field.getIcePwd().setPassword(channel.getIcePassword());
		field.setPort(channel.getLocalPort());
		RtcpAttribute ra=new RtcpAttribute(rtcpChannel.getLocalPort());
		field.setRtcp(ra);
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