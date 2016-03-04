package com.authpro.imageauthentication;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import static junit.framework.Assert.*;

public class LoginActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTagForChildren();
    }

    private void setTagForChildren()
    {
        ViewGroup gridLayout = (ViewGroup)findViewById(R.id.gridLayout);
        int childCount = gridLayout.getChildCount();

        Resources resources = getResources();
        TypedArray images = resources.obtainTypedArray(R.array.images);

        for (int i = 0; i < childCount; i++)
        {
            View child = gridLayout.getChildAt(i);
            child.setTag(i);

            int imageID = images.getResourceId(i, 0);

            assertTrue(child instanceof ImageButton);
            ((ImageButton)child).setImageResource(imageID);
        }
    }

    public void onClick(View view)
    {
        Toast.makeText(getApplicationContext(), "This is the #" + view.getTag() + " button." , Toast.LENGTH_SHORT).show();
    }
}

