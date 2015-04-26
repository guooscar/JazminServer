/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.listeners;

import java.io.UnsupportedEncodingException;
import jazmin.log.*;

import jazmin.server.turn.stack.IndicationListener;
import jazmin.server.turn.stack.stack.Allocation;
import jazmin.server.turn.stack.stack.TurnStack;

import org.ice4j.TransportAddress;
import org.ice4j.attribute.Attribute;
import org.ice4j.attribute.DataAttribute;
import org.ice4j.attribute.XorPeerAddressAttribute;
import org.ice4j.message.Indication;
import org.ice4j.message.Message;

/**
 * Class to handle the incoming Data indications.
 * 
 * @author Aakash Garg
 * 
 */
public class DataIndicationListener extends IndicationListener {
	/**
	 * The <tt>Logger</tt> used by the <tt>DataIndicationListener</tt> class and
	 * its instances for logging output.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(DataIndicationListener.class);

	/**
	 * parametrised constructor.
	 * 
	 * @param turnStack
	 *            the turnStack to set for this class.
	 */
	public DataIndicationListener(TurnStack turnStack) {
		super(turnStack);
	}

	/**
	 * Handles the incoming data indication.
	 * 
	 * @param ind
	 *            the indication to handle.
	 * @param alloc
	 *            the allocation associated with message.
	 */
	@Override
	public void handleIndication(Indication ind, Allocation alloc) {
		if (ind.getMessageType() == Message.DATA_INDICATION) {
			logger.debug("Received a Data Indication message.");
			byte[] tran = ind.getTransactionID();

			XorPeerAddressAttribute xorPeerAddress = (XorPeerAddressAttribute) ind
					.getAttribute(Attribute.XOR_PEER_ADDRESS);
			xorPeerAddress.setAddress(xorPeerAddress.getAddress(), tran);
			DataAttribute data = (DataAttribute) ind
					.getAttribute(Attribute.DATA);

			TransportAddress peerAddr = xorPeerAddress.getAddress();
			try {
				String line = new String(data.getData(), "UTF-8");
				// System.out.println(line);
				logger.debug("Data Indiaction message from  " + peerAddr
						+ " is " + line);
				/*
				 * System.out.println("Received a Data indiction from " +
				 * peerAddr + ", message : " + line);
				 */} catch (UnsupportedEncodingException e) {
				logger.catching(e);
			}
		}
	}

}
