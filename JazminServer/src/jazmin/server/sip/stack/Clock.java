/**
 * 
 */
package jazmin.server.sip.stack;

/**
 * Simple interface for representing the system time.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Clock {

    /**
     * Get the current time in milliseconds since midnight, Jan 1, 1970 UTC.
     * 
     * @return
     */
    long getCurrentTimeMillis();

}
