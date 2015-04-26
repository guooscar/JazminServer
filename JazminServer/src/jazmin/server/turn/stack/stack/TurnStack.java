/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.stack;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.turn.stack.TurnStackProperties;
import jazmin.server.turn.stack.listeners.PeerTcpConnectEventListner;
import jazmin.server.turn.stack.socket.IceTcpEventizedServerSockerWrapper;

import org.ice4j.StunMessageEvent;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.attribute.Attribute;
import org.ice4j.ice.Agent;
import org.ice4j.ice.Component;
import org.ice4j.ice.IceMediaStream;
import org.ice4j.message.Message;
import org.ice4j.security.CredentialsManager;
import org.ice4j.security.LongTermCredential;
import org.ice4j.security.LongTermCredentialSession;
import org.ice4j.socket.IceSocketWrapper;
import org.ice4j.socket.IceUdpSocketWrapper;
import org.ice4j.socket.SafeCloseDatagramSocket;
import org.ice4j.stack.ChannelDataEventHandler;
import org.ice4j.stack.PeerUdpMessageEventHandler;
import org.ice4j.stack.StunStack;

/**
 * The entry point to the TurnServer stack. The class is used to start, stop and
 * configure the stack.
 * 
 * @author Aakash Garg
 */
public class TurnStack extends StunStack {

	/**
	 * The <tt>Logger</tt> used by the <tt>turnStack</tt> class and its
	 * instances for logging output.
	 */
	private static final Logger logger = LoggerFactory.getLogger(TurnStack.class);

	/**
	 * The maximum no of Allocations per TurnStack.
	 */
	public static final int MAX_ALLOCATIONS = 500;

	/**
	 * To track the portNo used.
	 */
	// private static int nextPortNo = 49152;
	private static int nextPortNo = 15000;

	/**
	 * Represents the Allocations stored for Server Side.
	 */
	private final HashMap<FiveTuple, Allocation> serverAllocations = new HashMap<FiveTuple, Allocation>();

	/**
	 * Contains the mapping of relayAddress to Allocation.
	 */
	private final HashMap<TransportAddress, Allocation> serverRelayAllocationMap = new HashMap<TransportAddress, Allocation>();

	/**
	 * RelayAddress reserved by server.
	 */
	private final HashSet<TransportAddress> reservedAddress = new HashSet<TransportAddress>();

	/**
	 * Represents the Allocations stored for Client Side.
	 */
	private final HashMap<FiveTuple, Allocation> clientAllocations = new HashMap<FiveTuple, Allocation>();

	/**
	 * Maps one-to-one from Data Connection to Connection Id.
	 */
	private final HashMap<FiveTuple, Integer> dataConnToConnIdMap = new HashMap<FiveTuple, Integer>();

	/**
	 * Maps one-to-one from Peer TCP Connection to Connection Id.
	 */
	private final HashMap<FiveTuple, Integer> peerConnToConnIdMap = new HashMap<FiveTuple, Integer>();

	/**
	 * Maps many-to-one from Connection Id to Allocation for where
	 * ConnectionBind Request has been received for Connection ID .
	 */
	private final HashMap<Integer, Allocation> connIdToAllocMap = new HashMap<Integer, Allocation>();

	/**
	 * Contains unAcknowledged Connection Id. Every element will expire after
	 * min of 30 sec.
	 */
	private final HashSet<Integer> unAcknowledgedConnId = new HashSet<Integer>();

	/**
	 * The <tt>Thread</tt> which expires the <tt>TurnServerAllocation</tt>s of
	 * this <tt>TurnStack</tt> and removes them from {@link #serverAllocations}
	 * .
	 */
	private Thread serverAllocationExpireThread;

	/**
	 * Indicates that if the don't fragment is support or not.
	 */
	private static final boolean dontFragmentSupported = false;

	/**
	 * Component variable.
	 */
	private Component component;

	/**
	 * Boolean to allow or disallow TCP messages. Default is allowed.
	 */
	private boolean tcpAllowed = true;

	/**
	 * Boolean to allow or disallow UDP messages. Default is allowed.
	 */
	private boolean udpAllowed = true;

	/**
	 * Default Constructor. Initializes the NetAccessManager and
	 */
	public TurnStack() {
		super();
		initCredentials();
	}

