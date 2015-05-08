package jazmin.server.relay.udp.webrtc;
/**
 * 
 * Code derived and adapted from the Jitsi client side SRTP framework.
 * 
 * Distributed under LGPL license.
 * See terms of license at gnu.org.
 */


import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * SRTCPTransformer implements PacketTransformer.
 * It encapsulate the encryption / decryption logic for SRTCP packets
 * 
 * @author Bing SU (nova.su@gmail.com)
 * @author Werner Dittmann &lt;Werner.Dittmann@t-online.de>
 */
public class SRTCPTransformer implements PacketTransformer {
	
	private final RawPacket packet;
	
    private SRTPTransformEngine forwardEngine;
    private SRTPTransformEngine reverseEngine;

    /** All the known SSRC's corresponding SRTCPCryptoContexts */
    private Hashtable<Long,SRTCPCryptoContext> contexts;

    /**
     * Constructs a SRTCPTransformer object.
     * 
     * @param engine The associated SRTPTransformEngine object for both
     *            transform directions.
     */
    public SRTCPTransformer(SRTPTransformEngine engine)
    {
        this(engine, engine);
    }

    /**
     * Constructs a SRTCPTransformer object.
     * 
     * @param forwardEngine The associated SRTPTransformEngine object for
     *            forward transformations.
     * @param reverseEngine The associated SRTPTransformEngine object for
     *            reverse transformations.
     */
    public SRTCPTransformer(SRTPTransformEngine forwardEngine, SRTPTransformEngine reverseEngine) {
    	this.packet = new RawPacket();
        this.forwardEngine = forwardEngine;
        this.reverseEngine = reverseEngine;
        this.contexts = new Hashtable<Long,SRTCPCryptoContext>();
    }

    /**
     * Encrypts a SRTCP packet
     * 
     * @param pkt plain SRTCP packet to be encrypted
     * @return encrypted SRTCP packet
     */
    public byte[] transform(byte[] pkt) {
    	return transform(pkt, 0, pkt.length);
    }
    
    public byte[] transform(byte[] pkt, int offset, int length) {
    	// Wrap the data into raw packet for readable format
    	this.packet.wrap(pkt, offset, length);
    	
    	// Associate the packet with its encryption context
        long ssrc = this.packet.getRTCPSSRC();
        SRTCPCryptoContext context = contexts.get(ssrc);

        if (context == null) {
            context = forwardEngine.getDefaultContextControl().deriveContext(ssrc);
            context.deriveSrtcpKeys();
            contexts.put(ssrc, context);
        }
        
        // Secure packet into SRTCP format
        context.transformPacket(packet);
        return packet.getData();
    }

    public byte[] reverseTransform(byte[] pkt) {
    	return reverseTransform(pkt, 0, pkt.length);
    }
    
    public byte[] reverseTransform(byte[] pkt, int offset, int length) {
    	// wrap data into raw packet for readable format
    	this.packet.wrap(pkt, offset, length);
    	
    	// Associate the packet with its encryption context
        long ssrc = this.packet.getRTCPSSRC();
        SRTCPCryptoContext context = this.contexts.get(ssrc);

        if (context == null) {
            context = reverseEngine.getDefaultContextControl().deriveContext(ssrc);
            context.deriveSrtcpKeys();
            contexts.put(new Long(ssrc), context);
        }
        
        // Decode packet to RTCP format
        boolean reversed = context.reverseTransformPacket(packet);
        if(reversed) {
        	return packet.getData();
        }
        return null;
    }

    /**
     * Close the transformer and underlying transform engine.
     * 
     * The close functions closes all stored crypto contexts. This deletes key data 
     * and forces a cleanup of the crypto contexts.
     */
    public void close() 
    {
        forwardEngine.close();
        if (forwardEngine != reverseEngine)
            reverseEngine.close();

        Iterator<Map.Entry<Long, SRTCPCryptoContext>> iter
            = contexts.entrySet().iterator();

        while (iter.hasNext()) 
        {
            Map.Entry<Long, SRTCPCryptoContext> entry = iter.next();
            SRTCPCryptoContext context = entry.getValue();

            iter.remove();
            if (context != null)
                context.close();
        }
    }
}
