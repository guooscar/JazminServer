/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.collectors;

import java.util.logging.*;

import org.ice4j.*;
import org.ice4j.message.*;
import org.ice4j.stack.*;

/**
 * The class that would be handling and responding to incoming Refresh
 * responses.
 * 
 * @author Aakash Garg
 */
public class RefreshResponseCollector
    implements ResponseCollector
{
    /**
     * The <tt>Logger</tt> used by the <tt>RefreshresponseCollector</tt> class
     * and its instances for logging output.
     */
    private static final Logger logger = Logger
        .getLogger(RefreshResponseCollector.class.getName());

    private final StunStack stunStack;

    /**
     * Creates a new RefreshResponseCollector
     * 
     * @param turnStack
     */
    public RefreshResponseCollector(StunStack stunStack)
    {
        this.stunStack = stunStack;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ice4j.ResponseCollector#processResponse(org.ice4j.StunResponseEvent)
     */
    @Override
    public void processResponse(StunResponseEvent evt)
    {
        if (logger.isLoggable(Level.FINER))
        {
            logger.finer("Received response " + evt);
        }
        Message message = evt.getMessage();
        if (message.getMessageType() == Message.REFRESH_ERROR_RESPONSE)
        {
            // delete allocation
        }
        else if (message.getMessageType() == Message.REFRESH_RESPONSE)
        {
            // update allocation
        }
        else
        {
            return;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ice4j.ResponseCollector#processTimeout(org.ice4j.StunTimeoutEvent)
     */
    @Override
    public void processTimeout(StunTimeoutEvent event)
    {

    }

}
