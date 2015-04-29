/**
 * 
 */
package jazmin.server.sip.io.sip.header;

import java.util.function.Supplier;

import jazmin.server.sip.io.buffer.Buffer;
import jazmin.server.sip.io.sip.SipParseException;


/**
 * @author jonas@jonasborjesson.com
 */
public interface Parameters extends SipHeader {

    /**
     * Get the value of the named parameter. If the named parameter is a
     * so-called flag parameter, then the value returned will be an empty
     * {@link Buffer}, which can be checked with {@link Buffer#isEmpty()} or
     * {@link Buffer#capacity()}, which will return zero. As with any empty
     * {@link Buffer}, if you do {@link Buffer#toString()} you will be getting
     * an empty {@link String} back, which would be yet another way to check for
     * a flag parameter.
     * 
     * @param name
     *            the name of the parameter we are looking for.
     * @return the value of the named parameter or null if there is no such
     *         parameter. If the named parameter is a flag parameter, then an
     *         empty buffer will be returned.
     * @throws SipParseException
     *             in case anything goes wrong while extracting the parameter.
     * @throws IllegalArgumentException
     *             in case the name is null.
     */
    Buffer getParameter(Buffer name) throws SipParseException, IllegalArgumentException;

    /**
     * Same as {@link #getParameter(Buffer)}.
     * 
     * @param name
     * @return
     * @throws SipParseException
     *             in case anything goes wrong while extracting the parameter.
     * @throws IllegalArgumentException
     *             in case the name is null.
     */
    Buffer getParameter(String name) throws SipParseException, IllegalArgumentException;

    /**
     * Sets the value of the specified parameter. If there already is a
     * parameter with the same name its value will be overridden.
     * 
     * A value of null or a zero length buffer means that this parameter is a
     * flag parameter
     * 
     * @param name
     *            the name of the parameter
     * @param value
     *            the value of the parameter or null if you just want to set a
     *            flag parameter
     * @throws SipParseException
     *             in case anything goes wrong when setting the parameter.
     * @throws IllegalArgumentException
     *             in case the name is null or empty.
     */
    void setParameter(Buffer name, Buffer value) throws SipParseException, IllegalArgumentException;

    void setParameter(Buffer name, Supplier<Buffer> value) throws SipParseException, IllegalArgumentException;

}
