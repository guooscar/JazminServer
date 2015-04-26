package jazmin.server.stun;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.List;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.stun.stack.attribute.ChangeRequest;
import jazmin.server.stun.stack.attribute.ChangedAddress;
import jazmin.server.stun.stack.attribute.MappedAddress;
import jazmin.server.stun.stack.attribute.MessageAttributeInterface.MessageAttributeType;
import jazmin.server.stun.stack.attribute.ResponseAddress;
import jazmin.server.stun.stack.attribute.SourceAddress;
import jazmin.server.stun.stack.attribute.UnknownAttribute;
import jazmin.server.stun.stack.header.MessageHeader;
import jazmin.server.stun.stack.header.MessageHeaderInterface.MessageHeaderType;
import jazmin.server.stun.stack.util.Address;

/**
 * Inner class to handle incoming packets and react accordingly. I decided not
 * to start a thread for every received Binding Request, because the time
 * required to receive a Binding Request, parse it, generate a Binding Response
 * and send it varies only between 2 and 4 milliseconds. This amount of time is
 * small enough so that no extra thread is needed for incoming Binding Request.
 **/
public class StunServerReceiverThread extends Thread {
	private DatagramSocket receiverSocket;
	private DatagramSocket changedPort;
	private DatagramSocket changedIP;
	private DatagramSocket changedPortIP;
	volatile boolean isStop=false;
	//
	private static Logger logger=LoggerFactory.get(StunServerReceiverThread.class);
	//
	StunServerReceiverThread(DatagramSocket datagramSocket,
			List<DatagramSocket> sockets) {
		InetSocketAddress address = (InetSocketAddress) datagramSocket
				.getLocalSocketAddress();
		setName("StunServerReceiver-" + address.getHostString() + ":"
				+ address.getPort());
		this.receiverSocket = datagramSocket;
		for (DatagramSocket socket : sockets) {
			if ((socket.getLocalPort() != receiverSocket.getLocalPort())
					&& (socket.getLocalAddress().equals(receiverSocket
							.getLocalAddress())))
				changedPort = socket;
			if ((socket.getLocalPort() == receiverSocket.getLocalPort())
					&& (!socket.getLocalAddress().equals(
							receiverSocket.getLocalAddress())))
				changedIP = socket;
			if ((socket.getLocalPort() != receiverSocket.getLocalPort())
					&& (!socket.getLocalAddress().equals(
							receiverSocket.getLocalAddress())))
				changedPortIP = socket;
		}
	}

