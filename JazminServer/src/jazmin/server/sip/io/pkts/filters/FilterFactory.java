/**
 * 
 */
package jazmin.server.sip.io.pkts.filters;

/**
 * @author jonas@jonasborjesson.com
 */
public final class FilterFactory {

    private static final FilterFactory instance = new FilterFactory();

    public static final FilterFactory getInstance() {
        return instance;
    }

    /**
     * 
     */
    private FilterFactory() {
        // left empty by default.
    }

    /**
     * Create a new {@link Filter}.
     * 
     * @param expression
     * @return
     */
    public Filter createFilter(final String expression) throws FilterParseException {
        final String expr = expression.toLowerCase();
        if (!expr.startsWith("sip.")) {
            throw new FilterParseException(0, "Not a valid sip expression");
        }

        final int index = expression.indexOf("==");
        if (index == -1) {
            throw new FilterParseException(expression.length(), "Expected a value. Missing '=='");
        }

        final String token = expression.substring(0, index).trim();
        final String value = expression.substring(index + 2, expression.length()).trim();
        if (token.equalsIgnoreCase("sip.call-id")) {
            return new SipCallIdFilter(value);
        }

        // assume everything else is a generic header filter.
        final String[] parts = token.split("\\.");
        if (parts.length == 1) {
            throw new FilterParseException(0, "Expected \"sip.\"<value> but didn't find a dot");
        } else if (parts.length > 2) {
            throw new FilterParseException(0,
                    "Expected \"sip.\"<value> but found multiple dots so now I'm not sure what to do");
        }

        final String headername = parts[1];
        return new SipHeaderFilter(headername, value);

    }
}
