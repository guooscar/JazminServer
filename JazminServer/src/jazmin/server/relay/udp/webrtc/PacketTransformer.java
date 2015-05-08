package jazmin.server.relay.udp.webrtc;
/**
 * 
 * Code derived and adapted from the Jitsi client side SRTP framework.
 * 
 * Distributed under LGPL license.
 * See terms of license at gnu.org.
 */



/**
 * Encapsulate the concept of packet transformation. Given a packet,
 * <tt>PacketTransformer</tt> can either transform it or reverse the
 * transformation.
 * 
 * @author Bing SU (nova.su@gmail.com)
 * @author Ivelin Ivanov (ivelin.ivanov@telestax.com)
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 */
public interface PacketTransformer {
	/**
	 * Transforms a non-secure packet.
	 * 
	 * @param pkt
	 *            the packet to be transformed
	 * @return The transformed packet. Returns null if the packet cannot be transformed.
	 */
	public byte[] transform(byte[] pkt);

	/**
	 * Transforms a specific non-secure packet.
	 * 
	 * @param pkt
	 *            The packet to be secured
	 * @param offset
	 *            The offset of the packet data
	 * @param length
	 *            The length of the packet data
	 * @return The transformed packet. Returns null if the packet cannot be
	 *         transformed.
	 */
	public byte[] transform(byte[] pkt, int offset, int length);

	/**
	 * Reverse-transforms a specific packet (i.e. transforms a transformed
	 * packet back).
	 * 
	 * @param pkt
	 *            the transformed packet to be restored
	 * @return Whether the packet was successfully restored
	 */
	public byte[] reverseTransform(byte[] pkt);

	/**
	 * Reverse-transforms a specific packet (i.e. transforms a transformed
	 * packet back).
	 * 
	 * @param pkt
	 *            the packet to be restored
	 * @param offset
	 *            the offset of the packet data
	 * @param length
	 *            the length of data in the packet
	 * @return The restored packet. Returns null if packet cannot be restored.
	 */
	public byte[] reverseTransform(byte[] pkt, int offset, int length);

	/**
	 * Close the transformer and underlying transform engine.
	 * 
	 * The close functions closes all stored crypto contexts. This deletes key
	 * data and forces a cleanup of the crypto contexts.
	 */
	public void close();
}
