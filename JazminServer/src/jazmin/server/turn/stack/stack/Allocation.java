/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.stack;

import java.util.HashMap;
import java.util.Iterator;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import org.ice4j.Transport;
import org.ice4j.TransportAddress;

/**
 * This class is an implementation of Allocations in TURN server.
 * 
 * @author Aakash Garg
 * 
 */
public class Allocation
{
    /**
     * Our class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Allocation.class);

    /**
     * represents the relay address associated with this Allocation.
     */
    private final TransportAddress relayAddress;

    /**
     * Represents the FiveTuple associated with this Allocation.
     */
    private final FiveTuple fiveTuple;

    /**
     * Represents the username associated with this Allocation.
     */
    private final String username;

    /**
     * represents the password associated with this Allocation.
     */
    private final String password;

    /**
     * The time in milliseconds when the Allocation will expire.
     */
    private long expirationTime = -1;

    /**
     * Determines whether or not the Allocation has expired.
     */
    private boolean expired = false;

    /**
     * The default lifetime allowed for a Allocation.
     */
    public static final long DEFAULT_LIFETIME = 10 * 60 * 1000;

    /**
     * The max lifetime allowed for a Allocation.
     */
    public static final long MAX_LIFETIME = 60 * 60 * 1000;

    /**
     * The maximum no of Permissions per Allocation.
     */
    public static final int MAX_PERMISSIONS = 10;

    /**
     * The Maximum no of ChannelBinds per Allocation.
     */
    public static final int MAX_CHANNELBIND = 10;

    /**
     * The <tt>Thread</tt> which expires the <tt>Permission</tt>s of this
     * <tt>Allocation</tt> and removes them from {@link #permissions}.
     */
    private Thread permissionExpireThread;

    /**
     * Represents the permissions associated with peerAddress IP installed for
     * this Allocation.
     */
    private final HashMap<TransportAddress, Permission> permissions =
        new HashMap<TransportAddress, Permission>();

    /**
     * The <tt>Thread</tt> which expires the <tt>ChannelBind</tt>s of this
     * <tt>Allocation</tt> and removes them from {@link #channelBindings}.
     */
    private Thread channelBindExpireThread;

    /**
     * Represents the Channel Bindings associated with this Allocation.
     */
    private final HashMap<Character, ChannelBind> channelBindings =
        new HashMap<Character, ChannelBind>();

    /**
     * Contains the mapping of peerAdress of ChannelBinds to Channelno. This is
     * used to check the peerAddress while creating new Permissions and
     * ChannelBinds.
     */
    private final HashMap<TransportAddress, Character> peerToChannelMap =
        new HashMap<TransportAddress, Character>();

    /**
     * Maps one-to-one from ConnecionID to Data Connection.
     */
    private final HashMap<Integer,FiveTuple> connIdToDataConnMap
         = new HashMap<Integer,FiveTuple>();
    
    /**
     * Maps one-to-one from ConnecionID to Peer TCP Connection.
     */
    private final HashMap<Integer,FiveTuple> connIdToPeerConnMap
        = new HashMap<Integer,FiveTuple>();
    
    /**
     * Constructor to instantiate an Allocation without a username and password.
     * 
     * @param relayAddress the realyAddress associated with this Allocation.
     * @param fiveTuple the fiveTuple associated with this Allocation.
     */
    public Allocation(  TransportAddress relayAddress, 
                        FiveTuple fiveTuple)
    {
        this(relayAddress, fiveTuple, null, null);
    }

    /**
     * Constructor to instantiate an Allocation without a username and password
     * with the lifetime value.
     * 
     * @param relayAddress the realyAddress associated with this Allocation.
     * @param fiveTuple the fiveTuple associated with this Allocation.
     * @param lifetime the lifetime for this Allocation.
     */
    public Allocation(  TransportAddress relayAddress, 
                        FiveTuple fiveTuple,
                        long lifetime)
    {
        this(relayAddress, fiveTuple, null, null, lifetime);
    }

