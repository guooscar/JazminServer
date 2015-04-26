/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.stack;


import org.ice4j.Transport;
import org.ice4j.TransportAddress;

/**
 * This class is an implementation of ChannelBind in TURN protocol.
 * 
 * @author Aakash Garg
 * 
 */
public class ChannelBind
{
   
    /**
     * The maximum lifetime allowed for a ChannelBind (10 min).
     */
    public static final long MAX_LIFETIME = 10 * 60 * 1000;

    /**
     * The IP address and port of the peer for which to create ChannelBind.
     */
    private final TransportAddress peerAddress;

    /**
     * Represents the channel no of the ChannelBind.
     */
    private final char channelNo;
  
    /**
     * The time in milliseconds when the ChannelBinding will expire.
     */
    private long expirationTime = -1;

    /**
     * Determines whether or not the ChannelBinding has expired.
     */
    private boolean expired = true;

    /**
     * Creates a new ChannelBind object with MAX_LIFETIME as default lifetime
     * value.
     * 
     * @param peerAddress contains the peer IP address with port no and
     *            transport protocol to be assigned.
     * @param channelNo the channelNo of ChannelBind.
     */
    public ChannelBind( TransportAddress peerAddress, 
                        char channelNo)
    {
        this(peerAddress, channelNo, ChannelBind.MAX_LIFETIME);
    }

    /**
     * Creates a new ChannelBind object with given lifetime value.
     * 
     * @param peerAddress contains the peer IP address and transport protocol to
     *            be assigned. The port value is ignored.
     * @param channelNo the channelNo of the ChannelBind request.
     * @param lifetime the lifetime of ChannelBind.
     */
    public ChannelBind( TransportAddress peerAddress, 
                        char channelNo,
                        long lifetime)
    {
        this.peerAddress = peerAddress;
        if (channelNo < 0x4000)
            throw new IllegalArgumentException("Illegal value of channel no");
        this.channelNo = channelNo;
        this.setLifetime(lifetime);
    }

    /**
     * Creates a new ChannelBind object with given lifetime value and UDP as
     * default protocol.
     * 
     * @param peerAddress contains the peer IP address and port no in String
     *            format.
     * @param channelNo the channelNo of the ChannelBind.
     * @param lifetime the lifetime of ChannelBind.
     */
    public ChannelBind( String peerAddress, 
                        char channelNo, 
                        long lifetime)
    {
        this(new TransportAddress(peerAddress, 0, Transport.UDP), 
             channelNo,
             lifetime);
    }

    /**
     * @return the peerAddress as a String.
     */
    public String getPeerAddressString()
    {
        return this.getPeerAddress().getHostAddress();
    }
    
    /**
     * Returns the Peer Address associated with this ChannelBind.
     */
    public TransportAddress getPeerAddress()
    {
        return peerAddress;
    }
    
    /**
     * returns the channelNo associated with this ChannelBind.
     */
    public char getChannelNo()
    {
        return channelNo;
    }
 
    /**
     * Returns the lifetime associated with this ChannelBind.
     * If the ChannelBind is expired it returns 0. 
     */
    public long getLifetime()
    {
        if(!isExpired())
        {
            return (this.expirationTime-System.currentTimeMillis());
        }
        else
        {
            return 0;
        }
    }
    
    /**
     *  Sets the time to expire in milliseconds for this ChannelBind.
     *  Max lifetime can be ChannelBind.MAX_LIFEIME.
     *  
     *  @param lifetime the lifetime for this ChannelBind.
     */
    public void setLifetime(long lifetime)
    {
        synchronized(this)
        {
            this.expirationTime = System.currentTimeMillis()
                + Math.min(lifetime*1000, ChannelBind.MAX_LIFETIME);
        }
    }

    /**
     * Refreshes the ChannelBind with the MAX_LIFETIME value.
     */
    public void refresh()
    {
        this.setLifetime(ChannelBind.MAX_LIFETIME);
    }
    
    /**
     * refreshes the ChannelBind with given lifetime value.
     * @param lifetime the required lifetime of ChannelBind.
     */
    public void refresh(int lifetime)
    {
        this.setLifetime(lifetime);
    }
    
    /**
     * Start the ChannelBind. This launches the countdown to the moment the
     * ChannelBind would expire.
     */
    public synchronized void start()
    {
        synchronized(this)
        {
            if (expirationTime == -1)
            {
                expired = false;
                expirationTime = MAX_LIFETIME + System.currentTimeMillis();
            }
            else
            {
                throw new IllegalStateException(
                        "ChannelBind has already been started!");
            }
        }
    }
    
    /**
     * Determines whether this <tt>ChannelBind</tt> is expired now.
     *
     * @return <tt>true</tt> if this <tt>ChannelBind</tT> is expired
     * now; otherwise, <tt>false</tt>
     */
    public boolean isExpired()
    {
        return isExpired(System.currentTimeMillis());
    }
    
    /**
     * Expires the ChannelBind. Once this method is called the ChannelBind is
     * considered terminated.
     */
    public synchronized void expire()
    {
        expired = true;
        /*
         * Allocation has a background Thread running with the purpose of
         * removing expired ChannelBinds.
         */
    }
    
    /**
     * Determines whether this <tt>ChannelBind</tt> will be expired at
     * a specific point in time.
     *
     * @param now the time in milliseconds at which the <tt>expired</tt> state
     * of this <tt>ChannelBind</tt> is to be returned
     * @return <tt>true</tt> if this <tt>ChannelBind</tt> will be
     * expired at the specified point in time; otherwise, <tt>false</tt>
     */
    public synchronized boolean isExpired(long now)
    {
        if (expirationTime == -1)
            return false;
        else if (expirationTime < now)
            return true;
        else
            return expired;
    }
    
    
    @Override
    public int hashCode()
    {
        return channelNo + peerAddress.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof ChannelBind))
        {
            return false;
        }
        ChannelBind c = (ChannelBind) o;
        if(c.getChannelNo() == this.channelNo
                && c.getPeerAddress().equals(this.peerAddress))
        {
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "ChannelBind ["
            + (peerAddress != null ? "peerAddress=" + peerAddress + ", " : "")
            + "channelNo=" + (int)channelNo + "]";
    }
    
}
