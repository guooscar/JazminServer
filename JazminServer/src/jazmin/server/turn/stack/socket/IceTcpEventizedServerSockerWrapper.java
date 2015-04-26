/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.socket;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import org.ice4j.*;
import org.ice4j.ice.*;
import org.ice4j.socket.*;

/**
 * Class for eventized TCP server Socket where event is when someone tries to
 * connect to the given server Socket of the class.
 * 
 * @author Aakash Garg
 * 
 */
public class IceTcpEventizedServerSockerWrapper
    extends IceSocketWrapper
    implements TcpConnectEventGenerator
{

    /**
     * The <tt>Logger</tt> used by the <tt>LocalCandidate</tt> class and its
     * instances for logging output.
     */
    private static Logger logger = Logger
        .getLogger(IceTcpEventizedServerSockerWrapper.class.getName());

    /**
     * Thread that will wait new connections.
     */
    private Thread acceptThread = null;

    /**
     * The wrapped TCP ServerSocket.
     */
    private final ServerSocket serverSocket;

    /**
     * If the socket is still listening.
     */
    private boolean isRun = false;

    private TcpConnectEventListener listener;

    /**
     * STUN stack.
     */
    private final Component component;

    /**
     * List of TCP client sockets.
     */
    private final List<Socket> sockets = new ArrayList<Socket>();

    public IceTcpEventizedServerSockerWrapper(ServerSocket serverSocket,
        Component component)
    {
        this.serverSocket = serverSocket;
        this.component = component;
        acceptThread = new ThreadAccept();
        acceptThread.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DatagramPacket p) throws IOException
    {
        System.err.println("Send called in IceTcpServerSocketWrapper.");
        /* Do nothing for the moment */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receive(DatagramPacket p) throws IOException
    {
        System.err.println("Receive called in IceTcpServerSocketWrapper.");
        /* Do nothing for the moment */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        try
        {
            isRun = false;
            serverSocket.close();
            for (Socket s : sockets)
            {
                s.close();
            }
        }
        catch (IOException e)
        {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress getLocalAddress()
    {
        return serverSocket.getInetAddress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLocalPort()
    {
        return serverSocket.getLocalPort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SocketAddress getLocalSocketAddress()
    {
        return serverSocket.getLocalSocketAddress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket getTCPSocket()
    {
        if (sockets.size() > 0)
        {
            return sockets.get(0);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatagramSocket getUDPSocket()
    {
        return null;
    }

    @Override
    public void setEventListener(TcpConnectEventListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void removeEventListener()
    {
        this.listener = null;
    }

    @Override
    public void fireConnectEvent(TcpConnectEvent event)
    {
        if (this.listener != null)
            this.listener.onConnect(event);
        else
            logger.finest("Listener not registered");
    }

    /**
     * Thread that will wait for new TCP connections.
     * 
     */
    private class ThreadAccept
        extends Thread
    {
        /**
         * Thread entry point.
         */
        @Override
        public void run()
        {
            isRun = true;

            while (isRun)
            {
                try
                {
                    Socket tcpSocket = serverSocket.accept();

                    if (tcpSocket != null)
                    {
                        MultiplexingSocket multiplexingSocket =
                            new MultiplexingSocket(tcpSocket);
                        component.getParentStream().getParentAgent()
                            .getStunStack().addTcpSocket(
                                new IceTcpSocketWrapper(multiplexingSocket));

                        sockets.add(multiplexingSocket);
                        String[] serverIpPort =
                            serverSocket.getLocalSocketAddress().toString()
                                .replaceAll(
                                    "/", "").split(
                                    ":");
                        TransportAddress localAddr =
                            new TransportAddress(InetAddress.getLocalHost(),
                                Integer.parseInt(serverIpPort[1]),
                                Transport.TCP);
                        
                        String[] remoteIpPort =
                            tcpSocket.getRemoteSocketAddress().toString()
                                .replaceAll(
                                    "/", "").split(
                                    ":");
                        TransportAddress remoteAddr =
                            new TransportAddress(remoteIpPort[0],
                                Integer.parseInt(remoteIpPort[1]),
                                Transport.TCP);
                        logger.finest("Connection Request from "+remoteAddr+" to "+localAddr);
                        TcpConnectEvent event =
                            new TcpConnectEvent(localAddr, remoteAddr);
                        IceTcpEventizedServerSockerWrapper.this
                            .fireConnectEvent(event);
                    }
                }
                catch (IOException e)
                {
                    logger.info("Failed to accept TCP socket " + e);
                }
            }
        }
    }

}
