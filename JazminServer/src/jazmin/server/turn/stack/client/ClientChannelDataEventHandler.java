/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.client;

import java.io.UnsupportedEncodingException;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.turn.stack.stack.TurnStack;

import org.ice4j.ChannelDataMessageEvent;
import org.ice4j.message.ChannelData;
import org.ice4j.stack.ChannelDataEventHandler;
import org.ice4j.stack.StunStack;

/**
 * Handles the incoming ChannelData message for Client from Server.
 * 
 * @author Aakash Garg
 * 
 */
public class ClientChannelDataEventHandler implements ChannelDataEventHandler {

	/**
	 * The <tt>Logger</tt> used by the <tt>ServerChannelDataEventHandler</tt>
	 * class and its instances for logging output.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(ClientChannelDataEventHandler.class);

	/**
	 * The turnStack to call.
	 */
	private TurnStack turnStack;

	/**
	 * Default constructor.
	 */
	public ClientChannelDataEventHandler() {
	}

	/**
	 * parametrised contructor.
	 * 
	 * @param turnStack
	 *            the turnStack for this class.
	 */
	public ClientChannelDataEventHandler(StunStack turnStack) {
		if (turnStack instanceof TurnStack) {
			this.turnStack = (TurnStack) turnStack;
		} else {
			throw new IllegalArgumentException("This is not a TurnStack!");
		}
	}

	/**
	 * Sets the turnStack for this class.
	 * 
	 * @param turnStack
	 *            the turnStack to set for this class.
	 */
	public void setTurnStack(TurnStack turnStack) {
		this.turnStack = turnStack;
	}

	/**
	 * Handles the ChannelDataMessageEvent.
	 * 
	 * @param evt
	 *            the ChannelDataMessageEvent to handle/process.
	 */
	@Override
	public void handleMessageEvent(ChannelDataMessageEvent evt) {
		if (logger.isDebugEnabled()) {
			logger.debug("Received ChannelData message " + evt);
		}
		ChannelData channelData = evt.getChannelDataMessage();
		char channelNo = channelData.getChannelNumber();
		byte[] data = channelData.getData();
		try {
			String line = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.catching(e);
		}
	}
}
