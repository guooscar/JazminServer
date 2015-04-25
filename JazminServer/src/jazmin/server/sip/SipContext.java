/**
 * 
 */
package jazmin.server.sip;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipMessage;
import jazmin.server.sip.io.pkts.packet.sip.SipRequest;
import jazmin.server.sip.io.pkts.packet.sip.SipResponse;
import jazmin.server.sip.io.pkts.packet.sip.address.SipURI;
import jazmin.server.sip.io.pkts.packet.sip.header.ViaHeader;
import jazmin.server.sip.stack.Connection;
import jazmin.util.DumpIgnore;

/**
 * @author yama
 *
 */
@DumpIgnore
public class SipContext {
	Connection connection;
	SipMessage message;
	SipServer server;
	/**
	 * 
	 * @param msg
	 */
	public void proxy(final SipResponse msg) {
		final ViaHeader via = msg.getViaHeader();
		final Connection connection =connect(via.getHost().toString(),
				via.getPort());
		connection.send(msg);
	}
	//
	/**
     * Whenever we proxy a request we must also add a Via-header, which essentially says that the
     * request went "via this network address using this protocol". The {@link ViaHeader}s are used
     * for responses to find their way back the exact same path as the request took.
     * 
     * @param destination
     * @param msg
     */
    public void proxyTo(final SipURI destination, final SipRequest msg) {
        final int port = destination.getPort();
        final Connection connection = connect(
        		destination.getHost().toString(), port == -1 ? 5060 : port);

        // SIP is pretty powerful but there are a lot of little details to get things working.
        // E.g., this sample application is acting as a stateless proxy and in order to
        // correctly relay re-transmissions or e.g. CANCELs we have to make sure to always
        // generate the same branch-id of the same request. Since a CANCEL will have the same
        // branch-id as the request it cancels, we must ensure we generate the same branch-id as
        // we did when we proxied the initial INVITE. If we don't, then the cancel will not be
        // matched by the "other" side and their phone wouldn't stop ringing.
        // SO, for this example, we'll just grab the previous value and append "-abc" to it so
        // now we are relying on the upstream element to do the right thing :-)
        //
        // See section 16.11 in RFC3263 for more information.
        final Buffer otherBranch = msg.getViaHeader().getBranch();
        final Buffer myBranch = Buffers.createBuffer(otherBranch.getReadableBytes() + 7);
        otherBranch.getBytes(myBranch);
        myBranch.write((byte) 'z');
        myBranch.write((byte) ';');
        myBranch.write((byte) 'r');
        myBranch.write((byte) 'p');
        myBranch.write((byte) 'o');
        myBranch.write((byte) 'r');
        myBranch.write((byte) 't');
        
        final ViaHeader via = ViaHeader.with().host(server.getIp()).
        		port(server.getPort()).
        		transportUDP().
        		branch(myBranch).build();
        // This is how you should generate the branch parameter if you are a stateful proxy:
        // Note the ViaHeader.generateBranch()...
        // ViaHeader.with().host("10.0.1.28").port(5060).transportUDP()
        //.branch(ViaHeader.generateBranch()).build();
        msg.addHeaderFirst(via);
        connection.send(msg);
    }
	//
	public Connection connect(String ip,int port){
		return server.connect(ip, port);
	}
	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}
	/**
	 * @return the message
	 */
	public SipMessage getMessage() {
		return message;
	}
	/**
	 * @return the server
	 */
	public SipServer getServer() {
		return server;
	}
	/**
	 * 
	 */
	public SipSession getSession(){
		return server.getSession(message,true);
	}
	/**
	 * 
	 */
	public SipSession getSession(boolean create){
		return server.getSession(message,create);
	}
}
