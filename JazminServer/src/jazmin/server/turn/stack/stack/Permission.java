/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.stack;

import org.ice4j.*;

/**
 * This class is an implementation of Permissions in TURN protocol.
 * 
 * @author Aakash Garg
 * 
 */
public class Permission
{
    /**
     * The maximum lifetime allowed for a Permission.
     */
    public static final long MAX_LIFETIME = 300 * 1000;

    /**
     * The IP address of the peer for which to create Permission.
     */
    private TransportAddress ipAddress;
 /**
     * The time in milliseconds when the Permission will expire.
     */
    private long expirationTime = -1;

    /**
     * Determines whether or not the Permission has expired.
     */
    private boolean expired = false;

    /**
     * @param ipAddress contains the peer IP address and transport protocol to
     *            be assigned. The port value is ignored.
     */
    public Permission(TransportAddress ipAddress)
    {
        this.setIpAddress(ipAddress);
        this.setLifetime(Permission.MAX_LIFETIME);
    }
    
    /**
     * @param ipAddress contains the peer IP address and transport protocol to
     *            be assigned. The port value is ignored.
     * @param lifetime the lifetime of permission.
     */
    public Permission(TransportAddress ipAddress, long lifetime)
    {
        this.setIpAddress(ipAddress);
        this.setLifetime(lifetime);
    }

    /**
     * @param ipAddress contains the peer IP address in String format.
     * @param lifetime the lifetime of permission.
     */
    public Permission(String ipAddress, long lifetime)
    {
        this.setIpAddress(ipAddress);
        this.setLifetime(lifetime);
    }

    /**
     * @return the ipAddress of the Permission as a TransportAddress.
     */
    public TransportAddress getIpAddress()
    {
        return ipAddress;
    }

    /**
     * @return the ipAddress as a String.
     */
    public String getIpAddressString()
    {
        return this.getIpAddress().getHostAddress();
    }

    /**
     * @param ipAddress the ipAddress of the peer for which to create
     *            Permission.
     */
    public void setIpAddress(TransportAddress ipAddress)
    {
        this.ipAddress =
            new TransportAddress(ipAddress.getHostAddress(), 0,
                ipAddress.getTransport());
    }

    /**
     * @param ipAddress the ipAddress as String of the peer for which to create
     *            Permission.
     */
    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = new TransportAddress(ipAddress, 0, Transport.UDP);
    }

    /**
     * Returns the lifetime associated with this Permission.
     * If the Permission is expired it returns 0. 
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
     *  Sets the time to expire in milli-seconds for this Permission.
     *  Max lifetime can be Permission.MAX_LIFEIME.
     *  
     *  @param lifetime the lifetime for this Permission.
     */
    public void setLifetime(long lifetime)
    {
        synchronized(this)
        {
            this.expirationTime = System.currentTimeMillis()
                + Math.min(lifetime*1000, Permission.MAX_LIFETIME);
        }
    }
    
    /**
     * Refreshes the permission with the MAX_LIFETIME value.
     */
    public void refresh()
    {
        this.setLifetime(Permission.MAX_LIFETIME);
    }
    
    /**
     * refreshes the permission with given lifetime value.
     * @param lifetime the required lifetime of permission.
     */
    public void refresh(int lifetime)
    {
        this.setLifetime(lifetime);
    }

    /**
     * Start the Permission. This launches the countdown to the moment the
     * Permission would expire.
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
                        "Permission has already been started!");
            }
        }
    }
    
    /**
     * Determines whether this <tt>Permission</tt> is expired now.
     *
     * @return <tt>true</tt> if this <tt>Permission</tT> is expired
     * now; otherwise, <tt>false</tt>
     */
    public boolean isExpired()
    {
        return isExpired(System.currentTimeMillis());
    }
    
    /**
     * Expires the Permission. Once this method is called the Permission is
     * considered terminated.
     */
    public synchronized void expire()
    {
        expired = true;
        /*
         * TurnStack has a background Thread running with the purpose of
         * removing expired Permissions.
         */
    }
    
    /**
     * Determines whether this <tt>Permission</tt> will be expired at
     * a specific point in time.
     *
     * @param now the time in milliseconds at which the <tt>expired</tt> state
     * of this <tt>Permission</tt> is to be returned
     * @return <tt>true</tt> if this <tt>Permission</tt> will be
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
    /*
     * The permission is uniquely identified by its IP address, so hashCode is
     * calculated on the IP address only.
     */
    @Override
    public int hashCode()
    {
        return ipAddress.getHostAddress().hashCode();
    }

    /*
     * Two Permissions are equal if their associated IP address, lifetime and
     * transport protocol are same.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Permission))
        {
            return false;
        }
        Permission other = (Permission) obj;
        if (ipAddress == null)
        {
            if (other.ipAddress != null)
            {
                return false;
            }
        }
        else if (ipAddress.getHostAddress().compareTo(
            other.ipAddress.getHostAddress()) != 0)
        {
            return false;
        }
        if (expirationTime != other.expirationTime)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Permission ["
            + (ipAddress != null ? "ipAddress=" + ipAddress : "") + "]";
    }
}
