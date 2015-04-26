/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.turn.stack.TurnException;
import jazmin.server.turn.stack.listeners.AllocationRequestListener;
import jazmin.server.turn.stack.listeners.BindingRequestListener;
import jazmin.server.turn.stack.listeners.ChannelBindRequestListener;
import jazmin.server.turn.stack.listeners.ConnectRequestListener;
import jazmin.server.turn.stack.listeners.ConnectionBindRequestListener;
import jazmin.server.turn.stack.listeners.CreatePermissionRequestListener;
import jazmin.server.turn.stack.listeners.RefreshRequestListener;
import jazmin.server.turn.stack.listeners.SendIndicationListener;
import jazmin.server.turn.stack.stack.ServerChannelDataEventHandler;
import jazmin.server.turn.stack.stack.ServerPeerUdpEventHandler;
import jazmin.server.turn.stack.stack.TurnStack;

import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.socket.IceSocketWrapper;
import org.ice4j.socket.IceTcpServerSocketWrapper;
import org.ice4j.socket.IceUdpSocketWrapper;
import org.ice4j.socket.SafeCloseDatagramSocket;
import org.ice4j.stunclient.StunClient;
import org.ice4j.stunclient.StunDiscoveryReport;

/**
 * The class to run a Turn server.
 * 
 * @author Aakash Garg
 */
public class TurnServer {
	private static Logger logger = LoggerFactory.getLogger(TurnServer.class);

	private TransportAddress localAddress = null;

	private boolean started = false;

	private TurnStack turnStack = null;

	private IceUdpSocketWrapper turnUdpSocket;

	private final ServerPeerUdpEventHandler peerUdpHandler;

	private final ServerChannelDataEventHandler channelDataHandler;

	private IceSocketWrapper turnTcpServerSocket;

	public TurnServer(TransportAddress localUDPAddress) {
		this.localAddress = localUDPAddress;
		this.peerUdpHandler = new ServerPeerUdpEventHandler();
		this.channelDataHandler = new ServerChannelDataEventHandler();
		turnStack = new TurnStack(this.peerUdpHandler, this.channelDataHandler);
		this.peerUdpHandler.setTurnStack(turnStack);
		this.channelDataHandler.setTurnStack(turnStack);

		logger.info("Server initialized Waiting to be started");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// getPublicAddress();
		// getLocalAddress();
		TransportAddress localAddress = null;
		if (args.length == 2) {
			localAddress = new TransportAddress(args[0],
					Integer.valueOf(args[1]), Transport.UDP);
		} else {
			localAddress = new TransportAddress(InetAddress.getLocalHost(),
					3478, Transport.UDP);
		}
		TurnServer server = new TurnServer(localAddress);
		server.start();
		Thread.sleep(600 * 1000);
		if (server.isStarted()) {
			server.shutDown();
		}
	}

	public static void getLocalAddress() {
		System.out.print("Server public IP and port : ");
		System.out.println("127.0.0.1:3478");
	}

	public static void getPublicAddress() throws UnknownHostException,
			Exception {
		System.out.print("Server public IP and port : ");
		StunDiscoveryReport report = StunClient.getReport("stunserver.org",
				"3478", InetAddress.getLocalHost().getHostAddress(), "3478");
		System.out.println(report.getPublicAddress());

	}

	/**
	 * Function to start the server
	 * 
	 * @throws IOException
	 * @throws TurnException
	 */
	public void start() throws IOException, TurnException {
		if (localAddress == null) {
			throw new RuntimeException("Local address not initialized");
		}
		turnUdpSocket = new IceUdpSocketWrapper(new SafeCloseDatagramSocket(
				localAddress));
		/*
		 * BindingRequestListener listner = new
		 * BindingRequestListener(turnStack); listner.start();
		 */
		AllocationRequestListener allocationRequestListner = new AllocationRequestListener(
				turnStack);
		ChannelBindRequestListener channelBindRequestListener = new ChannelBindRequestListener(
				turnStack);
		ConnectionBindRequestListener connectionBindRequestListener = new ConnectionBindRequestListener(
				turnStack);
		ConnectRequestListener connectRequestListener = new ConnectRequestListener(
				turnStack);
		CreatePermissionRequestListener createPermissionRequestListener = new CreatePermissionRequestListener(
				turnStack);
		RefreshRequestListener refreshRequestListener = new RefreshRequestListener(
				turnStack);
		BindingRequestListener bindingRequestListener = new BindingRequestListener(
				turnStack);

		SendIndicationListener sendIndListener = new SendIndicationListener(
				turnStack);
		sendIndListener.setLocalAddress(localAddress);

		allocationRequestListner.start();
		channelBindRequestListener.start();
		connectionBindRequestListener.start();
		connectRequestListener.start();
		createPermissionRequestListener.start();
		refreshRequestListener.start();
		bindingRequestListener.start();

		sendIndListener.start();

		System.out.println("Address - " + localAddress.getHostAddress() + ":"
				+ localAddress.getPort());

		ServerSocket tcpServerSocket = new ServerSocket(localAddress.getPort());

		/*
		 * Agent agent = new Agent(); agent.setStunStack(this.turnStack);
		 * IceMediaStream stream = new IceMediaStream(agent,"Turn Server");
		 * Component component = new Component(Component.RTP, Transport.TCP,
		 * stream);
		 * 
		 * sock2 = new IceTcpServerSocketWrapper(mySock,component);
		 */
		System.out.println("Adding a TCP server socket - "
				+ tcpServerSocket.getLocalSocketAddress());
		/*
		 * try { Thread.sleep(5000); } catch (InterruptedException e) {
		 * System.err.println("Unable to sleep thread"); }
		 */turnTcpServerSocket = new IceTcpServerSocketWrapper(tcpServerSocket,
				this.turnStack.getComponent());

		turnStack.addSocket(turnUdpSocket);
		turnStack.addSocket(turnTcpServerSocket);
		started = true;
		logger.info("Server started, listening on " + localAddress.getAddress()
				+ ":" + localAddress.getPort());

	}

	/**
	 * function to stop the server and free resources allocated by it.
	 */
	public void shutDown() {
		logger.info("Stopping server at " + localAddress.getAddress() + ":"
				+ localAddress.getPort());
		turnStack.removeSocket(localAddress);
		turnStack = null;
		turnUdpSocket.close();
		turnUdpSocket = null;
		this.turnTcpServerSocket.close();
		this.turnTcpServerSocket = null;

		localAddress = null;
		this.started = false;
		logger.info("Server stopped");
	}

	public boolean isStarted() {
		return started;
	}
}
