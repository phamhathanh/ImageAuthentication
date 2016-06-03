package com.authpro.imageauthentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static junit.framework.Assert.*;

public class ChangePasswordActivity extends Activity implements ICallbackable<HttpResult>
{
    private enum State
    {
        ENTER_OLD_PASSWORD,
        ENTER_NEW_PASSWORD,
        CONFIRM_NEW_PASSWORD,
        FINISHED
    }

    private State state = State.ENTER_OLD_PASSWORD;

    private String oldPassword, newPassword;

    private InputFragment input;
    private TextView instruction;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);

        this.input = (InputFragment)getFragmentManager().findFragmentById(R.id.input);
        this.instruction = (TextView)findViewById(R.id.instruction);
        this.toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
    }

    public void clear(View view)
    {
        input.clear();
    }

    public void enter(View view)
    {
        switch (state)
        {
            case ENTER_OLD_PASSWORD:
                oldPassword = input.getInputString();
                state = State.ENTER_NEW_PASSWORD;
                instruction.setText(R.string.enter_new_password);
                break;
            case ENTER_NEW_PASSWORD:
                newPassword = input.getInputString();
                state = State.CONFIRM_NEW_PASSWORD;
                instruction.setText(R.string.confirm_password);
                break;
            case CONFIRM_NEW_PASSWORD:
                String passwordConfirm = input.getInputString();
                boolean matches = passwordConfirm.equals(newPassword);
                if (!matches)
                {
                    toast.setText("Password mismatched.");
                    toast.show();
                    return;
                }
                else
                    changePassword();
                state = State.FINISHED;
                break;
            case FINISHED:
                return;
            default:
                throw new IllegalStateException();
        }
        input.clear();
    }

    private void changePassword()
    {
        String deviceIDString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(deviceIDString, 16);

        String oldPasswordHash = Utils.computeHash(oldPassword, deviceID),
                newPasswordHash = Utils.computeHash(newPassword, deviceID);

        HttpTask.Method method = HttpTask.Method.PUT;
        String url = Config.API_URL + Utils.deviceURI(this);

        HttpTask task = new HttpTask(this, method, url);
        task.execute();
    }

    public void callback(HttpResult result)
    {
        String content = result.getContent();

        HttpStatus status = result.getStatus();
        switch (status)
        {
            case OK:
                // Change password successfully.
                String message = "Password changed.";
                toast.setText(message);
                toast.show();
                finish();
                break;
            case UNAUTHORIZED:
                // Wrong password.
                showErrorDialog("Wrong password.");
                break;
            default:
                showErrorDialog("An error with the connection has occurred. Error code:" + status.getCode());
        }
    }

    private void showErrorDialog(String message)
    {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Error");
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

    @Override
    public void onBackPressed()
    {
        input.clear();
        switch (state)
        {
            case ENTER_OLD_PASSWORD:
                finish();
                break;
            case ENTER_NEW_PASSWORD:
                assertNull(newPassword);
                oldPassword = null;
                state = State.ENTER_OLD_PASSWORD;
                instruction.setText(R.string.enter_old_password);
                break;
            case CONFIRM_NEW_PASSWORD:
                assertNotNull(oldPassword);
                newPassword = null;
                state = State.ENTER_NEW_PASSWORD;
                instruction.setText(R.string.enter_new_password);
                break;
            case FINISHED:
                return;
            default:
                throw new IllegalStateException();
        }
    }
}