	//
	private boolean process(DatagramPacket receive, MessageHeader receiveMH)
			throws Exception {
		receiveMH.parseAttributes(receive.getData());
		if (receiveMH.getType() == MessageHeaderType.BindingRequest) {
			if (logger.isDebugEnabled()) {
				logger.debug(receiverSocket.getLocalAddress().getHostAddress()
						+ ":" + receiverSocket.getLocalPort()
						+ " Binding Request received from "
						+ receive.getAddress().getHostAddress() + ":"
						+ receive.getPort());
			}
			ChangeRequest cr = (ChangeRequest) receiveMH
					.getMessageAttribute(MessageAttributeType.ChangeRequest);
			if (cr == null) {
				return false;
			}
			ResponseAddress ra = (ResponseAddress) receiveMH
					.getMessageAttribute(MessageAttributeType.ResponseAddress);

			MessageHeader sendMH = new MessageHeader(
					MessageHeaderType.BindingResponse);
			sendMH.setTransactionID(receiveMH.getTransactionID());

			// Mapped address attribute
			MappedAddress ma = new MappedAddress();
			ma.setAddress(new Address(receive.getAddress().getAddress()));
			ma.setPort(receive.getPort());
			sendMH.addMessageAttribute(ma);
			// Changed address attribute
			ChangedAddress ca = new ChangedAddress();
			ca.setAddress(new Address(changedPortIP.getLocalAddress().getAddress()));
			ca.setPort(changedPortIP.getLocalPort());
			sendMH.addMessageAttribute(ca);
			if (cr.isChangePort() && (!cr.isChangeIP())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Change port received in Change Request attribute");
				}
				// Source address attribute
				SourceAddress sa = new SourceAddress();
				sa.setAddress(new Address(changedPort.getLocalAddress()
						.getAddress()));
				sa.setPort(changedPort.getLocalPort());
				sendMH.addMessageAttribute(sa);
				byte[] data = sendMH.getBytes();
				DatagramPacket send = new DatagramPacket(data, data.length);
				if (ra != null) {
					send.setPort(ra.getPort());
					send.setAddress(ra.getAddress().getInetAddress());
				} else {
					send.setPort(receive.getPort());
					send.setAddress(receive.getAddress());
				}
				changedPort.send(send);
				if (logger.isDebugEnabled()) {
					logger.debug(changedPort.getLocalAddress().getHostAddress()
							+ ":" + changedPort.getLocalPort()
							+ " send Binding Response to "
							+ send.getAddress().getHostAddress() + ":"
							+ send.getPort());
				}
			} else if ((!cr.isChangePort()) && cr.isChangeIP()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Change ip received in Change Request attribute");
				}
				// Source address attribute
				SourceAddress sa = new SourceAddress();
				sa.setAddress(new Address(changedIP.getLocalAddress()
						.getAddress()));
				sa.setPort(changedIP.getLocalPort());
				sendMH.addMessageAttribute(sa);
				byte[] data = sendMH.getBytes();
				DatagramPacket send = new DatagramPacket(data, data.length);
				if (ra != null) {
					send.setPort(ra.getPort());
					send.setAddress(ra.getAddress().getInetAddress());
				} else {
					send.setPort(receive.getPort());
					send.setAddress(receive.getAddress());
				}
				changedIP.send(send);
				if (logger.isDebugEnabled()) {
					logger.debug(changedIP.getLocalAddress().getHostAddress()
							+ ":" + changedIP.getLocalPort()
							+ " send Binding Response to "
							+ send.getAddress().getHostAddress() + ":"
							+ send.getPort());
				}
			} else if ((!cr.isChangePort()) && (!cr.isChangeIP())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Nothing received in Change Request attribute");
				}
				// Source address attribute
				SourceAddress sa = new SourceAddress();
				sa.setAddress(new Address(receiverSocket.getLocalAddress()
						.getAddress()));
				sa.setPort(receiverSocket.getLocalPort());
				sendMH.addMessageAttribute(sa);
				byte[] data = sendMH.getBytes();
				DatagramPacket send = new DatagramPacket(data, data.length);
				if (ra != null) {
					send.setPort(ra.getPort());
					send.setAddress(ra.getAddress().getInetAddress());
				} else {
					send.setPort(receive.getPort());
					send.setAddress(receive.getAddress());
				}
				receiverSocket.send(send);
				if (logger.isDebugEnabled()) {
					logger.debug(receiverSocket.getLocalAddress()
							.getHostAddress()
							+ ":"
							+ receiverSocket.getLocalPort()
							+ " send Binding Response to "
							+ send.getAddress().getHostAddress()
							+ ":"
							+ send.getPort());
				}
			} else if (cr.isChangePort() && cr.isChangeIP()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Change port and ip received in Change Request attribute");
				}
				// Source address attribute
				SourceAddress sa = new SourceAddress();
				sa.setAddress(new Address(changedPortIP.getLocalAddress()
						.getAddress()));
				sa.setPort(changedPortIP.getLocalPort());
				sendMH.addMessageAttribute(sa);
				byte[] data = sendMH.getBytes();
				DatagramPacket send = new DatagramPacket(data, data.length);
				if (ra != null) {
					send.setPort(ra.getPort());
					send.setAddress(ra.getAddress().getInetAddress());
				} else {
					send.setPort(receive.getPort());
					send.setAddress(receive.getAddress());
				}
				changedPortIP.send(send);
				if (logger.isDebugEnabled()) {
					logger.debug(changedPortIP.getLocalAddress()
							.getHostAddress()
							+ ":"
							+ changedPortIP.getLocalPort()
							+ " send Binding Response to "
							+ send.getAddress().getHostAddress()
							+ ":"
							+ send.getPort());
				}
			}
		}else{
			logger.warn(receiveMH.getType()+" ignored");
		}
		return true;
	}

	//
	public void run() {
		while (!isStop) {
			try {
				DatagramPacket receive = new DatagramPacket(new byte[200], 200);
				receiverSocket.receive(receive);
				if (logger.isDebugEnabled()) {
					logger.debug(receiverSocket.getLocalAddress()
							.getHostAddress()
							+ ":"
							+ receiverSocket.getLocalPort()
							+ " datagram received from "
							+ receive.getAddress().getHostAddress()
							+ ":"
							+ receive.getPort());
				}
				MessageHeader receiveMH = MessageHeader.parseHeader(receive
						.getData());
				if (!process(receive, receiveMH)) {
					MessageHeader sendMH = new MessageHeader(
							MessageHeaderType.BindingErrorResponse);
					sendMH.setTransactionID(receiveMH.getTransactionID());
					// Unknown attributes
					UnknownAttribute ua = new UnknownAttribute();
					sendMH.addMessageAttribute(ua);
					byte[] data = sendMH.getBytes();
					DatagramPacket send = new DatagramPacket(data, data.length);
					send.setPort(receive.getPort());
					send.setAddress(receive.getAddress());
					receiverSocket.send(send);
					logger.debug(changedPortIP.getLocalAddress()
							.getHostAddress()
							+ ":"
							+ changedPortIP.getLocalPort()
							+ " send Binding Error Response to "
							+ send.getAddress().getHostAddress()
							+ ":"
							+ send.getPort());
				}
			} catch (Exception ioe) {
				logger.catching(ioe);
			}
		}
	}
	//
}