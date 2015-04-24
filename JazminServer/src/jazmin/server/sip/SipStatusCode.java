/**
 * 
 */
package jazmin.server.sip;

/**
 * @author yama
 *
 */
public interface SipStatusCode {
	/**
     * Status code (202) indicating that the request has been accepted for processing, but the processing has not been completed. The request might or might not eventually be acted upon, as it might be disallowed when processing actually takes place. There is no facility for re-sending a status code from an asynchronous operation such as this.
     * See Also:Constant Field Values
     */
    static final int SC_ACCEPTED=202;

    /**
     * Status code (484) indicating that the server received a request with a To (Section 6.37) address or Request-URI that was incomplete. Additional information SHOULD be provided.
     * Note: This status code allows overlapped dialing. With overlapped dialing, the client does not know the length of the dialing string. It sends strings of increasing lengths, prompting the user for more input, until it no longer receives a 484 status response.
     * See Also:Constant Field Values
     */
    static final int SC_ADDRESS_INCOMPLETE=484;

    /**
     * Status code (380) indicating alternative service.
     * See Also:Constant Field Values
     */
    static final int SC_ALTERNATIVE_SERVICE=380;

    /**
     * Status code (485) indicating that the callee address provided in the request was ambiguous. The response MAY contain a listing of possible unambiguous addresses in Contact headers.
     * Revealing alternatives can infringe on privacy concerns of the user or the organization. It MUST be possible to configure a server to respond with status 404 (Not Found) or to suppress the listing of possible choices if the request with the URL lee@example.com. 485 Ambiguous SIP/2.0 Contact: Carol Lee sip:carol.lee@example.com Contact: Ping Lee sip:p.lee@example.com Contact: Lee M. Foote sip:lee.foote@example.com
     * Some email and voice mail systems provide this functionality. A status code separate from 3xx is used since the semantics are different: for 300, it is assumed that the same person or sevice will be reached by the choices provided. While an automated choice or sequential search makes sense for a 3xx response, user intervention is required for a 485 response.
     * See Also:Constant Field Values
     */
    static final int SC_AMBIGUOUS=485;

    /**
     * Status code (489) indicating that the server did not understand the event package specified in a "Event" header field.
     * Since: 1.1 See Also:Constant Field Values
     */
    static final int SC_BAD_EVENT=489;

    /**
     * Status code (420) indicating that the server did not understand the protocol extension specified in a Require (Section 6.30) header field.
     * See Also:Constant Field Values
     */
    static final int SC_BAD_EXTENSION=420;

    /**
     * Status code (502) indicating that the server, while acting as a gateway or proxy, received an invalid response from the downstream server it accessed in attempting to fulfill the request.
     * See Also:Constant Field Values
     */
    static final int SC_BAD_GATEWAY=502;

    /**
     * Status code (436) indicating that the Identity-Info header contains a URI that cannot be dereferenced by the verifier (either the URI scheme is unsupported by the verifier, or the resource designated by the URI is otherwise unavailable). 
     */
    static final int SC_BAD_IDENTITY_INFO=436;
    
    /**
     * Status code (400) indicating Bad Request.
     * See Also:Constant Field Values
     */
    static final int SC_BAD_REQUEST=400;

    /**
     * Status code (600) indicating that the callee's end system was contacted successfully but the callee is busy and does not wish to take the call at this time.
     * See Also:Constant Field Values
     */
    static final int SC_BUSY_EVERYWHERE=600;

    /**
     * Status code (486) indicating that the callee's end system was contacted successfully but the callee is curently not willing or able to take additional call.
     * See Also:Constant Field Values
     */
    static final int SC_BUSY_HERE=486;

    /**
     * Status code (181) indicating the call is being forwarded.
     * See Also:Constant Field Values
     */
    static final int SC_CALL_BEING_FORWARDED=181;

    /**
     * Status code (481) indicating Call Leg/Transaction does not exist.
     * This status is returned under two conditions: The server received a BYE request that does not match any existing call leg or the server received a CANCEL request that does not match any existing transaction. (A server simply discards an ACK referring to an unknown transaction.)
     * See Also:Constant Field Values
     */
    static final int SC_CALL_LEG_DONE=481;