    /**
     * Constructor to instantiate an Allocation with given relayAddress,
     * fiveTuple, username, passowrd and with default lifetime value.
     * 
     * @param relayAddress the realyAddress associated with this Allocation.
     * @param fiveTuple the fiveTuple associated with this Allocation.
     * @param username the username associated with this Allocation.
     * @param password the password associated with this Allocation.
     */
    public Allocation(  TransportAddress relayAddress, 
                        FiveTuple fiveTuple,
                        String username, 
                        String password)
    {
        this(relayAddress, fiveTuple, username, password,
            Allocation.DEFAULT_LIFETIME);
    }

    /**
     * Constructor to instantiate an Allocation with given relayAddress,
     * fiveTuple, username, passowrd and with default lifetime value.
     * 
     * @param relayAddress the realyAddress associated with this Allocation.
     * @param fiveTuple the fiveTuple associated with this Allocation.
     * @param username the username associated with this Allocation.
     * @param password the password associated with this Allocation.
     * @param lifetime the lifetime for this allocation.
     */
    public Allocation(  TransportAddress relayAddress, 
                        FiveTuple fiveTuple,
                        String username, 
                        String password, 
                        long lifetime)
    {
        this.relayAddress = relayAddress;
        this.fiveTuple = fiveTuple;
        this.username = username;
        this.password = password;
        this.setLifetime(lifetime);
    }

    /**
     * returns the fiveTuple associated with this Allocation.
     */
    public FiveTuple getFiveTuple()
    {
        return this.fiveTuple;
    }

    /**
     * Returns the relayAddress associated with this Allocation.
     */
    public TransportAddress getRelayAddress()
    {
        return this.relayAddress;
    }

    /**
     * Returns the clientAddress associated with this Allocation.
     * The client address who instianted this allocation.
     */
    public TransportAddress getClientAddress()
    {
        return this.getFiveTuple().getClientTransportAddress();
    }    
    
    /**
     * Returns the serverAddress associated with this Allocation.
     * The serverAddress on which this allocation request is received.
     */
    public TransportAddress getServerAddress()
    {
        return this.getFiveTuple().getServerTransportAddress();
    }
    
    /**
     * Returns the Client Data Connection corresponding to Connection Id for
     * which ConnectionBind Request has been received.
     * 
     * @param connectionId the ConnectionId for which Client Data Connection is
     *            to be returned.
     * @return Client Data Connection if exists else null.
     */
    public FiveTuple getDataConnection(int connectionId){
        return this.connIdToDataConnMap.get(connectionId);
    }
    
    /**
     * Returns the Peer TCP Data Connection corresponding to Connection Id for
     * which ConnectionBind Request has been received.
     * 
     * @param connectionId the ConnectionId for which Peer TCP Data Connection is
     *            to be returned.
     * @return Peer TCP Data Connection if exists else null.
     */
    public FiveTuple getPeerTCPConnection(int connectionId){
        return this.connIdToPeerConnMap.get(connectionId);
    }

    /**
     * Adds the Connection Id with corresponding Client Data Connection to for
     * which ConnectionBind Request has been received.
     * 
     * @param connectionId the ConnectionId.
     * @param clientDataConn Client Data Connection to corresponding
     *            ConnectionId.
     */
    public void addDataConnection(int connectionId, FiveTuple clientDataConn)
    {
        this.connIdToDataConnMap.put(
            connectionId, clientDataConn);
    }

    /**
     * Adds the Connection Id corresponding to Peer TCP Data Connection for
     * which ConnectionBind Request has been received.
     * 
     * @param connectionId the ConnectionId.
     * @param peerDataConn Peer TCP Data Connection to corresponding
     *            ConnectionId.
     */
    public void addPeerTCPConnection(int connectionId, FiveTuple peerDataConn)
    {
        this.connIdToPeerConnMap.put(connectionId,peerDataConn);
    }
    
