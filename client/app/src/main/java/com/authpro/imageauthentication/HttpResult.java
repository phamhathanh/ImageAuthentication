package com.authpro.imageauthentication;

public class HttpResult
{
    private final HttpStatus status;
    private final String content;

    public HttpResult(HttpStatus status, String content)
    {
        this.status = status;
        this.content = content;
    }

    public HttpResult(int status, String content)
    {
        this.status = HttpStatus.fromCode(status);
        this.content = content;
    }

    public HttpStatus getStatus()
    {
        return status;
    }

    public String getContent()
    {
        return content;
    }
}
