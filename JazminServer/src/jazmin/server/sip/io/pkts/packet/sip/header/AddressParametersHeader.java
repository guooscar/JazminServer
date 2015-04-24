/**
 * 
 */
package jazmin.server.sip.io.pkts.packet.sip.header;

import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static jazmin.server.sip.io.pkts.packet.sip.impl.PreConditions.assertNotNull;

import java.io.IOException;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.address.Address;
import jazmin.server.sip.io.pkts.packet.sip.address.SipURI;
import jazmin.server.sip.io.pkts.packet.sip.address.impl.AddressImpl;
import jazmin.server.sip.io.pkts.packet.sip.header.impl.AddressParametersHeaderImpl;
import jazmin.server.sip.io.pkts.packet.sip.header.impl.ParametersSupport;
import jazmin.server.sip.io.pkts.packet.sip.impl.SipParser;

/**
 * @author jonas@jonasborjesson.com
 */
public interface AddressParametersHeader extends SipHeader, HeaderAddress, Parameters {

    /**
     * Frame the value as a {@link AddressParametersHeaderImpl}. This method assumes that you have
     * already parsed out the actual header name, e.g. "To: ". Also, this method assumes that a
     * message framer (or similar) has framed the buffer that is being passed in to us to only
     * contain this header and nothing else.
     * 
     * Note, as with all the frame-methods on all headers/messages/whatever, they do not do any
     * validation that the information is actually correct. This method will simply only try and
     * validate just enough to get the framing done.
     * 
     * @param value
     * @return an array where the first object is a {@link Address} object and the second is a
     *         {@link Buffer} with all the parameters.
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    public static Object[] frame(final Buffer buffer) throws SipParseException {
        try {
            final Address address = AddressImpl.parse(buffer);
            // we assume that the passed in buffer ONLY contains
            // this header and nothing else. Therefore, there are only
            // header parameters left after we have consumed the address
            // portion.
            Buffer params = null;
            if (buffer.hasReadableBytes()) {
                params = buffer.slice();
            }
            return new Object[] {address, params};
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to process the value due to a IndexOutOfBoundsException", e);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to process the To-header to due an IOException");
        }
    }

    static <T> Builder<AddressParametersHeader> with(final Buffer headerName)
            throws SipParseException {
        assertNotEmpty(headerName, "The name of the header cannot be null or the empty buffer");
        return new Builder<AddressParametersHeader>(headerName);
    }

    static class Builder<T extends AddressParametersHeader> {
        private final Buffer name;

        private SipURI.Builder uriBuilder;

        private Address address;

        private final ParametersSupport paramSupport = new ParametersSupport(null);

        protected Builder(final Buffer name) {
            this.name = name;
        }


        public final Builder<T> port(final int port) {
            ensureBuilder().port(port);
            return this;
        }

        /**
         * Set the user portion of the {@link ToHeader}. Since the user portion may in fact be null
         * (or empty), any value is accepted but of course, a value of null or empty will lead to no
         * user portion of the SIP-URI within the header.
         * 
         * @param user
         * @return
         */
        public final Builder<T> user(final Buffer user) {
            ensureBuilder().user(user);
            return this;
        }

        public final Builder<T> user(final String user) {
            ensureBuilder().user(user);
            return this;
        }

        /**
         * Use this host for the ToHeader.
         * 
         * NOTE: you can only specify either an address or a host and user but not both. If you do,
         * an exception will occur at the time you try and {@link #build()} the header.
         * 
         * @param host
         * @return
         */
        public final Builder<T> host(final Buffer host) {
            ensureBuilder().host(host);
            return this;
        }

        public final Builder<T> host(final String host) {
            ensureBuilder().host(host);
            return this;
        }

