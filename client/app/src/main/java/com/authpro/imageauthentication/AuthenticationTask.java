package com.authpro.imageauthentication;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AuthenticationTask extends AsyncTask<URL, Void, String>
{
    @Override
    public String doInBackground(URL... urls)
    {
        if (urls.length != 1)
            throw new IllegalArgumentException();

        URL url = urls[0];
        return getResult(url);
    }

    private String getResult(URL url)
    {
        HttpURLConnection connection = null;
        try
        {
            connection = (HttpURLConnection)url.openConnection();

            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            return readStream(inputStream);
        }
        catch (IOException exception)
        {
            throw new RuntimeException("IOException", exception);
        }
        finally
        {
            if (connection != null)
                connection.disconnect();
        }
    }

    private String readStream(InputStream input) throws IOException
    {
        StringBuffer output = new StringBuffer();
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

    private Context c;
    public AuthenticationTask(Context c)
    {
        this.c = c;
    }

    @Override
    public void onPostExecute(String result)
    {
        if (result != null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setMessage(result);
            builder.show();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setMessage("lol error");
            builder.show();
        }
    }
}