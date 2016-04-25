package com.authpro.imageauthentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity implements ICallbackable<HttpResult>
{
    private static final int INITIALIZE_PASSWORD_REQUEST_CODE = 1;

    private boolean deviceRegistered;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void logIn()
    {
        String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID),
                urlString = "http://192.168.1.102:52247/api/devices/" + deviceID;
        try
        {
            URL url = new URL(urlString);
            HttpVerificationTask task = new HttpVerificationTask(this);
            task.execute(url);
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
        switch (result)
        {
            case ERROR:
                this.deviceRegistered = false;
                showErrorDialog();
                return;
            case TRUE:
                // Device registered, verify password.
                this.deviceRegistered = true;
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            case FALSE:
                // Device unregistered, registering...
                this.deviceRegistered = false;
                intent = new Intent(this, SetPasswordActivity.class);
                startActivityForResult(intent, INITIALIZE_PASSWORD_REQUEST_CODE);
                break;
            default:
                throw new RuntimeException("Something is wrong with the source code.");
        }
    }

    private void showErrorDialog()
    {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Connection error");
        errorDialog.setMessage("An error with the connection has occurred.");
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
        if (requestCode == INITIALIZE_PASSWORD_REQUEST_CODE)
        {
            switch (resultCode)
            {
                case RESULT_OK:
                    // TODO: PUT (POST?) to server.
                    Log.d("TAG", "success");
                    this.deviceRegistered = true;
                    break;
                case RESULT_CANCELED:
                    // May be just ignore the canceled ones.
                    break;
                default:
                    throw new RuntimeException();
            }
        }
    }

    public void setPassword()
    {

    }
}