        /**
         * Set a parameter on the header.
         * 
         * NOTE: if you want to set a parameter on the URI you need to use the method
         * {@link #uriParameter(Buffer, Buffer)}.
         * 
         * @param name
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder<T> parameter(final Buffer name, final Buffer value) throws SipParseException,
        IllegalArgumentException {
            this.paramSupport.setParameter(name, value);
            return this;
        }

        public Builder<T> parameter(final String name, final String value) throws SipParseException,
        IllegalArgumentException {
            this.paramSupport.setParameter(name, value);
            return this;
        }

        /**
         * Set a parameter on the underlying {@link SipURI}.
         * 
         * @param name
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder<T> uriParameter(final Buffer name, final Buffer value) throws SipParseException,
        IllegalArgumentException {
            ensureBuilder().parameter(name, value);
            return this;
        }

        /**
         * Set a parameter on the underlying {@link SipURI}.
         * 
         * @param name
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder<T> uriParameter(final String name, final String value) throws SipParseException,
        IllegalArgumentException {
            ensureBuilder().parameter(name, value);
            return this;
        }

        /**
         * Set the transport parameter on the underlying {@link SipURI} to be "tcp".
         * 
         * @return
         * @throws SipParseException
         */
        public Builder<T> transportTCP() throws SipParseException {
            ensureBuilder().parameter(SipParser.TRANSPORT, SipParser.TCP);
            return this;
        }

        /**
         * Set the transport parameter on the underlying {@link SipURI} to be "udp".
         * 
         * @return
         * @throws SipParseException
         */
        public Builder<T> transportUDP() throws SipParseException {
            ensureBuilder().parameter(SipParser.TRANSPORT, SipParser.UDP);
            return this;
        }

        /**
         * Set the transport parameter on the underlying {@link SipURI} to be "tls".
         * 
         * @return
         * @throws SipParseException
         */
        public Builder<T> transportTLS() throws SipParseException {
            ensureBuilder().parameter(SipParser.TRANSPORT, SipParser.TLS);
            return this;
        }

        /**
         * Set the transport parameter on the underlying {@link SipURI} to be "sctp".
         * 
         * @return
         * @throws SipParseException
         */
        public Builder<T> transportSCTP() throws SipParseException {
            ensureBuilder().parameter(SipParser.TRANSPORT, SipParser.SCTP);
            return this;
        }

        /**
         * Set the transport parameter on the underlying {@link SipURI} to be "ws".
         * 
         * @return
         * @throws SipParseException
         */
        public Builder<T> transportWS() throws SipParseException {
            ensureBuilder().parameter(SipParser.TRANSPORT, SipParser.WS);
            return this;
        }

        /**
         * Use this address for the ToHeader.
         * 
         * NOTE: you can only specify either an address or a host and user but not both. If you do,
         * an exception will occur at the time you try and {@link #build()} the header.
         * 
         * @param address
         * @return
         */
        public final Builder<T> address(final Address address) {
            this.address = assertNotNull(address, "Address cannot be null");
            return this;
        }

        /**
         * Build a new ToHeader.
         * 
         * @return
         * @throws SipParseException in case anything goes wrong while constructing the
         *         {@link ToHeader}.
         */
        public final T build() throws SipParseException {
            if (uriBuilder != null && address != null) {
                throw new SipParseException(
                        "You specified both an address as well as parts of a SipURI. Not sure which to choose");
            } else if (uriBuilder == null && address == null) {
                throw new SipParseException("You must sepcify either the host or a full address.");
            }

            Address addressToUse = this.address;
            if (uriBuilder != null) {
                final SipURI uri = uriBuilder.build();
                addressToUse = Address.with(uri).build();
            }
            return internalBuild(addressToUse, this.paramSupport.toBuffer());
        }

        @SuppressWarnings("unchecked")
        protected T internalBuild(final Address address, final Buffer params) {
            return (T) new AddressParametersHeaderImpl(name, address, params);
        }

        private SipURI.Builder ensureBuilder() {
            if (uriBuilder == null) {
                this.uriBuilder = SipURI.with();
            }
            return this.uriBuilder;
        }

    }

}
