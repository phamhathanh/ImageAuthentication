package com.authpro.imageauthentication;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import static junit.framework.Assert.assertTrue;

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
        Resources resources = getResources();

        int columnCountResID = R.integer.gridColumnCount,
            rowCountResID = R.integer.gridRowCount,
            columnCount = resources.getInteger(columnCountResID),
            rowCount = resources.getInteger(rowCountResID);

        ViewGroup gridLayout = (ViewGroup)findViewById(R.id.gridLayout);
        int childCount = gridLayout.getChildCount();
        assertTrue("childCount = " + childCount
                        + ", columnCount = " + columnCount
                        + ", rowCount = " + rowCount,
                childCount == columnCount * rowCount);

        int screenWidth = getScreenWidth(),
        // assuming vertical orientation
            childWidth = screenWidth / columnCount;
        // beware of calculation error

        TypedArray images = null;
        try
        {
            images = resources.obtainTypedArray(R.array.images);

            for (int i = 0; i < childCount; i++) {
                View child = gridLayout.getChildAt(i);
                child.setTag(i);

                int imageID = images.getResourceId(i, 0);

                assertTrue(child instanceof ImageButton);
                ImageButton imageButton = (ImageButton)child;
                imageButton.setImageResource(imageID);
                imageButton.setMaxWidth(childWidth);
                imageButton.setMinimumHeight(childWidth);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            images.recycle();
        }
    }

    private int getScreenWidth()
    {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        return width;
    }

    public void onClick(View view)
    {
        Toast.makeText(getApplicationContext(), "This is the #" + view.getTag() + " button." , Toast.LENGTH_SHORT).show();
    }
}

