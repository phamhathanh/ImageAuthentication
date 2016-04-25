package com.authpro.imageauthentication;

public class HttpResult
{
    private final HttpStatus statusCode;
    private final String content;

    public HttpResult(HttpStatus statusCode, String content)
    {
        this.statusCode = statusCode;
        this.content = content;
    }

    public HttpResult(int statusCode, String content)
    {
        this.statusCode = statusCode;
        this.content = content;
    }

    public Response getResponse()
    {
        return response;
    }

    public String getContent()
    {
        return content;
    }
}