    /**
     * Removes the Client Data Connection with corresponding to Connection Id.
     * 
     * @param connectionId the ConnectionId corresponding to Client Data
     *            Connection.
     */
    public void removeDataConnection(int connectionId)
    {
        this.connIdToDataConnMap.remove(connectionId);
    }

    /**
     * Removes the Peer TCP Data Connection corresponding to Connection Id.
     * 
     * @param connectionId the ConnectionId corresponding to Client Data
     *            Connection.
     */
    public void removePeerTCPConnection(int connectionId)
    {
        this.connIdToPeerConnMap.remove(connectionId);
    }    
    
    /**
     * Returns the lifetime associated with this Allocation. If the allocation
     * is expired it returns 0.
     */
    public long getLifetime()
    {
        if (!isExpired())
        {
            return (this.expirationTime - System.currentTimeMillis());
        }
        else
        {
            return 0;
        }
    }

    /**
     * Sets the time to expire in milli-seconds for this allocation. Max
     * lifetime can be Allocation.MAX_LIFEIME.
     * 
     * @param lifetime the lifetime for this Allocation.
     */
    public void setLifetime(long lifetime)
    {
        synchronized (this)
        {
            this.expirationTime = System.currentTimeMillis() 
                + Math.min(lifetime * 1000, Allocation.MAX_LIFETIME);
        }
    }

    /**
     * Refreshes the allocation with the DEFAULT_LIFETIME value.
     */
    public void refresh()
    {
        this.setLifetime(Allocation.DEFAULT_LIFETIME);
    }

    /**
     * refreshes the allocation with given lifetime value.
     * 
     * @param lifetime the required lifetime of allocation.
     */
    public void refresh(int lifetime)
    {
        this.setLifetime(lifetime);
    }

    /**
     * Start the Allocation. This launches the countdown to the moment the
     * Allocation would expire.
     */
    public synchronized void start()
    {
        synchronized (this)
        {
            if (expirationTime == -1)
            {
                expired = false;
                expirationTime = DEFAULT_LIFETIME + System.currentTimeMillis();
            }
            else
            {
                throw new IllegalStateException(
                    "Allocation has already been started!");
            }
        }
    }
    
    /**
     * Determines whether this <tt>Allocation</tt> is expired now.
     * 
     * @return <tt>true</tt> if this <tt>Allocation</tT> is expired now;
     *         otherwise, <tt>false</tt>
     */
    public boolean isExpired()
    {
        return isExpired(System.currentTimeMillis());
    }

    /**
     * Expires the Allocation. Once this method is called the Allocation is
     * considered terminated.
     */
    public synchronized void expire()
    {
        expired = true;
        /*
         * TurnStack has a background Thread running with the purpose of
         * removing expired Allocations.
         */
    }

    /**
     * Determines whether this <tt>Allocation</tt> will be expired at a specific
     * point in time.
     * 
     * @param now the time in milliseconds at which the <tt>expired</tt> state
     *            of this <tt>Allocation</tt> is to be returned
     * @return <tt>true</tt> if this <tt>Allocation</tt> will be expired at the
     *         specified point in time; otherwise, <tt>false</tt>
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

    /**
     * Adds a new Permission for this Allocation.
     * 
     * @param peerIP the peer IP address foe which to create this permission to
     *            be added to this allocation.
     */
    public void addNewPermission(TransportAddress peerIP)
    {
        TransportAddress peerIp =
            new TransportAddress(peerIP.getAddress(), 0, Transport.UDP);
        Permission permission = new Permission(peerIp);
        this.addNewPermission(permission);
    }

