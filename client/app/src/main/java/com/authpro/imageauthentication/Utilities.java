package com.authpro.imageauthentication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public final class Utilities
{
    // C# static class mimic.
    private Utilities() {}

    private AlertDialog.Builder buildDialog(String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        return builder;
    }
}
