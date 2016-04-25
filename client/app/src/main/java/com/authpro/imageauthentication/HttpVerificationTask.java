package com.authpro.imageauthentication;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpVerificationTask extends AsyncTask<URL, Void, HttpResult>
{
    public enum Method
    {
        OPTIONS,
        GET,
        HEAD,
        POST,
        PUT,
        DELETE,
        TRACE
    }

    private final ICallbackable<HttpResult> caller;
    private final Method method;

    public HttpVerificationTask(ICallbackable<HttpResult> caller, Method method)
    {
        this.method = method;
        this.caller = caller;
    }

    @Override
    public HttpResult doInBackground(URL... urls)
    {
        if (urls.length != 1)
            throw new IllegalArgumentException();

        URL url = urls[0];
        return getResult(url);
    }

    private HttpResult getResult(URL url)
    {
        HttpURLConnection connection = null;
        String methodString;

        switch (this.method)
        {
            case OPTIONS:
                methodString = "OPTIONS";
                break;
            case GET:
                methodString = "GET";
                break;
            case HEAD:
                methodString = "HEAD";
                break;
            case POST:
                methodString = "POST";
                break;
            case PUT:
                methodString = "PUT";
                break;
            case DELETE:
                methodString = "DELETE";
                break;
            case TRACE:
                methodString = "TRACE";
                break;
            default:
                throw new RuntimeException();
        }

        try
        {
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod(methodString);
            connection.connect();

            int statusCode = connection.getResponseCode();

            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            String content = readStream(inputStream);
            return new HttpResult(statusCode, content);
        }
        catch (IOException exception)
        {
            return "ERROR";
        }
        finally
        {
            if (connection != null)
                connection.disconnect();
        }
    }

    private String readStream(InputStream input) throws IOException
    {
        StringBuilder output = new StringBuilder();
        while (true)
        {
            byte[] writtenBytes = new byte[4096];

            int bytesReadCount = input.read(writtenBytes);
            Boolean endOfStreamReached = bytesReadCount == -1;
            if (endOfStreamReached)
                return output.toString();

            output.append(new String(writtenBytes, 0, bytesReadCount));
        }
    }

    @Override
    public void onPostExecute(HttpResult result)
    {
        this.caller.callback(result);
    }
}