    /**
     * Adds a new Permission for this Allocation.
     * 
     * @param permission the permission to be added to this allocation.
     */
    public void addNewPermission(Permission permission)
    {
        TransportAddress peerAddr =
            new TransportAddress(permission.getIpAddress().getAddress(), 0,
                Transport.UDP);
        if (this.permissions.containsKey(peerAddr))
        {
            this.permissions.get(permission.getIpAddress()).refresh();
        }
        else if (!this.canHaveMorePermisions())
        {
            return;
        }
        else
        {
            this.permissions.put(
                permission.getIpAddress(), permission);
            maybeStartPermissionExpireThread();
        }
    }

    /**
     * Binds a new Channel to this Allocation.
     * If an existing ChannelBind is found it is refreshed
     * else a new ChannelBind and permission is added.
     * 
     * @param channelBind the channelBind to be added to this allocation.
     * @throws IllegalArgumentException if the channelNo of the channelBind to
     *             be added is already occupied.
     */
    public void addChannelBind(ChannelBind channelBind)
    {
        TransportAddress peerAddr =
            new TransportAddress(
                    channelBind.getPeerAddress().getAddress(),
                    0, 
                    Transport.UDP);
        if (isBadChannelRequest(channelBind))
        {
            throw new IllegalArgumentException("400: BAD REQUEST");
        }
        else if(!channelBindings.containsKey(channelBind.getChannelNo()) 
               && !peerToChannelMap.containsKey(channelBind.getPeerAddress()))
        {
            synchronized(this.channelBindings)
            {
                this.channelBindings.put(   channelBind.getChannelNo(),
                                            channelBind);
            }
            synchronized(this.peerToChannelMap)
            {
                this.peerToChannelMap.put(
                    channelBind.getPeerAddress(), channelBind.getChannelNo());
            }
        }
        else
        {
            synchronized(this.channelBindings)
            {
                this.channelBindings.get(channelBind.getChannelNo()).refresh();
            }
        }
        this.addNewPermission(peerAddr);
        maybeStartChannelBindExpireThread();
    }

    /**
     * Determines whether the ChannelBind request is a BAD request or not.
     * A request is BAD when the same client sends a ChannelBind Request and
     * channel no or peerAddress coincides with existing channel bindings.
     * A request is not bad if the channel no and peerAddress in the ChannelBind
     * request are same as that in current mapping.
     * 
     * @param channelBind the channelBind request to validate.
     * @return true if request is a BAD request.
     */
    public boolean isBadChannelRequest(ChannelBind channelBind)
    {
        boolean hasChannelNo =
            this.channelBindings.containsKey(channelBind.getChannelNo());
        boolean hasPeerAddr =
            this.peerToChannelMap.containsKey(channelBind.getPeerAddress());
        if(hasChannelNo && hasPeerAddr)
        {
            if (this.channelBindings.get(
                channelBind.getChannelNo()).equals(
                    channelBind.getPeerAddress()))
            {
                return false;
            }
      }
      else if(!hasChannelNo && !hasPeerAddr)
      {
          return false;
      }
        return true;
    }
    
    /**
     * Removes the channelBind associated with this channlNo from this
     * allocation.
     * 
     * @param channelNo the channelNo for which the ChannelBind to delete.
     * @return the ChannnelBindingf associated with this channelNo.
     */
    public ChannelBind removeChannelBind(char channelNo)
    {
        ChannelBind channelBind = null;
        synchronized (this.channelBindings)
        {
            channelBind = this.channelBindings.remove(channelNo);
        }
        return channelBind;
    }

    /**
     * Checks if the Permission is installed for the peerAddress. The port value
     * is ignored.
     * 
     * @param peerAddress
     *            the peerAddress for which to check permission.
     * @return true if permission is installed for peerAddress else false.
     */
    public boolean isPermitted(TransportAddress peerAddress)
    {
        peerAddress =
            new TransportAddress(peerAddress.getAddress(), 0,
                peerAddress.getTransport());
        if (this.permissions.containsKey(peerAddress))
        {
            return true;
        }
        return false;
    }
    
