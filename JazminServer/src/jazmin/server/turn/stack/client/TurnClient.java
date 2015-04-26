/*
 * TurnServer, the OpenSource Java Solution for TURN protocol. Maintained by the
 * Jitsi community (http://jitsi.org).
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package jazmin.server.turn.stack.client;

import java.net.InetAddress;

import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.stack.StunStack;
import org.ice4j.stunclient.NetworkConfigurationDiscoveryProcess;
import org.ice4j.stunclient.StunDiscoveryReport;

/**
 * The class would be acting as a TurnClient. The client runs on UDP initiates
 * displays network configuration <br>
 * Usage from command prompt <br>
 * 
 * java org.ice4j.Turnclient.TurnCient Turnserver.org TurnPort <br>
 * (Displays the network configuration and port mapping using Turnserver.org as
 * Turnserver and TurnPort as server's Turn port<br>
 * * java org.ice4j.Turnclient.TurnCient N <br>
 * (chooses server from array of { "Turnserver.org", "Turn.softjoys.com",
 * "Turn.voiparound.com", "Turn.voipbuster.com", "Turn.voipTurnt.com",
 * "Turn.voxgratia.org", "Turn.ekiga.net", "Turn.ideasip.com", "Turn.iptel.org",
 * "Turn.rixtelecom.se" })
 * 
 * @author Aakash
 * 
 */

public class TurnClient
{
    /**
     * @param args
     */
    public static StunDiscoveryReport getReport(String... args)
        throws Exception
    {
        TransportAddress localAddr = null;
        TransportAddress serverAddr = null;
        Transport protocol = Transport.UDP;
        int serverUdpPort = 3478;
        
        String[] server =
            { "stunserver.org", "stun.softjoys.com", "stun.voiparound.com",
                "stun.voipbuster.com", "stun.voipstunt.com",
                "stun.voxgratia.org", "stun.ekiga.net", "stun.ideasip.com",
                "stun.iptel.org", "stun.rixtelecom.se" };

        String[] temp = {"120.57.226.103","26768"};
        String[] temp2 = {"127.0.0.1","3478"};
        args  = temp2;
        if (args.length == 4)
        {
            // uses args[0] and args[1] as server name and port and args[2] and
            // args[3] as client ip and port
            localAddr = new TransportAddress(args[2], Integer.valueOf(
                args[3]).intValue(), protocol);
            serverUdpPort = Integer.valueOf(
                args[1]).intValue();
            serverAddr = new TransportAddress(args[0], serverUdpPort, protocol);
        }
        else if (args.length == 2)
        {
            // uses args as server name and port
            localAddr =
                new TransportAddress(InetAddress.getLocalHost(), 5678, protocol);
            serverAddr = new TransportAddress(args[0], Integer.valueOf(
                args[1]).intValue(), protocol);
        }
        else if (args.length == 1)
        {
            // chooses a server indexed by N
            localAddr =
                new TransportAddress(InetAddress.getLocalHost(), 5678, protocol);
            serverAddr =
                new TransportAddress(server[Integer.parseInt(args[0])],
                    serverUdpPort, protocol);
            // serverAddr = new
            // TransportAddress(InetAddress.getLocalHost(),serverUdpPort,protocol);
            System.out.println("Stun Server - "
                + server[Integer.parseInt(args[0])] + ":" + serverUdpPort);
        }
        else
        {
            // runs server running on same computer
            localAddr =
                new TransportAddress(InetAddress.getLocalHost(), 5678, protocol);
            // serverAddr = new TransportAddress(
            // server[0],serverUdpPort,protocol);
            serverAddr =
                new TransportAddress(InetAddress.getLocalHost(), serverUdpPort,
                    protocol);
        }

        NetworkConfigurationDiscoveryProcess addressDiscovery =
            new NetworkConfigurationDiscoveryProcess(new StunStack(),
                localAddr, serverAddr);

        addressDiscovery.start();
        StunDiscoveryReport report = addressDiscovery.determineAddress();
        addressDiscovery.shutDown();
        return report;
    }

    public static void main(String... args) throws Exception
    {
        StunDiscoveryReport report = TurnClient.getReport(args);
        System.out.println(report);
    }

}
