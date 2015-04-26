/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.socket;

/**
 * Represents the source of generating the TCP connect events.
 * 
 * @author Aakash Garg
 * 
 */
public interface TcpConnectEventGenerator
{
    public void setEventListener(TcpConnectEventListener listener);
    
    public void removeEventListener();
    
    public void fireConnectEvent(TcpConnectEvent event);
}