    /**
     * Checks if the specified channel no is binded to this allocation.
     * 
     * @param channelNo
     *            the channel number to check.
     * @return true if the specified channel no. is installed for this
     *         allocation.
     */
    public boolean containsChannel(char channelNo)
    {
	return this.channelBindings.containsKey(channelNo);
    }

    /**
     * Gets the channelNO for the specified peerAddress.
     * @param peerAddress the peerAddress for which to get the channel.
     * @return channelNo is channelNo is found, else 0x1000.
     */
    public char getChannel(TransportAddress peerAddress)
    {
	char val = 0x1000;
	if(this.peerToChannelMap.containsKey(peerAddress))
	{
	    return this.peerToChannelMap.get(peerAddress);
	}
	return val;
    }
    
    /**
     * Gets the peerAddress associated with specified channelNo.
     * 
     * @param channelNo
     *            the channel no for which to get the peerAddress.
     * @return peerAddress the peerAddress associated with the channelNo in this
     *         allocation.
     */
    public TransportAddress getPeerAddr(char channelNo)
    {
	ChannelBind cb = this.channelBindings.get(channelNo);
	if(cb!=null)
	{
	    return cb.getPeerAddress();
	}
	return null;
    }
    
    /**
     * Determines if more permissions can be added to this allocation.
     * 
     * @return true if no of permissions are less than maximum allowed
     *         permissions per Allocation.
     */
    public boolean canHaveMorePermisions()
    {
        return (this.permissions.size() < MAX_PERMISSIONS);
    }

    /**
     * Determines if more channels can be added to this allocation.
     * 
     * @return true if no of channels are less than maximum allowed channels per
     *         Allocation.
     */
    public boolean canHaveMoreChannels()
    {
        return (this.channelBindings.size() < MAX_CHANNELBIND);
    }

    /**
     * Initialises and starts {@link #channelBindExpireThread} if necessary.
     */
    public void maybeStartChannelBindExpireThread()
    {
        synchronized (channelBindings)
        {
            if (!channelBindings.isEmpty() && (channelBindExpireThread == null))
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        runInAllocationChannelBindExpireThread();
                    }
                };

                t.setDaemon(true);
                t.setName(getClass().getName() + ".channelBindExpireThread");

                boolean started = false;

