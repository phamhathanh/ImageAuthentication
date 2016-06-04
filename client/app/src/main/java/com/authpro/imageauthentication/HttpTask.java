package com.authpro.imageauthentication;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpTask extends AsyncTask<Void, Void, HttpResult>
{

    private final ICallbackable<HttpResult> caller;
    private final HttpMethod method;
    private final URL url;
    private final String header, content;

    public HttpTask(ICallbackable<HttpResult> caller, HttpMethod method, String urlString)
    {
        this.method = method;
        this.caller = caller;
        this.url = urlFromString(urlString);
        this.header = null;
        this.content = null;
    }

    public HttpTask(ICallbackable<HttpResult> caller, HttpMethod method, String urlString, String header, String content)
    {
        this.method = method;
        this.caller = caller;
        this.url = urlFromString(urlString);
        this.header = header;
        this.content = content;
    }

    @Override
    public HttpResult doInBackground(Void... params)
    {
        HttpURLConnection connection = null;
        try
        {
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod(method.name());
            if (header != null)
                connection.setRequestProperty("Authorization", header);
            if (content != null)
            {
                connection.setDoInput(true);
                byte[] bytes = content.getBytes("UTF-8");
                OutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.write(bytes);
                outputStream.close();
            }

            connection.connect();

            int statusCode = connection.getResponseCode();
            String content = getContent(connection);

            Map<String, List<String>> headers = connection.getHeaderFields();
            return new HttpResult(statusCode, content, headers);
        }
        catch (IOException exception)
        {
            return new HttpResult(null, exception.getMessage(), null);
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

    private String getContent(HttpURLConnection connection)
    {
        String content;
        try
        {
            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            content = readStream(inputStream);
        }
        catch (IOException exception)
        {
            content = null;
        }
        return content;
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