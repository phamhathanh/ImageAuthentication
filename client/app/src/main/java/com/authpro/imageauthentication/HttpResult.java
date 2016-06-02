package com.authpro.imageauthentication;

import java.util.List;
import java.util.Map;

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

    public List<String> getHeader(String name)
    {
        return headers.get(name);
    }
}