    /**
     * Status code (182) indicating the call is queued.
     * See Also:Constant Field Values
     */
    static final int SC_CALL_QUEUED=182;

    /**
     * Status code (412) indicating that the precondition given for the request has failed.
     * Since: 1.1 See Also:Constant Field Values
     */
    static final int SC_CONDITIONAL_REQUEST_FAILED=412;

    /**
     * Status code (603) indicating that the callee's machine was successfully contacted but the user explicily does not wish to or cannot participate. The response MAY indicate a better time to call in the Retry-After header.
     * See Also:Constant Field Values
     */
    static final int SC_DECLINE=603;

    /**
     * Status code (604) indicating that the server has authoritative information that the user indicated in the To request field does not exist anywhere. Searching for the user elsewhere will not yield an results.
     * See Also:Constant Field Values
     */
    static final int SC_DOES_NOT_EXIT_ANYWHERE=604;

    /**
     * Status code (421) indicating that the UAS needs a particular extension to process the request, but this extension is not listed in a Supported header field in the request.
     * See Also:Constant Field Values
     */
    static final int SC_EXTENSION_REQUIRED=421;

    /**
     * Status code (403) indicating that the caller is forbidden to make such requests.
     * See Also:Constant Field Values
     */
    static final int SC_FORBIDDEN=403;

    /**
     * Status code (410) indicating that the requested resource is no longer available at the server an no forwarding address is known. This condition is expected to be considered permanent. If the server does not know, or has no facility to determine, whether or not the codition is permanent, the status code 404 (Not Found) SHOULD be used instead.
     * See Also:Constant Field Values
     */
    static final int SC_GONE=410;

    /**
     * Status code (423) indicating that the server is rejecting the request because the expiration time of the resource refreshed by the request is too short.
     * See Also:Constant Field Values
     */
    static final int SC_INTERVAL_TOO_BRIEF=423;

    /**
     * Status code (438) indicating that the verifier receives a message with an Identity signature that does not correspond to the digest-string calculated by the verifier. 
     */
    static final int SC_INVALID_IDENTITY_HEADER=438;
    
    /**
     * Status code (482) indicating that the server received a request with a Via (Section 6.40) path containing itself.
     * See Also:Constant Field Values
     */
    static final int SC_LOOP_DETECTED=482;

    /**
     * Status code (513) indicating that the server was unable to process the request since the message length exceeded its capabilities.
     * See Also:Constant Field Values
     */
    static final int SC_MESSAGE_TOO_LARGE=513;

    /**
     * Status code (405) indicating that the method specified in the Request-Line is not allowed for the address identified byt the Request-URI. The response MUST include an Allow header field containing a list of valid methods for the indicated address.
     * See Also:Constant Field Values
     */
    static final int SC_METHOD_NOT_ALLOWED=405;

    /**
     * Status code (301) indicating that the callee has moved permanantly.
     * See Also:Constant Field Values
     */
    static final int SC_MOVED_PERMANENTLY=301;

    /**
     * Status code (302) indicating that the callee has moved temporarily.
     * See Also:Constant Field Values
     */
    static final int SC_MOVED_TEMPORARILY=302;

    /**
     * Status code (300) indicating Multiple Choices. i.e., user may be reached at multiple locations.
     * See Also:Constant Field Values
     */
    static final int SC_MULTIPLE_CHOICES=300;

    /**
     * Status code (406) indicating the the resource identified by the request is only capable of generating response entities which have content characteristics not acceptable according to the accept headers sent in the request.
     * See Also:Constant Field Values
     */
    static final int SC_NOT_ACCEPTABLE=406;

    /**
     * Status code (606) indicating that the user's agent was contacted successfully but some aspects of the session description such as the requested media, bandwidth, or addressing style were not acceptable.
     * See Also:Constant Field Values
     */
    static final int SC_NOT_ACCEPTABLE_ANYWHERE=606;

