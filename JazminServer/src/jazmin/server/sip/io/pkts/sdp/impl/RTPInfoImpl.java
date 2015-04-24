/**
 * 
 */
package jazmin.server.sip.io.pkts.sdp.impl;

import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.SdpParseException;

import jazmin.server.sip.io.pkts.sdp.RTPInfo;


/**
 * @author jonas@jonasborjesson.com
 */
public final class RTPInfoImpl implements RTPInfo {

    /**
     * The c-field (connection) of the SDP. If null, then the connection
     * information should be retrieved from the {@link MediaDescription}.
     */
    private final Connection connection;

    private final MediaDescription mediaDescription;

    public RTPInfoImpl(final Connection connection, final MediaDescription mediaDescription) {
        this.connection = mediaDescription.getConnection() != null ? mediaDescription.getConnection() : connection;
        this.mediaDescription = mediaDescription;
    }

    @Override
    public String toString() {
        return this.connection + this.mediaDescription.toString();
    }

    @Override
    public String getAddress() {
        try {
            if (this.connection != null) {
                return this.connection.getAddress();
            }

            return this.mediaDescription.getConnection().getAddress();
        } catch (final SdpParseException e) {
            throw new RuntimeException("TODO: real exception pls", e);
        }
    }

    public byte[] getRawAddress() {
        final String address = getAddress();
        final String[] parts = address.split("\\.");
        if (parts.length == 4) {
            try {
                final byte[] raw = new byte[4];
                raw[0] = (byte) Integer.parseInt(parts[0]);
                raw[1] = (byte) Integer.parseInt(parts[1]);
                raw[2] = (byte) Integer.parseInt(parts[2]);
                raw[3] = (byte) Integer.parseInt(parts[3]);
                return raw;
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("Address is not a raw IPv4 address");
            }
        }
        throw new IllegalArgumentException("Address is not a raw IPv4 address");
    }

    @Override
    public int getMediaPort() {
        try {
            return this.mediaDescription.getMedia().getMediaPort();
        } catch (final SdpParseException e) {
            throw new RuntimeException("TODO: real exception pls", e);
        }
    }

}
