/**
 * 
 */
package jazmin.server.sip.io.pkts.sdp.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.sdp.Connection;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.sdp.RTPInfo;
import jazmin.server.sip.io.pkts.sdp.SDP;

/**
 * Initial implementation that wraps a {@link SessionDescription} object from
 * the javax.sdp package.
 * 
 * @author jonas@jonasborjesson.com
 */
public class SDPWrapper implements SDP {

    private final SessionDescription sdp;

    /**
     * 
     */
    public SDPWrapper(final SessionDescription sdp) {
        this.sdp = sdp;
    }

    @Override
    public Collection<RTPInfo> getRTPInfo() {
        final List<RTPInfo> list = new ArrayList<RTPInfo>();
        try {
            final Connection c = this.sdp.getConnection();
            @SuppressWarnings("unchecked")
            final Vector<MediaDescription> mds = this.sdp.getMediaDescriptions(false);
            if (mds != null) {
                for (final MediaDescription md : mds) {
                    final RTPInfo rtpInfo = processMediaDescription(c, md);
                    if (rtpInfo != null) {
                        list.add(rtpInfo);
                    }
                }
            }
        } catch (final SdpException e) {
            throw new RuntimeException("TODO: throw real exception", e);
        }
        return list;
    }

    /**
     * Convenience method for creating a new {@link RTPInfo} object.
     * 
     * @param connection
     *            the connection (the c-field) information from the SDP or null
     *            if there were none.
     * @param md
     *            the media description from the SDP
     * @return a new {@link RTPInfo} object if the {@link MediaDescription} was
     *         of type "RTP/AVP", otherwise null.
     * @throws SdpParseException
     */
    private RTPInfo processMediaDescription(final Connection connection, final MediaDescription md)
            throws SdpParseException {
        final Media m = md.getMedia();
        if ("RTP/AVP".equalsIgnoreCase(m.getProtocol())) {
            final Connection c = md.getConnection() != null ? null : connection;
            return new RTPInfoImpl(connection, md);
        }
        return null;
    }

    @Override
    public Buffer toBuffer() {
        return Buffers.wrap(this.sdp.toString());
    }

    @Override
    public String toString() {
        return this.sdp.toString();
    }

}