	/**
	 * Parameterized constructor for TurnStack.
	 * 
	 * @param peerUdpMessageEventHandler
	 *            the PeerUdpMessageEventHandler for this turnStack.
	 * @param channelDataEventHandler
	 *            the ChannelDataEventHandler for this turnStack.
	 */
	public TurnStack(PeerUdpMessageEventHandler peerUdpMessageEventHandler,
			ChannelDataEventHandler channelDataEventHandler) {
		super(peerUdpMessageEventHandler, channelDataEventHandler);
		initCredentials();
	}

	/**
	 * Called to notify this provider for an incoming message. method overridden
	 * to modify the logic of the Turn Stack.
	 * 
	 * @param ev
	 *            the event object that contains the new message.
	 */
	@Override
	public void handleMessageEvent(StunMessageEvent ev) {
		Message msg = ev.getMessage();
		if (logger.isDebugEnabled()) {
			logger.debug("Received an Event." + ev.getTransactionID());
		}
		if (!TurnStack.isTurnMessage(msg)) {
			logger.warn("Ignored a non-TURN message!");
			return;
		} else {
			removeUsernameIntegrityFromBinding(ev.getMessage());
			super.handleMessageEvent(ev);
			return;
		}

		/*
		 * logger.setLevel(Level.FINEST); if (logger.isLoggable(Level.FINEST)) {
		 * logger.finest("Received a message on " + ev.getLocalAddress() +
		 * " of type:" + (int) msg.getMessageType()); }
		 * 
		 * // request if (msg instanceof Request) { TransactionID serverTid =
		 * ev.getTransactionID(); logger.finer("parsing request : "+serverTid);
		 * TurnServerTransaction sTran = (TurnServerTransaction)
		 * getServerTransaction(serverTid);
		 * 
		 * if (sTran != null) { // requests from this transaction have already
		 * been seen // retransmit the response if there was any
		 * logger.finest("found an existing transaction");
		 * 
		 * try { sTran.retransmitResponse();
		 * logger.finest("Response retransmitted"); } catch (Exception ex) { //
		 * we couldn't really do anything here .. apart from logging logger.log(
		 * Level.WARNING, "Failed to retransmit a Turn response", ex); }
		 * 
		 * if (!Boolean
		 * .getBoolean(StackProperties.PROPAGATE_RECEIVED_RETRANSMISSIONS)) {
		 * return; } } else { logger.finest("existing transaction not found");
		 * sTran = new TurnServerTransaction(this, serverTid,
		 * ev.getLocalAddress(), ev.getRemoteAddress());
		 * 
		 * // if there is an OOM error here, it will lead to //
		 * NetAccessManager.handleFatalError that will stop the //
		 * MessageProcessor thread and restart it that will lead again // to an
		 * OOM error and so on... So stop here right now try { sTran.start(); }
		 * catch (OutOfMemoryError t) {
		 * logger.info("Turn transaction thread start failed:" + t); return; }
		 * startNewServerTransactionThread( serverTid, sTran); }
		 * 
		 * // validate attributes that need validation. try { //
		 * validateRequestAttributes(ev); } catch (Exception exc) { //
		 * validation failed. log get lost. logger.log( Level.FINE,
		 * "Failed to validate msg: " + ev, exc); return; }
		 * 
		 * try { fireMessageEventFormEventDispatcher(ev); } catch (Throwable t)
		 * { Response error;
		 * 
		 * logger.log( Level.INFO, "Received an invalid request.", t); Throwable
		 * cause = t.getCause();
		 * 
		 * if (((t instanceof StunException) && ((StunException) t) .getID() ==
		 * StunException.TRANSACTION_ALREADY_ANSWERED) || ((cause instanceof
		 * StunException) && ((StunException) cause) .getID() ==
		 * StunException.TRANSACTION_ALREADY_ANSWERED)) { // do not try to send
		 * an error response since we will // get another
		 * TRANSACTION_ALREADY_ANSWERED return; }
		 * 
		 * if (t instanceof IllegalArgumentException) { error =
		 * MessageFactory.createBindingErrorResponse(
		 * ErrorCodeAttribute.BAD_REQUEST, t.getMessage()); } else { error =
		 * MessageFactory.createBindingErrorResponse(
		 * ErrorCodeAttribute.SERVER_ERROR,
		 * "Oops! Something went wrong on our side :("); }
		 * 
		 * try { sendResponse( serverTid.getBytes(), error,
		 * ev.getLocalAddress(), ev.getRemoteAddress()); } catch (Exception exc)
		 * { logger.log( Level.FINE, "Couldn't send a server error response",
		 * exc); } } } // response else if (msg instanceof Response) {
		 * logger.finer("Parsing response"); TransactionID tid =
		 * ev.getTransactionID(); StunClientTransaction tran =
		 * removeTransactionFromClientTransactions(tid); if (tran != null) {
		 * tran.handleResponse(ev); } else { // do nothing - just drop the
		 * phantom response. logger
		 * .fine("Dropped response - no matching client tran found for" +
		 * " tid " + tid + "\n"); } } // indication else if (msg instanceof
		 * Indication) { logger.finer("Dispatching a Indication.");
		 * fireMessageEventFormEventDispatcher(ev); }
		 */}

