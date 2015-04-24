package jazmin.server.sip.io.pkts.packet.sip.header;

import jazmin.server.sip.io.pkts.buffer.Buffer;
import jazmin.server.sip.io.pkts.buffer.Buffers;
import jazmin.server.sip.io.pkts.packet.sip.SipParseException;
import jazmin.server.sip.io.pkts.packet.sip.address.Address;
import jazmin.server.sip.io.pkts.packet.sip.address.SipURI;
import jazmin.server.sip.io.pkts.packet.sip.header.impl.ContactHeaderImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ContactHeader extends AddressParametersHeader {

    Buffer NAME = Buffers.wrap("Contact");

    @Override
    ContactHeader clone();

    static Builder with() {
        return new Builder();
    }

    static Builder with(final Address address) throws SipParseException {
        final Builder builder = new Builder();
        builder.address(address);
        return builder;
    }

    static Builder with(final SipURI uri) throws SipParseException {
        final Builder builder = new Builder();
        final Address address = Address.with(uri.clone()).build();
        builder.address(address);
        return builder;
    }

    /**
     * Frame the value as a {@link ContactHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    public static ContactHeader frame(final Buffer buffer) throws SipParseException {
        final Object[] result = AddressParametersHeader.frame(buffer);
        return new ContactHeaderImpl((Address) result[0], (Buffer) result[1]);
    }

    static class Builder extends AddressParametersHeader.Builder<ContactHeader> {

        private Builder() {
            super(NAME);
        }

        @Override
        public ContactHeader internalBuild(final Address address, final Buffer params) throws SipParseException {
            return new ContactHeaderImpl(address, params);
        }
    }

}
