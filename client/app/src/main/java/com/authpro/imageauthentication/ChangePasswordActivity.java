package com.authpro.imageauthentication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import static junit.framework.Assert.*;

public class ChangePasswordActivity extends PasswordActivity implements ICallbackable<HttpResult>
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

    private TextView instruction;
    private Toast toast;
    private AuthenticationComponent authenticationComponent;

    @Override
    protected int getLayout()
    {
        return R.layout.activity_register;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.instruction = (TextView)findViewById(R.id.instruction);
        this.toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        String deviceIDString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(deviceIDString, 16);
        HttpMethod method = HttpMethod.PUT;
        this.authenticationComponent = new AuthenticationComponent(deviceID, method);
    }

    @Override
    protected void onEnter(String input)
    {
        switch (state)
        {
            case ENTER_OLD_PASSWORD:
                oldPassword = input;
                state = State.ENTER_NEW_PASSWORD;
                instruction.setText(R.string.enter_new_password);
                break;
            case ENTER_NEW_PASSWORD:
                newPassword = input;
                state = State.CONFIRM_NEW_PASSWORD;
                instruction.setText(R.string.confirm_password);
                break;
            case CONFIRM_NEW_PASSWORD:
                String passwordConfirm = input;
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
    }

    private void changePassword()
    {
        String deviceIDString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(deviceIDString, 16);

        String password = oldPassword;
        String header = authenticationComponent.buildAuthorizationHeader(password);

        String content = newPassword;

        HttpMethod method = HttpMethod.PUT;
        String url = Config.API_URL + "api/devices/" + deviceID;
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
        clear(null);
        switch (state)
        {
            case ENTER_OLD_PASSWORD:
                super.onBackPressed();
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

