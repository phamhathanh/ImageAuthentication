package com.authpro.imageauthentication;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpTask extends AsyncTask<Void, Void, HttpResult>
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
    private final URL url;

    public HttpTask(ICallbackable<HttpResult> caller, Method method, String urlString)
    {
        this.method = method;
        this.caller = caller;
        this.url = urlFromString(urlString);
    }

    @Override
    public HttpResult doInBackground(Void... params)
    {
        HttpURLConnection connection = null;
        String methodString = this.method.name();

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
            return new HttpResult(null, "ERROR");
        }
        finally
        {
            if (connection != null)
                connection.disconnect();
        }
    }

    private URL urlFromString(String urlString)
    {
        try
        {
            return new URL(urlString);
        }
        catch (MalformedURLException exception)
        {
            throw new RuntimeException("Wrong URL.", exception);
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