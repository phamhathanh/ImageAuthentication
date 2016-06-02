package com.authpro.imageauthentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static junit.framework.Assert.assertNotNull;

public class RegisterActivity extends Activity implements ICallbackable<HttpResult>
{
    private enum State
    {
        ENTER_PASSWORD,
        CONFIRM_PASSWORD,
        FINISHED
    }

    private State state = State.ENTER_PASSWORD;
    private String password;

    private InputFragment input;
    private TextView instruction;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.input = (InputFragment)getFragmentManager().findFragmentById(R.id.input);
        this.instruction = (TextView)findViewById(R.id.instruction);
        this.toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.refresh:
                // TODO: Refresh.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void clear(View view)
    {
        input.clear();
    }

    public void enter(View view)
    {
        switch (state)
        {
            case ENTER_PASSWORD:
                password = input.getInputString();
                state = State.CONFIRM_PASSWORD;
                instruction.setText(R.string.confirm_password);
                break;
            case CONFIRM_PASSWORD:
                String passwordConfirm = input.getInputString();
                boolean matches = passwordConfirm.equals(password);
                if (!matches)
                {
                    toast.setText("Password mismatched.");
                    toast.show();
                    return;
                }
                else
                    register();
                state = State.FINISHED;
                // TODO: Loading screen and disable input.
                break;
            case FINISHED:
                return;
            default:
                throw new IllegalStateException();
        }
        input.clear();
    }

    @Override
    public void onBackPressed()
    {
        switch (state)
        {
            case ENTER_PASSWORD:
                super.onBackPressed();
                return;
            case CONFIRM_PASSWORD:
                input.clear();
                state = State.ENTER_PASSWORD;
                instruction.setText(R.string.enter_new_password);
                break;
            case FINISHED:
                return;
            default:
                throw new IllegalStateException();
        }
    }

    private void register()
    {
        String deviceIDString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(deviceIDString, 16);

        assertNotNull(password);
        String passwordHash = Utils.computeHash(password, deviceID);

        HttpTask.Method method = HttpTask.Method.POST;
        String url = Config.API_URL + deviceID + "/" + passwordHash;

        HttpTask task = new HttpTask(this, method, url);
        task.execute();
    }

    public void callback(HttpResult result)
    {
        HttpStatus status = result.getStatus();
        switch (status)
        {
            case CREATED:
                // Registered successfully.
                String message = "Device registered.";
                toast.setText(message);
                toast.show();
                finish();
                break;
            case FORBIDDEN:
                throw new RuntimeException("Device already registered. Something is wrong.");
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

