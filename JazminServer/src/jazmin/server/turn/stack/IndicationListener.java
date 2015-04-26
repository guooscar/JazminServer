/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack;

import jazmin.server.turn.stack.stack.Allocation;
import jazmin.server.turn.stack.stack.FiveTuple;
import jazmin.server.turn.stack.stack.TurnStack;

import org.ice4j.StunMessageEvent;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.message.Indication;
import org.ice4j.message.Message;
import org.ice4j.stack.MessageEventHandler;

/**
 * Abstract class for Indication Listener.
 * 
 * @author Aakash Garg
 */
public abstract class IndicationListener implements MessageEventHandler 
{
    /**
     * The turnStack for this instance.
     */
    private final TurnStack turnStack;
    
    /**
     * Represents if the listener is started or not.
     */
    private boolean started = false;
    
    /**
     * Represents the localAddress associated with the listener.
     */
    private TransportAddress localAddress;

    /**
     * The turnStack to call.
     * @param turnStack
     */
    public IndicationListener(TurnStack turnStack)
    {
	this.turnStack = turnStack;
    }
    
    /**
     * Checks if the message is an Indication message. If yes it then finds the
     * five tuple and corresponding allocation and calls the handleIndication.
     */
    @Override
    public void handleMessageEvent(StunMessageEvent evt) {
	Message message = evt.getMessage();
	if(Message.isIndicationType(message.getMessageType()))
	{
	    Indication ind = (Indication) message;
	    
	    TransportAddress clientAddress = evt.getRemoteAddress();
            TransportAddress serverAddress = evt.getLocalAddress();
            Transport transport = serverAddress.getTransport();
            FiveTuple fiveTuple =
                new FiveTuple(clientAddress, serverAddress, transport);
            Allocation alloc = turnStack.getServerAllocation(fiveTuple);
            
	    this.handleIndication(ind,alloc);
	}		
    }
    
    /**
     * Sets the turnStack for this class.
     * @return
     */
    public TurnStack getTurnStack()
    {
	return this.turnStack;
    }
    
    /**
     * Sets the localAddress.
     * 
     * @param localAddress
     *            the localAddress to listen on.
     */
    public void setLocalAddress(TransportAddress localAddress)
    {
	this.localAddress = localAddress;
    }
    
    /**
     * Handles the Indication message.
     * It must be implemented by the subclass.
     * 
     * @param ind
     *            the Indication to handle.
     * @param alloc
     *            the corresponding to the request.
     */
    abstract public void handleIndication(Indication ind, Allocation alloc);

    /**
     * Starts this <tt>IndicationListener</tt>. If it is not currently
     * running, does nothing.
     */
    public void start()
    {
        if (!started)
        {
            turnStack.addIndicationListener(localAddress, this);
            started = true;
        }
    }

    /**
     * Stops this <tt>IndicationListenerr</tt>. A stopped
     * <tt>IndicationListenerr</tt> can be restarted by calling
     * {@link #start()} on it.
     */
    public void stop()
    {
        turnStack.removeIndicationListener(localAddress, this);
        started = false;
    }
}
