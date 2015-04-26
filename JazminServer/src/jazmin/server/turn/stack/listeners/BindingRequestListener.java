/*
 * ice4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 * Maintained by the SIP Communicator community (http://sip-communicator.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package jazmin.server.turn.stack.listeners;

import java.util.logging.*;

import org.ice4j.*;
import org.ice4j.message.*;
import org.ice4j.stack.*;

/**
 * The class that would be handling and responding to incoming requests that are
 * validated and sends a SUCCESS response
 * 
 * @author Aakash Garg
 */
public class BindingRequestListener
    implements RequestListener
{
    /**
     * The <tt>Logger</tt> used by the <tt>BindingRequestListener</tt> class and
     * its instances for logging output.
     */
    private static final Logger logger = Logger
        .getLogger(BindingRequestListener.class.getName());

    private final StunStack stunStack;

    /**
     * The indicator which determines whether this
     * <tt>ValidatedrequestListener</tt> is currently started.
     */
    private boolean started = false;

    /**
     * Creates a new BindingRequestListener
     * 
     * @param turnStack
     */
    public BindingRequestListener(StunStack stunStack)
    {
        this.stunStack = stunStack;
    }

    @Override
    public void processRequest(StunMessageEvent evt)
        throws IllegalArgumentException
    {
        if (logger.isLoggable(Level.FINER))
        {
            logger.setLevel(Level.FINEST);
        }
        Message message = evt.getMessage();

        if (message.getMessageType() == Message.BINDING_REQUEST)
        {
            logger.finest("Received a Binding Request from "
                + evt.getRemoteAddress());
            TransportAddress mappedAddress = evt.getRemoteAddress();
            // Response response =
            // MessageFactory.createBindingResponse(request,mappedAddress);
            TransportAddress sourceAddress = evt.getLocalAddress();
            TransportAddress changedAddress =
                new TransportAddress("stunserver.org", 3489, Transport.UDP);
            Response response = MessageFactory.create3489BindingResponse(
                mappedAddress, sourceAddress, changedAddress);

            try
            {
                stunStack.sendResponse(
                    evt.getTransactionID().getBytes(), response,
                    evt.getLocalAddress(), evt.getRemoteAddress());
                logger.finest("Binding Response Sent.");
            }
            catch (Exception e)
            {
                logger.log(
                    Level.INFO, "Failed to send " + response + " through "
                        + evt.getLocalAddress(), e);
                // try to trigger a 500 response although if this one failed,
                throw new RuntimeException("Failed to send a response", e);
            }
        }
    }

    /**
     * Starts this <tt>BindingRequestListener</tt>. If it is not currently
     * running, does nothing.
     */
    public void start()
    {
        if (!started)
        {
            stunStack.addRequestListener(this);
            started = true;
        }
    }

    /**
     * Stops this <tt>ValidatedRequestListenerr</tt>. A stopped
     * <tt>ValidatedRequestListenerr</tt> can be restarted by calling
     * {@link #start()} on it.
     */
    public void stop()
    {
        stunStack.removeRequestListener(this);
        started = false;
    }
}
