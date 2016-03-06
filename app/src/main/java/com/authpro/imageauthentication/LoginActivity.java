package com.authpro.imageauthentication;

import android.app.Activity;
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
        final Resources resources = getResources();

        final int columnCountResID = R.integer.gridColumnCount,
            rowCountResID = R.integer.gridRowCount,
            columnCount = resources.getInteger(columnCountResID),
            rowCount = resources.getInteger(rowCountResID);

        final ViewGroup gridLayout = (ViewGroup)findViewById(R.id.gridLayout);
        final int childCount = gridLayout.getChildCount();
        assertTrue("childCount = " + childCount + ", columnCount = " + columnCount + ", rowCount = " + rowCount, childCount == columnCount * rowCount);

        final int screenPixelWidth = getPixelScreenWidth();
            // assuming vertical orientation

        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;

        final int padding = (int)resources.getDimension(R.dimen.imagebutton_margin),
            childWidth = screenPixelWidth / columnCount;// - (int)Math.ceil(2 * padding * density);
            // beware of calculation error

        TypedArray images = null;
        try
        {
            images = resources.obtainTypedArray(R.array.images);

            for (int i = 0; i < childCount; i++) {
                final View child = gridLayout.getChildAt(i);
                child.setTag(i);

                final int imageID = images.getResourceId(i, 0);

                assertTrue(child instanceof ImageButton);
                final ImageButton imageButton = (ImageButton)child;
                imageButton.setImageResource(imageID);

                imageButton.setMaxWidth(childWidth);
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

    private int getPixelScreenWidth()
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

    public void clear(View view)
    {
        Toast.makeText(getApplicationContext(), "Cleared." , Toast.LENGTH_SHORT).show();
    }

    public void enter(View view)
    {
        Toast.makeText(getApplicationContext(), "Entered." , Toast.LENGTH_SHORT).show();
    }
}

