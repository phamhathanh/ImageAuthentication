package com.authpro.imageauthentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity implements ICallbackable<HttpResult>
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void logIn(View view)
    {
        checkIfDeviceRegistered();
    }

    private void checkIfDeviceRegistered()
    {
        HttpMethod method = HttpMethod.GET;
        String url = Config.API_URL + Utils.deviceURI(this);

        HttpTask task = new HttpTask(this, method, url);
        task.execute();
    }

    @Override
    public void callback(HttpResult result)
    {
        if (result.getStatus() == null)
        {
            showErrorDialog(result.getContent());
            return;
        }

        switch (result.getStatus())
        {
            case UNAUTHORIZED:
                // Device registered, ask for password.
                Intent intent = new Intent(this, LoginActivity.class);
                String header = result.getHeader("WWW-Authenticate");
                intent.putExtra("header", header);
                startActivity(intent);
                break;
            case NOT_FOUND:
                // Device unregistered, registering...
                intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            default:
                showErrorDialog(result.getStatus().getCode());
        }
    }

    private void showErrorDialog(int errorCode)
    {
        showErrorDialog("An error with the connection has occurred. Error code: " + errorCode);
    }

    private void showErrorDialog(String message)
    {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Connection error");
        errorDialog.setMessage(message);
        errorDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        errorDialog.show();
    }
}
