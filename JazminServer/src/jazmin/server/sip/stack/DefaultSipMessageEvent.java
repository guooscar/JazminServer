/**
 * 
 */
package jazmin.server.sip.stack;

import io.pkts.packet.sip.SipMessage;

/**
 * @author jonas@jonasborjesson.com
 */
public class DefaultSipMessageEvent implements SipMessageEvent {

    private final Connection connection;
    private final SipMessage msg;
    private final long arrivalTime;

    /**
     * 
     */
    public DefaultSipMessageEvent(final Connection connection, final SipMessage msg, final long arrivalTime) {
        this.connection = connection;
        this.msg = msg;
        this.arrivalTime = arrivalTime;
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public SipMessage getMessage() {
        return this.msg;
    }

    @Override
    public long getArrivalTime() {
        return this.arrivalTime;
    }

}
