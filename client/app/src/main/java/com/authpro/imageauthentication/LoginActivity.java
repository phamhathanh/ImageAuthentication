package com.authpro.imageauthentication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class LoginActivity extends PasswordActivity implements ICallbackable<HttpResult>
{
    private Toast toast;
    private AuthenticationComponent authenticationComponent;

    @Override
    protected int getLayout()
    {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        String deviceIDString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(deviceIDString, 16);
        HttpMethod method = HttpMethod.GET;
        this.authenticationComponent = new AuthenticationComponent(deviceID, method);
    }

    @Override
    protected void onEnter(String input)
    {
        String deviceIDString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(deviceIDString, 16);

        HttpMethod method = HttpMethod.GET;
        String relativeURI = "api/devices/" + deviceID;
        String url = Config.API_URL + relativeURI;

        String password = input;

        String header = authenticationComponent.buildAuthorizationHeader(password);
        String content = null;

        HttpTask task = new HttpTask(this, method, url, header, content);
        task.execute();
    }

    public void callback(HttpResult result)
    {
        if (result.getStatus() == null)
        {
            showErrorDialog(result.getContent());
            return;
        }

        HttpStatus status = result.getStatus();
        switch (status)
        {
            case OK:
                toast.setText("Success!");
                toast.show();
                Intent intent = new Intent(this, LoggedInActivity.class);
                startActivity(intent);
                break;
            case UNAUTHORIZED:
                toast.setText("Wrong password.");
                toast.show();
                finish();
                break;
            case NOT_FOUND:
                throw new RuntimeException("Device not registered. Something is wrong.");
            default:
                showErrorDialog(status.getCode());
        }
    }

    private void showErrorDialog(int errorCode)
    {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Connection error");
        errorDialog.setMessage("An error with the connection has occurred. Error code: " + errorCode);
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
                finish();
            }
        });
        errorDialog.show();
    }
}