	/**
	 * Method to know if the Don't fragment is supported.
	 * 
	 * @return true if supported else false.
	 */
	public static boolean isDontfragmentsupported() {
		return dontFragmentSupported;
	}

	/**
	 * Returns the Allocation with the specified <tt>fiveTuple</tt> or
	 * <tt>null</tt> if no such Allocation exists.
	 * 
	 * @param fiveTuple
	 *            the fiveTuple of the Allocation we are looking for.
	 * 
	 * @return the {@link Allocation} we are looking for.
	 */
	public Allocation getServerAllocation(FiveTuple fiveTuple) {
		Allocation allocation = null;

		synchronized (this.serverAllocations) {
			allocation = this.serverAllocations.get(fiveTuple);
		}
		/*
		 * If a Allocation is expired, do not return it. It will be removed from
		 * serverAllocations soon.
		 */
		if ((allocation != null) && allocation.isExpired())
			allocation = null;
		return allocation;
	}

	/**
	 * Returns the Allocation with the specified <tt>fiveTuple</tt> or
	 * <tt>null</tt> if no such Allocation exists.
	 * 
	 * @param fiveTuple
	 *            the fiveTuple of the Allocation we are looking for.
	 * 
	 * @return the {@link Allocation} we are looking for.
	 */

	public Allocation getClientAllocation(FiveTuple fiveTuple) {
		Allocation allocation;

		synchronized (clientAllocations) {
			allocation = clientAllocations.get(fiveTuple);
		}
		/*
		 * If a Allocation is expired, do not return it. It will be removed from
		 * serverAllocations soon.
		 */
		if ((allocation != null) && allocation.isExpired())
			allocation = null;
		return allocation;
	}

	/**
	 * Determines if more allocations can be added to this TurnStack.
	 * 
	 * @return true if no of allocations are less than maximum allowed
	 *         allocations per TurnStack.
	 */
	public boolean canHaveMoreAllocations() {
		return (this.serverAllocations.size() < MAX_ALLOCATIONS);
	}

	/**
	 * Adds a new server allocation to this TurnStack.
	 * 
	 * @param allocation
	 *            the allocation to be added to this TurnStack.
	 */
	public synchronized void addNewServerAllocation(Allocation allocation) {
		synchronized (this.serverAllocations) {
			this.serverAllocations.put(allocation.getFiveTuple(), allocation);
			IceSocketWrapper sock;
			if (true) { // check if meanwhile other thread has put the same
						// allocation.
				try {
					logger.debug("Adding a new Socket for : "
							+ allocation.getRelayAddress());
					if (allocation.getRelayAddress().getTransport() == Transport.UDP) {
						sock = new IceUdpSocketWrapper(
								new SafeCloseDatagramSocket(
										allocation.getRelayAddress()));
					} else {
						IceTcpEventizedServerSockerWrapper mySock2 = new IceTcpEventizedServerSockerWrapper(
								new ServerSocket(allocation.getRelayAddress()
										.getPort()), this.getComponent());
						PeerTcpConnectEventListner listener = new PeerTcpConnectEventListner(
								this);
						mySock2.setEventListener(listener);
						sock = mySock2;
						/*
						 * sock = new IceTcpServerSocketWrapper(new
						 * ServerSocket(allocation
						 * .getRelayAddress().getPort()),this.getComponent());
						 */}
					this.addSocket(sock);
					logger.debug("Added a new Socket for : "
							+ allocation.getRelayAddress());
					try {
						allocation.start();
					} catch (Exception e) {
						logger.catching(e);
					}
				} catch (Exception e) {
					logger.catching(e);
				} 
			}
			this.serverRelayAllocationMap.put(allocation.getRelayAddress(),
					allocation);
			maybeStartServerAllocationExpireThread();
		}
	}

