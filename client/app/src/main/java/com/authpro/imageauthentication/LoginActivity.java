package com.authpro.imageauthentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class LoginActivity extends Activity implements ICallbackable<HttpResult>
{
    private Toast toast;
    private InputFragment input;
    private Challenge challenge;
    private int nc;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        this.input = (InputFragment)getFragmentManager().findFragmentById(R.id.input);

        Intent intent = getIntent();
        String header = intent.getStringExtra("header");
        this.challenge = Utils.ParseChallenge(header);
        this.nc = 0;
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
                input.fetchImages();
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
        String deviceIDString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(deviceIDString, 16);

        HttpTask.Method method = HttpTask.Method.GET;
        String relativeURI = Utils.deviceURI(this);
        String url = Config.API_URL + relativeURI;

        nc++;
        String password = input.getInputString();
        String header = Utils.computeHeader(method.name(), relativeURI, challenge, nc, deviceID, password);
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