                channelBindExpireThread = t;
                try
                {
                    t.start();
                    started = true;
                }
                finally
                {
                    if (!started && (channelBindExpireThread == t))
                        channelBindExpireThread = null;
                }
            }
        }
    }

    /**
     * Runs in {@link #channelBindExpireThread} and expires the
     * <tt>ChannelBind</tt>s of this <tt>Allocation</tt> and removes them from
     * {@link #channelBindingings}.
     */
    private void runInAllocationChannelBindExpireThread()
    {
        try
        {
            long idleStartTime = -1;

            do
            {
                synchronized (channelBindings)
                {
                    try
                    {
                        channelBindings.wait(ChannelBind.MAX_LIFETIME);
                    }
                    catch (InterruptedException ie)
                    {
                    }

                    /*
                     * Is the current Thread still designated to expire the
                     * ChannelBinds of this Allocation?
                     */
                    if (Thread.currentThread() != channelBindExpireThread)
                        break;

                    long now = System.currentTimeMillis();

                    /*
                     * Has the current Thread been idle long enough to merit
                     * disposing of it?
                     */
                    if (channelBindings.isEmpty())
                    {
                        if (idleStartTime == -1)
                            idleStartTime = now;
                        else if (now - idleStartTime > 60 * 1000)
                            break;
                    }
                    else
                    {
                        // Expire the ChannelBinds of this Allocation.

                        idleStartTime = -1;

                        for (Iterator<ChannelBind> i =
                            channelBindings.values().iterator(); i.hasNext();)
                        {
                            ChannelBind channelBind = i.next();

                            if (channelBind == null)
                            {
                                i.remove();
                            }
                            else if (channelBind.isExpired(now))
                            {
                                logger.debug("ChannelBind " + channelBind
                                    + " expired");
                                i.remove();
                                this.peerToChannelMap.remove(
                                    channelBind.getPeerAddress());
                                channelBind.expire();
                            }
                        }
                    }
                }
            }
            while (true);
        }
        finally
        {
            synchronized (channelBindings)
            {
                if (channelBindExpireThread == Thread.currentThread())
                    channelBindExpireThread = null;
                /*
                 * If channelBindExpireThread dies unexpectedly and yet it is
                 * still necessary, resurrect it.
                 */
                if (channelBindExpireThread == null)
                    maybeStartChannelBindExpireThread();
            }
        }
    }

    /**
     * Initialises and starts {@link #permissionExpireThread} if necessary.
     */
    public void maybeStartPermissionExpireThread()
    {
        synchronized (permissions)
        {
            if (!permissions.isEmpty() && (permissionExpireThread == null))
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        runInAllocationPermissionExpireThread();
                    }
                };

                t.setDaemon(true);
                t.setName(getClass().getName() + ".permissionExpireThread");

                boolean started = false;

                permissionExpireThread = t;
                try
                {
                    t.start();
                    started = true;
                }
                finally
                {
                    if (!started && (permissionExpireThread == t))
                        permissionExpireThread = null;
                }
            }
        }
    }

    /**
     * Runs in {@link #PermissionExpireThread} and expires the
     * <tt>Permission</tt>s of this <tt>Allocation</tt> and removes them from
     * {@link #permissions}.
     */
    private void runInAllocationPermissionExpireThread()
    {
        try
        {
            long idleStartTime = -1;

            do
            {
                synchronized (permissions)
                {
                    try
                    {
                        permissions.wait(Permission.MAX_LIFETIME);
                    }
                    catch (InterruptedException ie)
                    {
                    }

                    /*
                     * Is the current Thread still designated to expire the
                     * Permissions of this Allocation?
                     */
                    if (Thread.currentThread() != permissionExpireThread)
                        break;

                    long now = System.currentTimeMillis();

                    /*
                     * Has the current Thread been idle long enough to merit
                     * disposing of it?
                     */
                    if (permissions.isEmpty())
                    {
                        if (idleStartTime == -1)
                            idleStartTime = now;
                        else if (now - idleStartTime > 60 * 1000)
                            break;
                    }
                    else
                    {
                        // Expire the Permissions of this Allocation.

                        idleStartTime = -1;

                        for (Iterator<Permission> i =
                            permissions.values().iterator(); i.hasNext();)
                        {
                            Permission permission = i.next();

                            if (permission == null)
                            {
                                i.remove();
                            }
                            else if (permission.isExpired(now))
                            {
                                logger.debug("Permission " + permission
                                    + " expired");
                                i.remove();
                                permission.expire();
                            }
                        }
                    }
                }
            }
            while (true);
        }
        finally
        {
            synchronized (permissions)
            {
                if (permissionExpireThread == Thread.currentThread())
                    permissionExpireThread = null;
                /*
                 * If permissionExpireThread dies unexpectedly and yet it is
                 * still necessary, resurrect it.
                 */
                if (permissionExpireThread == null)
                    maybeStartPermissionExpireThread();
            }
        }
    }

    @Override
    public int hashCode()
    {
        return this.fiveTuple.hashCode();
    }

    /**
     * Since an Allocation is uniquely identified by its relay address or five
     * tuple hence we only compare these members.
     */
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Allocation))
        {
            return false;
        }
        Allocation allocation = (Allocation) o;
        if (!this.fiveTuple.equals(allocation.fiveTuple))
        {
            return false;
        }
        if (!this.relayAddress.equals(allocation.relayAddress))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return this.getRelayAddress().toString();
    }
 
}
