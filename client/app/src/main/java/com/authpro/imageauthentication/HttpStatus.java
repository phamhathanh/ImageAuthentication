package com.authpro.imageauthentication;

import java.util.HashMap;

public enum HttpStatus
{
    OK(200), CREATED(201), ACCEPTED(202), NON_AUTHORITATIVE_INFO(203),
    NO_CONTENT(204), RESET_CONTENT(205), PARTIAL_CONTENT(206),

    MULTIPLE_CHOICES(300), MOVED_PERMANENTLY(301), MOVED_TEMPORARILY(302), SEE_OTHER(303),
    NOT_MODIFIED(304), USE_PROXY(305),

    BAD_REQUEST(400), UNAUTHORIZED(401), PAYMENT_REQUIRED(402), FORBIDDEN(403), NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405), NOT_ACCEPTABLE(406), PROXY_AUTHENTICATION_REQUIRED(407),
    REQUEST_TIMEOUT(408), CONFLICT(409), GONE(410), LENGTH_REQUIRED(411), PRECONDITION_FAILED(412),
    PAYLOAD_TOO_LARGE(413), URI_TOO_LONG(414), UNSUPPORTED_MEDIA_TYPE(415),

    INTERNAL_SERVER_ERROR(500), NOT_IMPLEMENTED(501), BAD_GATEWAY(502), UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504), HTTP_VERSION_NOT_SUPPORTED(505);

    private static final HashMap<Integer, HttpStatus> hashMap = new HashMap<>();

    private final int code;

    private HttpStatus(int code)
    {
        this.code = code;
    }

    public static HttpStatus fromCode(int code)
    {
        HttpStatus status = hashMap.get(code);
        if (status == null)
            throw new IllegalArgumentException("Invalid status code.");

        return status;
    }

    public int getCode()
    {
        return code;
    }

    // Hack: caching
    static
    {
        for (HttpStatus status : HttpStatus.values())
        {
            hashMap.put(status.code, status);
        }
    }
}
