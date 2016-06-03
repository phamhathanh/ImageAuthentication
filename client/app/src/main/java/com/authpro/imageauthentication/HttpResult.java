package com.authpro.imageauthentication;

import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

public class HttpResult
{
    private final HttpStatus status;
    private final String content;
    private final Map<String, List<String>> headers;

    public HttpResult(HttpStatus status, String content, Map<String, List<String>> headers)
    {
        this.status = status;
        this.content = content;
        this.headers = headers;
    }

    public HttpResult(int status, String content, Map<String, List<String>> headers)
    {
        this.status = HttpStatus.fromCode(status);
        this.content = content;
        this.headers = headers;
    }

    public HttpStatus getStatus()
    {
        return status;
    }

    public String getContent()
    {
        return content;
    }

    public String getHeader(String name)
    {
        List<String> hits = headers.get(name);
        if (hits.size() != 1)
            throw new RuntimeException("Wrong header format.");
        return hits.get(0);
    }
}