    /**
     * Status code (488) indicating that the response has the same meaning as 606 (Not Acceptable), but only applies to the specific resource addressed by the Request-URI and the request may succeed elsewhere.
     * See Also:Constant Field Values
     */
    static final int SC_NOT_ACCEPTABLE_HERE=488;

    /**
     * Status code (404) indicating that the server had definitive information that the user does not exist at the domain specified in the Request-URI. This status is also returned if the domain in the Request-URI does not match any of the domains handled by the recipent of the request.
     * See Also:Constant Field Values
     */
    static final int SC_NOT_FOUND=404;

    /**
     * Status code (501) indicating that the server does not support the functionality required to fulfill the request.
     * See Also:Constant Field Values
     */
    static final int SC_NOT_IMPLEMENTED=501;

    /**
     * Status code (200) indicating the request succeeded normally.
     * See Also:Constant Field Values
     */
    static final int SC_OK=200;

    /**
     * Status code (402) indicating that the caller needs to make a payment.
     * See Also:Constant Field Values
     */
    static final int SC_PAYMENT_REQUIRED=402;

    /**
     * Status code (580) indicating failure to meet certain preconditions.
     * Since: 1.1 See Also:Constant Field Values
     */
    static final int SC_PRECONDITION_FAILURE=580;

    /**
     * Status code (429) indicating that the referee must provide a valid Referred-By token.
     * Since: 1.1 See Also:Constant Field Values
     */
    static final int SC_PROVIDE_REFERER_IDENTITY=429;

    /**
     * Status code (407) indicating that the client MUST first authenticate itself with the proxy. The proxy MUST return a Proxy-Authenticate header field (section 6.26) containing a challenge applicable to the proxy for the requested resource. The client MAY repeat the request with a suitable Proxy-Authorization header field (section 6.27). SIP access authorization is explained in section 13.2 and 14.
     * This status code is used for applications where access to the communication channel (e.g., a telephony gateway) rather than the callee requires authentication.
     * See Also:Constant Field Values
     */
    static final int SC_PROXY_AUTHENTICATION_REQUIRED=407;

    /**
     * Status code (413) indicating that the server si refusing to process a request becaus the request entity is larger than the server is willing or able to process. The server MAY close the connection to prevent the client from continuing the request.
     * If the condition is temporary, teh server SHOULD include a Retry-After header field to indicate that it is temporary and after what time the client MAY try again.
     * See Also:Constant Field Values
     */
    static final int SC_REQUEST_ENTITY_TOO_LARGE=413;

    /**
     * Status code (491) indicating that the request was received by a UAS that had a pending request within the same dialog.
     * See Also:Constant Field Values
     */
    static final int SC_REQUEST_PENDING=491;

    /**
     * Status code (487) indicating that the request was terminated by a BYE or CANCEL request.
     * See Also:Constant Field Values
     */
    static final int SC_REQUEST_TERMINATED=487;

    /**
     * Status code (408) indicating that the server could not produce a response, e.g., a user location, within the time indicated in the Expires request-header field. The client MAY repeat the request without modifications at any later time.
     * See Also:Constant Field Values
     */
    static final int SC_REQUEST_TIMEOUT=408;

    /**
     * Status code (414) indicating that the server if refusing to service the request because the Request-URI is longer than the server is willing to interpret.
     * See Also:Constant Field Values
     */
    static final int SC_REQUEST_URI_TOO_LONG=414;

    /**
     * Status code (180) indicating the server has located the callee, and callee user agent is Ringing the callee.
     * See Also:Constant Field Values
     */
    static final int SC_RINGING=180;

    /**
     * Status code (494) indicating that the client must initiate a security mechanism as defined in RFC 3329.
     * Since: 1.1 See Also:Constant Field Values
     */
    static final int SC_SECURITY_AGREEMENT_REQUIRED=494;

    /**
     * Status code (500) indicating that the server encountered an unexpected condition that prevented it from fulfilling the request.
     * See Also:Constant Field Values
     */
    static final int SC_SERVER_INTERNAL_ERROR=500;

