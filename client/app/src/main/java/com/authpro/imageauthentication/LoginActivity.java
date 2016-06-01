package com.authpro.imageauthentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.provider.Settings;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static junit.framework.Assert.*;

public class LoginActivity extends Activity implements ICallbackable<HttpResult>
{
    private Toast toast;
    private InputFragment input;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);

        this.input = (InputFragment)getFragmentManager().findFragmentById(R.id.input);
        this.toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
    }

    public void clear(View view)
    {
        input.clear();

        toast.setText("Cleared.");
        toast.show();
        // Somehow this line is necessary for the TextView to update
    }

    public void enter(View view)
    {
        String deviceIDString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(deviceIDString, 16);

        String password = input.getInputString(),
            passwordHash = Utils.computeHash(password, deviceID);

        HttpTask.Method method = HttpTask.Method.GET;
        String url = Config.API_URL + deviceID + "/" + passwordHash;

        HttpTask task = new HttpTask(this, method, url);
        task.execute();
    }

    public void callback(HttpResult result)
    {
        HttpStatus status = result.getStatus();
        switch (status)
        {
            case OK:
                String content = result.getContent();
                if (content.equals("true"))
                {
                    toast.setText("Success!");
                    toast.show();
                    Intent intent = new Intent(this, LoggedInActivity.class);
                    startActivity(intent);
                }
                else if (content.equals("false"))
                {
                    toast.setText("Password mismatched.");
                    toast.show();
                    finish();
                }
                else
                    throw new RuntimeException("API content error.");
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
}

