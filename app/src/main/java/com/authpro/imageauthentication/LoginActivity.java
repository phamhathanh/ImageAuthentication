package com.authpro.imageauthentication;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

        final ViewGroup gridLayout = (ViewGroup)findViewById(R.id.grid);
        final int realRowCount = gridLayout.getChildCount();
        assertTrue("realRowCount = " + realRowCount + ", rowCount = " + rowCount, realRowCount == rowCount);

        TypedArray images = null;
        try
        {
            images = resources.obtainTypedArray(R.array.images);

            for (int i = 0; i < rowCount; i++)
            {
                final View child = gridLayout.getChildAt(i);

                assertTrue(child instanceof LinearLayout);
                LinearLayout row = (LinearLayout)child;
                final int realColumnCount = row.getChildCount();
                assertTrue("realColumnCount = " + realColumnCount + ", columnCount = " + columnCount, realColumnCount == columnCount);

                for (int j = 0; j < columnCount; j++)
                {
                    final View cell = row.getChildAt(j);

                    assertTrue(cell instanceof ImageButton);
                    final ImageButton imageButton = (ImageButton)cell;

                    final int index = i * columnCount + j,
                            imageID = images.getResourceId(index, 0);
                    if (imageID == 0)
                        throw new IndexOutOfBoundsException("Index is outside of resources array range.");
                    imageButton.setTag(index);
                    imageButton.setImageResource(imageID);
                }
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

