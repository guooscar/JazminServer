package jazmin.server.relay.udp.webrtc;
/**
 * 
 * Code derived and adapted from the Jitsi client side SRTP framework.
 * 
 * Distributed under LGPL license.
 * See terms of license at gnu.org.
 */


import java.util.Hashtable;

/**
 * SRTPTransformer implements PacketTransformer and provides implementations for
 * RTP packet to SRTP packet transformation and SRTP packet to RTP packet
 * transformation logic.
 * 
 * It will first find the corresponding SRTPCryptoContext for each packet based
 * on their SSRC and then invoke the context object to perform the
 * transformation and reverse transformation operation.
 * 
 * @author Bing SU (nova.su@gmail.com)
 * @author Ivelin Ivanov (ivelin.ivanov@telestax.com)
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 */
public class SRTPTransformer implements PacketTransformer {
	
	
	private final RawPacket rawPacket;
	
	private SRTPTransformEngine forwardEngine;
	private SRTPTransformEngine reverseEngine;

	/**
	 * All the known SSRC's corresponding SRTPCryptoContexts
	 */
	private Hashtable<Long, SRTPCryptoContext> contexts;

	/**
	 * Constructs a SRTPTransformer object.
	 * 
	 * @param engine
	 *            The associated SRTPTransformEngine object for both transform
	 *            directions.
	 */
	public SRTPTransformer(SRTPTransformEngine engine) {
		this(engine, engine);
	}

	/**
	 * Constructs a SRTPTransformer object.
	 * 
	 * @param forwardEngine
	 *            The associated SRTPTransformEngine object for forward
	 *            transformations.
	 * @param reverseEngine
	 *            The associated SRTPTransformEngine object for reverse
	 *            transformations.
	 */
	public SRTPTransformer(SRTPTransformEngine forwardEngine, SRTPTransformEngine reverseEngine) {
		this.forwardEngine = forwardEngine;
		this.reverseEngine = reverseEngine;
		this.contexts = new Hashtable<Long, SRTPCryptoContext>();
		this.rawPacket = new RawPacket();
	}

	public byte[] transform(byte[] pkt) {
		return transform(pkt, 0, pkt.length);
	}
	
	public byte[] transform(byte[] pkt, int offset, int length) {
		// Updates the contents of raw packet with new incoming packet 
		this.rawPacket.wrap(pkt, offset, length);
		
		// Associate packet to a crypto context
		long ssrc = rawPacket.getSSRC();
		SRTPCryptoContext context = contexts.get(ssrc);

		if (context == null) {
			context = forwardEngine.getDefaultContext().deriveContext(ssrc, 0, 0);
			context.deriveSrtpKeys(0);
			contexts.put(ssrc, context);
		}

		// Transform RTP packet into SRTP
		context.transformPacket(this.rawPacket);
		return this.rawPacket.getData();
	}

	/**
	 * Reverse-transforms a specific packet (i.e. transforms a transformed
	 * packet back).
	 * 
	 * @param pkt
	 *            the transformed packet to be restored
	 * @return the restored packet
	 */
	public byte[] reverseTransform(byte[] pkt) {
		return reverseTransform(pkt, 0, pkt.length);
	}
	
	public byte[] reverseTransform(byte[] pkt, int offset, int length) {
		// Wrap data into the raw packet for readable format
		this.rawPacket.wrap(pkt, offset, length);
		
		// Associate packet to a crypto context
		long ssrc = this.rawPacket.getSSRC();
		SRTPCryptoContext context = this.contexts.get(ssrc);
		if (context == null) {
			context = this.reverseEngine.getDefaultContext().deriveContext(ssrc, 0, 0);
			context.deriveSrtpKeys(this.rawPacket.getSequenceNumber());
			contexts.put(ssrc, context);
		}

		boolean reversed = context.reverseTransformPacket(this.rawPacket);
		if(reversed) {
			return this.rawPacket.getData();
		}
		return null;
	}

	/**
	 * Close the transformer and underlying transform engine.
	 * 
	 * The close functions closes all stored crypto contexts. This deletes key
	 * data and forces a cleanup of the crypto contexts.
	 */
	public void close() {
		forwardEngine.close();
		if (forwardEngine != reverseEngine) {
			reverseEngine.close();
		}
		for (Long ssrc : contexts.keySet()) {
			SRTPCryptoContext context = contexts.get(ssrc);
			if (context != null) {
				context.close();
				contexts.remove(ssrc);
			}
		}
	}
	
}
