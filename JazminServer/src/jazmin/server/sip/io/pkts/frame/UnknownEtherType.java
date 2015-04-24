/**
 * 
 */
package jazmin.server.sip.io.pkts.frame;

/**
 * @author jonas@jonasborjesson.com
 */
public final class UnknownEtherType extends Exception {

    private static final long serialVersionUID = 1L;

    private final byte b1;
    private final byte b2;

    public UnknownEtherType(final byte b1, final byte b2) {
        this.b1 = b1;
        this.b2 = b2;
    }

}