    /**
     * Status code (504) indicating that the server did not receive a timely response from an external server it accessed in attempting to process the request.
     * See Also:Constant Field Values
     */
    static final int SC_SERVER_TIMEOUT=504;

    /**
     * Status code (503) indicating that the server is currently unable to handle the request due to a temporary overloading or maintenance of the server.
     * See Also:Constant Field Values
     */
    static final int SC_SERVICE_UNAVAILABLE=503;

    /**
     * Status code (422) indicating that a request contained a Session-Expires header field with a duration below the minimum timer for the server.
     * Since: 1.1 See Also:Constant Field Values
     */
    static final int SC_SESSION_INTERVAL_TOO_SMALL=422;

    /**
     * Status code (183) carries miscellaneous call progress information. The Reason-Phrase may convey more details about the call progress.
     * See Also:Constant Field Values
     */
    static final int SC_SESSION_PROGRESS=183;

    /**
     * Status code (480) indicating that the callee's end system was contacted successfully but the callee is currently unavailable (e.g., not logged in or logged in such a manner as to preclude communication with the callee). The response MAY indicate a better time to call in the Retry-After header. The user could also be available elsewhere (unbeknownst to this host), thus, this response does not terminate any searches. The reason phrase SHOULD be setable by the user agent. Status 486 (Busy Here) MAY be used to more precisely indicate a particular reason for the call failure.
     * This status is also returned by a redirect server that recognizes the user identified by the Request-URI, but does not currently have a valide forwarding location for that user.
     * See Also:Constant Field Values
     */
    static final int SC_TEMPORARLY_UNAVAILABLE=480;

    /**
     * Status code (483) indicating that the server received a request that contains more Via entries (hops) (Section 6.40) than allowed by the Max-Forwards (Section 6.23) header field.
     * See Also:Constant Field Values
     */
    static final int SC_TOO_MANY_HOPS=483;

    /**
     * Status code (100) indicating the server is trying to locate the callee.
     * See Also:Constant Field Values
     */
    static final int SC_TRYING=100;

    /**
     * Status code (401) indicating that the caller is unauthorized to make this request.
     * See Also:Constant Field Values
     */
    static final int SC_UNAUTHORIZED=401;

    /**
     * Status code (493) indicating that the request was received by a UAS that contained an encrypted MIME body for which the recipient does not possess or will not provide an appropriate decryption key.
     * See Also:Constant Field Values
     */
    static final int SC_UNDECIPHERABLE=493;

    /**
     * Status code (437) indicating that the verifier cannot validate the certificate referenced by the URI of the Identity-Info header, because, for example, the certificate is self-signed, or signed by a root certificate authority for whom the verifier does not possess a root certificate. 
     */
    static final int SC_UNSUPPORTED_CERTIFICATE=437;
    
    /**
     * Status code (415) indicating that the server is refusing to service the request because the message body of the request is in a format not supported by the requested resource for the requested method. The server SHOULD return a list of acceptable formats using the Accept, Accept-Encoding and Accept-Language header fields.
     * See Also:Constant Field Values
     */
    static final int SC_UNSUPPORTED_MEDIA_TYPE=415;

    /**
     * Status code (416) indicating that the server cannot process the request because the scheme of the URI in the Request-URI is unknown to the server.
     * See Also:Constant Field Values
     */
    static final int SC_UNSUPPORTED_URI_SCHEME=416;

    /**
     * Status code (428) indicating that the request should be re-sent with an Identity header. 
     */
    static final int SC_USE_IDENTITY_HEADER=428;
    
    /**
     * Status code (305) indicating that he call can be better handled by the specified proxy server.
     * See Also:Constant Field Values
     */
    static final int SC_USE_PROXY=305;

    /**
     * Status code (505) indicating that the server does not support, the SIP protocol version that was used in the request message.
     * See Also:Constant Field Values
     */
    static final int SC_VERSION_NOT_SUPPORTED=505;
}
