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

public abstract class PasswordActivity extends Activity implements ICallbackable<HttpResult>
{
    protected abstract int getLayout();

    private InputFragment input;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());

        this.input = (InputFragment)getFragmentManager().findFragmentById(R.id.input);
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

    public final void clear(View view)
    {
        input.clear();
    }

    public final void enter(View view)
    {
        String inputString = input.getInputString();
        onEnter(inputString);
        input.clear();
    }

    protected abstract void onEnter(String input);
}

