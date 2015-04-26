/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.stack;

import java.util.Arrays;
import java.util.logging.*  ;

import org.ice4j.*;
import org.ice4j.message.*;
import org.ice4j.stack.*;

/**
 * Class to handle incoming ChannelData messages coming from Client to Server.
 * It first finds if there is a ChannelBind installed for the peer. 
 * If yes it then sends the UDP message to peer.
 * If no it then silently ignores the message.
 * 
 * @author Aakash Garg
 */
public class ServerChannelDataEventHandler implements
	ChannelDataEventHandler {
    
    /**
     * The <tt>Logger</tt> used by the
     * <tt>ServerChannelDataEventHandler</tt> class and its instances for
     * logging output.
     */
    private static final Logger logger = Logger
        .getLogger(ServerChannelDataEventHandler.class.getName());

    /**
     * The turnStack to call.
     */
    private TurnStack turnStack;

    /**
     * Default Constructor.
     */
    public ServerChannelDataEventHandler()
    {
    }
    
    /**
     * Parametrized constructor.
     * @param turnStack the turnStack to set for this class.
     */
    public ServerChannelDataEventHandler(StunStack turnStack) 
    {
	if (turnStack instanceof TurnStack)
        {
            this.turnStack = (TurnStack) turnStack;
        }
        else
        {
            throw new IllegalArgumentException("This is not a TurnStack!");
        }
    }

    /**
     * Sets the TurnStack for this class.
     * @param turnStack the turnStack to set for this class.
     */
    public void setTurnStack(TurnStack turnStack)
    {
	this.turnStack = turnStack;
    }
    
    /**
     * Handles the ChannelDataMessageEvent.
     * @param evt the ChannelDataMessageEvent to handle/process.
     */
    @Override
    public void handleMessageEvent(ChannelDataMessageEvent evt) 
    {
        if(!logger.isLoggable(Level.FINER)){
            logger.setLevel(Level.FINER);
        }
	ChannelData channelData = evt.getChannelDataMessage();
	char channelNo = channelData.getChannelNumber();
	byte[] data = channelData.getData();
	logger.finer("Received a ChannelData message for " + (int)channelNo
		+ " , message : " + Arrays.toString(data));
	
	TransportAddress clientAddress = evt.getRemoteAddress();
        TransportAddress serverAddress = evt.getLocalAddress();
        Transport transport = Transport.UDP;
        FiveTuple fiveTuple =
            new FiveTuple(clientAddress, serverAddress, transport);
        
        Allocation allocation 
            = this.turnStack.getServerAllocation(fiveTuple);
        
        if(allocation==null)
        {
            logger.finer("allocation not found.");
        }
        else if(!allocation.containsChannel(channelNo))
        {
	    logger.finer("ChannelNo " + (int) channelNo
		    + " not found in Allocation!");
            return;
        }
        TransportAddress destAddr = allocation.getPeerAddr(channelNo);
        if(destAddr != null)
        {
	    RawMessage message = new RawMessage(data, data.length, destAddr,
		    allocation.getClientAddress());
	    try {
		logger.finer("Dispatching a UDP message to " + destAddr
			+ ", data: " + Arrays.toString(message.getBytes()));
		this.turnStack.sendUdpMessage(message, destAddr,
		    allocation.getRelayAddress());
	    } catch (StunException e) {
		logger.finer(e.getMessage());
	    }
        }
        else
        {
	    logger.finer("Peer address not found for channel "
		    + (int) channelNo);
        }

    }

}
