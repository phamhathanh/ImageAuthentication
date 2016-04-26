package com.authpro.imageauthentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity implements ICallbackable<HttpResult>
{
    private static final int REGISTER_REQUEST_CODE = 1;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void logIn(View view)
    {
        String deviceIDString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(deviceIDString, 16);
        String urlString = "http://192.168.1.102:52247/api/devices/" + deviceID;
        try
        {
            URL url = new URL(urlString);
            HttpTask task = new HttpTask(this, HttpTask.Method.GET, url);
            task.execute();
        }
        catch (MalformedURLException exception)
        {
            throw new RuntimeException("Wrong URL.", exception);
        }
    }

    @Override
    public void callback(HttpResult result)
    {
        Intent intent;

        switch (result.getStatus())
        {
            case OK:
                String content = result.getContent();
                if (content.equals("true"))
                {
                    // Device registered, verify password.
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                else if (content.equals("false"))
                {
                    // Device unregistered, registering...
                    intent = new Intent(this, RegisterActivity.class);
                    startActivityForResult(intent, REGISTER_REQUEST_CODE);
                }
                else
                    throw new RuntimeException("API content error.");
                break;
            default:
                showErrorDialog(result.getStatus().getCode());
        }
    }

    private void showErrorDialog(int errorCode)
    {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Connection error");
        errorDialog.setMessage("An error with the connection has occurred. Error code:" + errorCode);
        errorDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                finish();
            }
        });
        errorDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REGISTER_REQUEST_CODE)
        {
            switch (resultCode)
            {
                case RESULT_OK:
                    // TODO: To new Activity.
                    Log.d("TAG", "success");
                    break;
                case RESULT_CANCELED:
                    // Do nothing.
                    break;
                default:
                    throw new RuntimeException();
            }
        }
    }
}
