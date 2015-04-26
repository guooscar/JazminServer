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
 * The class would represent the FiveTuple object of TURN protocol. The hashCode
 * function should be unique if possible since it will be used to uniquely find
 * the allocation object.
 * 
 * @author Aakash Garg
 */
public class FiveTuple
{

    /**
     * Represents the Client's Transport Address.
     */
    protected TransportAddress clientTransportAddress;

    /**
     * Represents the Server's Transport Address.
     */
    protected TransportAddress serverTransportAddress;

    /**
     * Represents the Transport Protocol.
     */
    protected Transport transport = Transport.UDP;

    /**
     * Creates a new Five tuple Object with given arguments.
     * 
     * @param clientTransportAddress The client's Address to be set
     * @param serverTransportAddress The server's Address to be set
     * @param transport The transport protocol of the client server connection.
     */
    public FiveTuple(TransportAddress clientAddress,
        TransportAddress serverAddress, Transport transport)
    {
        this.clientTransportAddress = clientAddress;
        this.serverTransportAddress = serverAddress;
        this.transport = transport;
    }

    /**
     * @return the clientTransportAddress or null if the the Client's Address
     *         has not been set.
     */
    public TransportAddress getClientTransportAddress()
    {
        return clientTransportAddress;
    }

    /**
     * @return client's port number as +ve int value or -1 if the the Client's
     *         Address has not been set.
     */
    public int getClientPortNo()
    {
        if (this.clientTransportAddress != null)
        {
            return this.clientTransportAddress.getPort();
        }
        return -1;
    }

    /**
     * @return client Host Address in String or null if the the Client's Address
     *         has not been set.
     */
    public String getClientHostAddress()
    {
        if (this.clientTransportAddress != null)
        {
            return this.clientTransportAddress.getHostAddress();
        }
        else
        {
            return null;
        }
    }

    /**
     * @param clientTransportAddress The client's IP Address to be set.
     */
    public void setClientTransportAddress(TransportAddress clientAddress)
    {
        this.clientTransportAddress = clientAddress;
    }

    /**
     * @return the serverTransportAddress or null if the the Server's Address
     *         has not been set.
     */
    public TransportAddress getServerTransportAddress()
    {
        return serverTransportAddress;
    }

    /**
     * @return server's port number as +ve int value or -1 if the the Server's
     *         Address has not been set.
     */
    public int getServerPortNo()
    {
        if (this.serverTransportAddress != null)
        {
            return this.serverTransportAddress.getPort();
        }
        else
        {
            return -1;
        }
    }

    /**
     * @return server Host Address in String or null if the the Server's Address
     *         has not been set.
     */
    public String getServerHostAddress()
    {
        if (this.serverTransportAddress != null)
        {
            return this.serverTransportAddress.getHostAddress();
        }
        else
        {
            return null;
        }
    }

    /**
     * @param serverTransportAddress The Server's Address to be set.
     */
    public void setServerTransportAddress(TransportAddress serverAddress)
    {
        this.serverTransportAddress = serverAddress;
    }

    /**
     * @return the transport protocol used for client server connection.
     */
    public Transport getTransport()
    {
        return transport;
    }

    /**
     * @param transport the transport to set.
     */
    public void setTransport(Transport transport)
    {
        this.transport = transport;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result =
            prime
                * result
                + ((clientTransportAddress == null) ? 0
                    : clientTransportAddress.hashCode());
        result =
            prime
                * result
                + ((serverTransportAddress == null) ? 0
                    : serverTransportAddress.hashCode());
        result =
            prime * result + ((transport == null) ? 0 : transport.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof FiveTuple))
        {
            return false;
        }
        FiveTuple other = (FiveTuple) obj;
        if (clientTransportAddress == null)
        {
            if (other.clientTransportAddress != null)
            {
                return false;
            }
        }
        else if (!clientTransportAddress.equals(other.clientTransportAddress))
        {
            return false;
        }
        if (serverTransportAddress == null)
        {
            if (other.serverTransportAddress != null)
            {
                return false;
            }
        }
        else if (!serverTransportAddress.equals(other.serverTransportAddress))
        {
            return false;
        }
        if (transport != other.transport)
        {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "FiveTuple ["
            + (getClientTransportAddress() != null ? "getClientAddress()="
                + getClientTransportAddress() + ", " : "")
            + (getServerTransportAddress() != null ? "getServerAddress()="
                + getServerTransportAddress() + ", " : "")
            + (getTransport() != null ? "getTransport()=" + getTransport()
                + ", " : "") + "hashCode()=" + hashCode() + "]";
    }
}
