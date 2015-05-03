package jazmin.test.server.sip;

import java.io.File;

import jazmin.codec.sdp.SessionDescription;
import jazmin.codec.sdp.SessionDescriptionParser;
import jazmin.codec.sdp.fields.ConnectionField;
import jazmin.codec.sdp.fields.MediaDescriptionField;
import jazmin.codec.sdp.ice.attributes.CandidateAttribute;
import jazmin.server.sip.SipContext;
import jazmin.server.sip.SipMessageAdapter;
import jazmin.server.sip.SipStatusCode;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipMessage;
import jazmin.server.sip.io.sip.SipRequest;
import jazmin.server.sip.io.sip.SipResponse;
import jazmin.server.sip.io.sip.address.SipURI;
import jazmin.server.sip.io.sip.header.ContactHeader;
import jazmin.server.sip.io.sip.header.ContentLengthHeader;
import jazmin.util.FileUtil;

/**
 * 
 * @author yama
 *
 */
public class WebRtcB2BUAMessageHandler extends SipMessageAdapter {
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
		changeSDP(message, 
					ctx.getServer().getHostAddress(),
					5684,
					5685);
		//
		ctx.getConnection().send(message.createResponse(SipStatusCode.SC_TRYING));
		ctx.getConnection().send(message.createResponse(SipStatusCode.SC_RINGING));
		//
		SipResponse rsp=message.createResponse(SipStatusCode.SC_OK);
		//rsp.addHeader(ContentTypeHeader.frame(Buffers.wrap("application/sdp")));
		//
		ContactHeader ch=ContactHeader.with(SipURI.frame(Buffers.wrap("sip:1@192.168.3.103"))).build();
		rsp.addHeader(ch);
		String sdp=FileUtil.getContent(new File("sdp"));
		rsp.setRawContent(Buffers.wrap(sdp));
		//
		ctx.getConnection().send(rsp);
	}
	//
	private void changeSDP(SipMessage message,String host,int audioPort,int videoPort)throws Exception{
		//if(true){
		//	return;
		//}
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
		if(audioField!=null){
			for(CandidateAttribute ca:audioField.getCandidates()){
				ca.setAddress(host);
				ca.setPort(audioPort);
			}
			audioField.setPort(audioPort);
		}
		MediaDescriptionField videoField=s.getMediaDescription("video");
		if(videoField!=null){
			for(CandidateAttribute ca:videoField.getCandidates()){
				ca.setAddress(host);
				ca.setPort(videoPort);
			}
			videoField.setPort(videoPort);
		}
		//
		
		//
		byte newSdpBytes[]=s.toBytes();
		message.setRawContent(Buffers.wrap(newSdpBytes));
		ContentLengthHeader clh=message.getContentLengthHeader();
		clh.setContentLength(newSdpBytes.length);
	}

}