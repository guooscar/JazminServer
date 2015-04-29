/**
 * 
 */
package jazmin.server.sip.io.sip.address;

import static jazmin.server.sip.io.sip.impl.PreConditions.assertArgument;
import static jazmin.server.sip.io.sip.impl.PreConditions.assertNotEmpty;
import static jazmin.server.sip.io.sip.impl.PreConditions.assertNotNull;
import static jazmin.server.sip.io.sip.impl.PreConditions.ifNull;
import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.buffer.Buffers;
import jazmin.server.sip.io.sip.SipParseException;
import jazmin.server.sip.io.sip.address.impl.AddressImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Address {

    /**
     * Get the display name of this {@link Address} or an empty buffer if it is
     * not set.
     * 
     * @return
     */
    Buffer getDisplayName();

    /**
     * Get the {@link URI} of this {@link Address}.
     * 
     * @return the {@link URI}
     * @throws SipParseException
     */
    URI getURI() throws SipParseException;

    /**
     * Get the {@link Address} as a raw buffer.
     * 
     * @return
     */
    Buffer toBuffer();

    void getBytes(Buffer dst);

    static AddressBuilder with() {
        return new AddressBuilder();
    }

    static AddressBuilder with(final URI uri) {
        return new AddressBuilder(assertNotNull(uri, "URI cannot be null"));
    }

    static class AddressBuilder {

        private URI uri;
        private Buffer displayName;

        // the user may not want to first go off and create a SipURI
        // only to pass it in here, it is annoying and the most common
        // use case is to create an address with a SipURI "in" it. Hence,
        // the user should be able to do so without having to call
        // a gazzilion other crap just to make that use case happen.
        private Buffer user;
        private Buffer host;
        private int port = -1;

        private AddressBuilder() {
            // left empty intentionally
        }

        private AddressBuilder(final URI uri) {
            this.uri = uri;
        }

        /**
         * Use this user for the {@link SipURI} that will be part of this {@link Address}. See
         * {@link #host(Buffer)} for more information.
         * 
         * @param user
         * @return
         */
        public AddressBuilder user(final Buffer user) {
            this.user = user;
            return this;
        }

        /**
         * Use this port for the {@link SipURI} that will be part of this {@link Address}. See
         * {@link #host(Buffer)} for more information.
         * 
         * @param port
         * @return
         */
        public AddressBuilder port(final int port) {
            assertArgument(port > 0 || port == -1, "Port must be greater than zero or negative one (use default)");
            this.port = port;
            return this;
        }

        /**
         * Use this host for the URI with this {@link Address}. This is a convenient way of creating
         * an {@link Address} with a {@link SipURI} and is the same as:
         * 
         * <pre>
         * SipURI uri = SipURI.with().host(host).build();
         * Address.with(uri);
         * </pre>
         * 
         * NOTE: you cannot specify a host and also specify a URI since those would conflict and an
         * exception will occur at the time you try and {@link #build()} the address.
         * 
         * @param host
         * @return
         */
        public AddressBuilder host(final Buffer host) {
            this.host = assertNotEmpty(host, "host cannot be empty or null");
            return this;
        }

        public AddressBuilder displayName(final Buffer displayName) {
            this.displayName = ifNull(displayName, Buffers.EMPTY_BUFFER);
            return this;
        }

        public AddressBuilder displayName(final String displayName) {
            this.displayName = Buffers.wrap(ifNull(displayName, ""));
            return this;
        }

        public AddressBuilder uri(final URI uri) {
            this.uri = assertNotNull(uri, "URI cannot be null");
            return this;
        }

        public Address build() throws SipParseException {
            if (this.host != null && this.uri != null) {
                throw new SipParseException("Both host and URI was specified. Not sure which to pick");
            }

            if (this.host == null && this.uri == null) {
                throw new SipParseException("You must specify either a full address or a host");
            }

            URI uriToUse = this.uri;
            if (this.host != null) {
                uriToUse = SipURI.with().user(user).host(host).port(port).build();
            }
            return new AddressImpl(this.displayName, uriToUse);
        }

    }

}