	/**
	 * Gets the allocation corresponding to the relay address.
	 * 
	 * @param relayAddress
	 *            the relayAddress for which to find allocation.
	 * @return the Allocation corresponding to relayAddress.
	 */
	public Allocation getServerAllocation(TransportAddress relayAddress) {
		return this.serverRelayAllocationMap.get(relayAddress);
	}

	/**
	 * Function to check if given IP is allowed for peer address.s
	 * 
	 * @param peerAddr
	 * @return
	 */
	public static boolean isIPAllowed(TransportAddress peerAddr) {
		String ip = peerAddr.getHostAddress();
		int portNo = peerAddr.getPort();
		// TODO : logic for validating the invalid IP address.
		return true;
	}

	/**
	 * Reserves a port for future use for Reservation-token.
	 * 
	 * @param reserAddr
	 *            the address to be reserved.
	 * @return false if it is already reserved, else true.
	 */
	public boolean reservePort(TransportAddress reserAddr) {
		if (this.reservedAddress.contains(reserAddr)) {
			return false;
		} else {
			this.reservedAddress.add(reserAddr);
			return true;
		}
	}

	/**
	 * Function to get new Relay address. TODO : It has to be replaced with
	 * jitsi api.
	 * 
	 * @param evenCompulsary
	 * @return a new RelayAddress
	 */
	public TransportAddress getNewRelayAddress(boolean evenCompulsary,
			Transport transport) {
		InetAddress ipAddress = null;
		try {
			ipAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		TransportAddress possibleAddr = new TransportAddress(ipAddress,
				nextPortNo++, transport);
		int diff = evenCompulsary ? 2 : 1;
		nextPortNo += (evenCompulsary && (nextPortNo % 2) == 0) ? 0 : 1;
		while (this.reservedAddress.contains(possibleAddr)
				&& nextPortNo < 65535) {
			nextPortNo += diff;
			possibleAddr = new TransportAddress(ipAddress, nextPortNo++,
					Transport.UDP);
		}
		return possibleAddr;
	}

	/**
	 * Adds a new ConnectionId for the specified peerAddress and for the
	 * specified allocation.
	 * 
	 * @param connectionId
	 *            the connectionId created.
	 * @param peerAddress
	 *            the peerAddress who initiated the TCP connection on the relay
	 *            address of the Allocation.
	 * @param allocation
	 *            the allocation corresponding to the relay address on which the
	 *            connect request is received.
	 */
	public void addUnAcknowlededConnectionId(int connectionId,
			TransportAddress peerAddress, Allocation allocation) {
		FiveTuple peerTuple = new FiveTuple(peerAddress,
				allocation.getRelayAddress(), Transport.TCP);
		this.unAcknowledgedConnId.add(connectionId);
		this.peerConnToConnIdMap.put(peerTuple, connectionId);
		this.connIdToAllocMap.put(connectionId, allocation);
		allocation.addPeerTCPConnection(connectionId, peerTuple);
		logger.debug("Adding connectionId-" + connectionId + " for peerTuple-"
				+ peerTuple + " at allocation-" + allocation);
	}

	/**
	 * Acknowledges the ConnectionID associated with the specified client data
	 * connection.
	 * 
	 * @param connectionId
	 *            the connectionId associated with the data connection.
	 * @param clientDataConnectionTuple
	 *            the fiveTuple of the data connection.
	 */
	public void acknowledgeConnectionId(int connectionId,
			FiveTuple clientDataConnectionTuple) {
		if (!this.unAcknowledgedConnId.contains(connectionId)) {
			throw new IllegalArgumentException("No such connectionId:"
					+ connectionId + " exists");
		} else {
			this.unAcknowledgedConnId.remove(connectionId);
			this.dataConnToConnIdMap.put(clientDataConnectionTuple,
					connectionId);
			Allocation allocation = this.connIdToAllocMap.get(connectionId);
			allocation.addDataConnection(connectionId,
					clientDataConnectionTuple);
			logger.debug("Acknowledging connectiodId-" + connectionId
					+ " for client data conn-" + clientDataConnectionTuple);
		}
	}

	/**
	 * Determines if the given ConnectionId is acknowledged or not.
	 * 
	 * @param connectionID
	 *            the connectionId to check.
	 * @return true if the specified connectionID is acknowledged, else false.
	 */
	public boolean isUnacknowledged(int connectionID) {
		return this.unAcknowledgedConnId.contains(connectionID);
	}

	/**
	 * Returns the Connection associated with the specified peerFiveTuple.
	 * 
	 * @param peerFiveTuple
	 *            the peerFiveTuple for which to get the ConnectionID.
	 * @return connectionID associated with the specified peerFiveTuple.
	 */
	public int getConnectionIdForPeer(FiveTuple peerFiveTuple) {
		return this.peerConnToConnIdMap.get(peerFiveTuple);
	}

	/**
	 * Returns the ConnectionID associated with the specified
	 * 
	 * @param dataConnTuple
	 *            the five tuple of the data connection.
	 * @return the connectionId associated with the given data connection if
	 *         exists.
	 */
	public int getConnectionIdForDataConn(FiveTuple dataConnTuple) {
		return this.dataConnToConnIdMap.get(dataConnTuple);
	}

	/**
	 * Initialises and starts {@link #serverAllocationExpireThread} if
	 * necessary.
	 */
	public void maybeStartServerAllocationExpireThread() {
		synchronized (serverAllocations) {
			if (!serverAllocations.isEmpty()
					&& (serverAllocationExpireThread == null)) {
				Thread t = new Thread() {
					@Override
					public void run() {
						runInServerAllocationExpireThread();
					}
				};

				t.setDaemon(true);
				t.setName(getClass().getName()
						+ ".serverAllocationExpireThread");

				boolean started = false;

				serverAllocationExpireThread = t;
				try {
					t.start();
					started = true;
				} finally {
					if (!started && (serverAllocationExpireThread == t))
						serverAllocationExpireThread = null;
				}
			}
		}
	}

	/**
	 * Runs in {@link #serverAllocationExpireThread} and expires the
	 * <tt>Allocation</tt>s of this <tt>TurnStack</tt> and removes them from
	 * {@link #serverAllocations}.
	 */
	private void runInServerAllocationExpireThread() {
		try {
			long idleStartTime = -1;

			do {
				synchronized (serverAllocations) {
					try {
						serverAllocations.wait(Allocation.DEFAULT_LIFETIME);
					} catch (InterruptedException ie) {
					}

					/*
					 * Is the current Thread still designated to expire the
					 * Allocations of this TurnStack?
					 */
					if (Thread.currentThread() != serverAllocationExpireThread)
						break;

					long now = System.currentTimeMillis();

					/*
					 * Has the current Thread been idle long enough to merit
					 * disposing of it?
					 */
					if (serverAllocations.isEmpty()) {
						if (idleStartTime == -1)
							idleStartTime = now;
						else if (now - idleStartTime > 60 * 1000)
							break;
					} else {
						// Expire the Allocations of this TurnStack.

						idleStartTime = -1;

						for (Iterator<Allocation> i = serverAllocations
								.values().iterator(); i.hasNext();) {
							Allocation allocation = i.next();

							if (allocation == null) {
								i.remove();
							} else if (allocation.isExpired(now)) {
								logger.debug("allocation " + allocation
										+ " expired");
								i.remove();
								allocation.expire();
							}
						}
					}
				}
			} while (true);
		} finally {
			synchronized (serverAllocations) {
				if (serverAllocationExpireThread == Thread.currentThread())
					serverAllocationExpireThread = null;
				/*
				 * If serverAllocationExpireThread dies unexpectedly and yet it
				 * is still necessary, resurrect it.
				 */
				if (serverAllocationExpireThread == null)
					maybeStartServerAllocationExpireThread();
			}
		}
	}

	/**
	 * Method to check if the given message method is of Turn method.
	 * 
	 * @param message
	 * @return true if message is of Turn method else false.
	 */
	public static boolean isTurnMessage(Message message) {
		char method = message.getMessageType();
		method = (char) (method & 0xfeef); // ignore the class
		if(logger.isDebugEnabled()){
			logger.debug("method extracted from " + (int) message.getMessageType()
				+ " is : " + (int) method);
		}
		boolean isTurnMessage = false;
		switch (method) {
		// Turn Specific Methods
		case Message.TURN_METHOD_ALLOCATE:
		case Message.TURN_METHOD_CHANNELBIND:
		case Message.TURN_METHOD_CREATEPERMISSION:
		case Message.TURN_METHOD_DATA:
		case Message.TURN_METHOD_REFRESH:
		case Message.TURN_METHOD_SEND:
			// Turn TCP support Methods
		case Message.TURN_METHOD_CONNECT:
		case Message.TURN_METHOD_CONNECTION_BIND:
		case Message.TURN_METHOD_CONNECTION_ATTEMPT:
		case Message.STUN_METHOD_BINDING:
			isTurnMessage = true;
			break;
		default:
			isTurnMessage = false;
		}
		return isTurnMessage;

	}

	/**
	 * Removes the username and Message Integrity attribute form Binding
	 * messages only.
	 * 
	 * @param msg
	 *            the Binding message from which the attribute is to be removed.
	 */
	private void removeUsernameIntegrityFromBinding(Message msg) {
		if ((msg.getMessageType() & 0xfeef) != Message.STUN_METHOD_BINDING) {
			return;
		}
		if (msg.containsAttribute(Attribute.USERNAME)) {
			msg.removeAttribute(Attribute.USERNAME);
		}
		if (msg.containsAttribute(Attribute.MESSAGE_INTEGRITY)) {
			msg.removeAttribute(Attribute.MESSAGE_INTEGRITY);
		}
	}

	/**
	 * Initializes the turnstack with the registered users with username and
	 * their corresponding key.
	 */
	public void initCredentials() {
		String fileName = TurnStackProperties.DEFAULT_ACCOUNTS_FILE;
		FileReader fr;
		try {
			fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			CredentialsManager cm = this.getCredentialsManager();
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] tok = line.split(":");
				LongTermCredential ltc = new LongTermCredential(
						tok[0].getBytes("UTF-8"), tok[1].getBytes("UTF-8"));
				System.out.println("Adding - " + new String(ltc.getUsername())
						+ ":" + new String(ltc.getPassword()));
				// TODO replace with REALM instead of DEFAULT_REALM.
				LongTermCredentialSession ltcs = new LongTermCredentialSession(
						ltc,
						TurnStackProperties.DEFAULT_REALM.getBytes("UTF-8"));
				cm.registerAuthority(ltcs);
			}
			fr.close();
			br.close();
		} catch (FileNotFoundException fnfe) {
			logger.debug("File not found.");
		} catch (IOException ioe) {
			logger.debug("Unable to read file.");
		}

	}

	/**
	 * Gets the component as RTP with TCP as transport also agent's stunStack as
	 * this TurnStack.
	 * 
	 * @return component.
	 */
	public Component getComponent() {
		if (this.component == null) {
			Agent agent = new Agent();
			agent.setStunStack(this);
			IceMediaStream stream = new IceMediaStream(agent, "Turn Server");
			this.component = new Component(Component.RTP, Transport.TCP, stream);
		}
		return this.component;
	}

	/**
	 * Determines if the UDP messages are allowed in TURN server.
	 * 
	 * @return true if UDP is allowed else false.
	 */
	public boolean isUDPAllowed() {
		return this.udpAllowed;
	}

	/**
	 * Sets the udpAllowed variable to enable disable UDP messages.
	 * 
	 * @param udpAllowed
	 *            the boolean value to allow or disallow UDP messages.
	 */
	public void setUDPAllowed(boolean udpAllowed) {
		this.udpAllowed = udpAllowed;
	}

	/**
	 * Determines if the TCP messages are allowed in TURN server.
	 * 
	 * @return true if TCP is allowed else false.
	 */
	public boolean isTCPAllowed() {
		return this.tcpAllowed;
	}

	/**
	 * Sets the tcpAllowed variable to enable disable TCP messages.
	 * 
	 * @param tcpAllowed
	 *            the boolean value to allow or disallow TCP messages.
	 */
	public void setTCPAllowed(boolean tcpAllowed) {
		this.tcpAllowed = tcpAllowed;
	}

	/**
	 * Gets the allocation for the specified connectionID no.
	 * 
	 * @param connectionId
	 *            the connectionID for which to get the allocation.
	 * @return Allocation corresponding to specified connectionID or nul if not
	 *         found.
	 */
	public Allocation getAllocationFromConnectionId(int connectionId) {
		return this.connIdToAllocMap.get(connectionId);
	}

}
