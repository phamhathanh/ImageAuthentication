package com.authpro.imageauthentication;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginActivity extends Activity
{
    private ArrayList<Integer> input = new ArrayList<>();
    private TextView textView;

    private final ArrayList<Integer> correctInput = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textView = (TextView)findViewById(R.id.textView);
        setupImages();
    }

    private void setupImages()
    {
        final Resources resources = getResources();

        final int columnCountResID = R.integer.gridColumnCount,
            rowCountResID = R.integer.gridRowCount,
            columnCount = resources.getInteger(columnCountResID),
            rowCount = resources.getInteger(rowCountResID);

        final ViewGroup gridLayout = (ViewGroup) findViewById(R.id.grid);
        final int realRowCount = gridLayout.getChildCount();
        assertEquals(realRowCount, rowCount);

        TypedArray images = null;
        try
        {
            images = resources.obtainTypedArray(R.array.images);

            for (int i = 0; i < rowCount; i++)
            {
                final View child = gridLayout.getChildAt(i);
                assertTrue(child instanceof LinearLayout);
                LinearLayout row = (LinearLayout) child;

                final int realColumnCount = row.getChildCount();
                assertEquals(realColumnCount, columnCount);

                for (int j = 0; j < columnCount; j++)
                {
                    final View cell = row.getChildAt(j);
                    assertTrue(cell instanceof ImageButton);
                    final ImageButton imageButton = (ImageButton) cell;

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
        Object tag = view.getTag();
        assertTrue(tag instanceof Integer);
        int index = (int)tag;
        input.add(index);

        int charCount = textView.length() + 1;
        assertEquals(charCount, input.size());
        char[] text = new char[charCount];
        Arrays.fill(text, '*');
        textView.setText(new String(text));
    }

    public void clear(View view)
    {
        input = new ArrayList<>();
        textView.setText("");

        Toast.makeText(getApplicationContext(), "Cleared.", Toast.LENGTH_SHORT).show();
        // Somehow this line is necessary for the TextView to update
    }

    public void enter(View view)
    {
        String result;
        if (matches())
            result = "Success!";
        else
            result = "Password mismatched.";

        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
    }

    private boolean matches()
    {
        int size1 = input.size(),
            size2 = correctInput.size();
        if (size1 != size2)
            return false;

        for (int i = 0; i < size1; i++)
            if (input.get(i) != correctInput.get(i))
                return false;

        return true;
    }
}

