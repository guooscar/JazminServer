package jazmin.test.server.sip.b2bua;

import jazmin.codec.sdp.SessionDescription;
import jazmin.codec.sdp.SessionDescriptionParser;
import jazmin.codec.sdp.fields.ConnectionField;
import jazmin.codec.sdp.fields.MediaDescriptionField;
import jazmin.core.Jazmin;
import jazmin.server.relay.EchoRelayChannel;
import jazmin.server.relay.RelayServer;
import jazmin.server.relay.TransportType;
import jazmin.server.relay.UDPRelayChannel;
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
import jazmin.server.sip.io.sip.header.ContentTypeHeader;

/**
 * 
 * @author yama
 *
 */
public class SipEchoMessageHandler extends SimpleSipMessageHandler {
	//
	public class SessionStatus {
		public UDPRelayChannel rtpMuxRelayChannelA;
		public UDPRelayChannel rtpMuxRelayChannelB;
		
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
				ss.rtpMuxRelayChannelA=(UDPRelayChannel) 
						relayServer.createRelayChannel(TransportType.UDP,"rtpmuxa");
				ss.rtpMuxRelayChannelB=(UDPRelayChannel) 
						relayServer.createRelayChannel(TransportType.UDP,"rtpmuxb");
				//
				EchoRelayChannel echoRelayChannel=new EchoRelayChannel(relayServer);
				ss.rtpMuxRelayChannelA.relayTo(echoRelayChannel);
				ss.rtpMuxRelayChannelB.relayTo(echoRelayChannel);
				
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
		rsp.setHeader(ContentTypeHeader.frame(Buffers.wrap("application/sdp")));
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
		MediaDescriptionField audioMF=s.getMediaDescription("audio");
		if(audioMF!=null){
			audioMF.setPort(ss.rtpMuxRelayChannelA.getLocalPort());
		}
		MediaDescriptionField videoMF=s.getMediaDescription("video");
		if(videoMF!=null){
			videoMF.setPort(ss.rtpMuxRelayChannelB.getLocalPort());
		}
		//
		s.getOrigin().setAddress(host);
		byte newSdpBytes[]=s.toBytes();
		message.setRawContent(Buffers.wrap(newSdpBytes));
		ContentLengthHeader clh=message.getContentLengthHeader();
		clh.setContentLength(newSdpBytes.length);
	}

}