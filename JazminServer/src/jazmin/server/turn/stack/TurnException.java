/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack;

import org.ice4j.StunException;

/**
 * @author Aakash Garg
 * 
 */
public class TurnException
    extends StunException
{

    /**
     * 
     */
    private static final long serialVersionUID = -8004612606830162094L;

    /**
     * 
     */
    public TurnException()
    {
    }

    /**
     * @param id
     */
    public TurnException(int id)
    {
        super(id);
    }

    /**
     * @param message
     */
    public TurnException(String message)
    {
        super(message);
    }

    /**
     * @param id
     * @param message
     */
    public TurnException(int id, String message)
    {
        super(id, message);
    }

    /**
     * @param id
     * @param message
     * @param cause
     */
    public TurnException(int id, String message, Throwable cause)
    {
        super(id, message, cause);
    }

    /**
     * @param message
     * @param cause
     */
    public TurnException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public TurnException(Throwable cause)
    {
        super(cause);
    }

}
