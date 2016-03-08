package com.authpro.imageauthentication;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.MotionEvent;
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
    private final int alphabetCount = 30;

    private ArrayList<Integer> input = new ArrayList<>();
    private TextView textView;

    private Toast toast;

    private final ArrayList<Integer> correctInput = new ArrayList<>(Arrays.asList(0, 31, 62, 93, 124));

    private Integer heldButtonIndex = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textView = (TextView)findViewById(R.id.textView);
        setupImages();
        setOnTouchEventListener();

        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
    }

    private void setupImages()
    {
        final Resources resources = getResources();

        final int columnCountResID = R.integer.gridColumnCount,
            rowCountResID = R.integer.gridRowCount,
            columnCount = resources.getInteger(columnCountResID),
            rowCount = resources.getInteger(rowCountResID);

        final ViewGroup gridLayout = (ViewGroup) findViewById(R.id.rows);
        final int realRowCount = gridLayout.getChildCount();
        assertEquals(realRowCount, rowCount);

        TypedArray images = resources.obtainTypedArray(R.array.images);

        for (int i = 0; i < rowCount; i++)
        {
            final View child = gridLayout.getChildAt(i);
            assertTrue(child instanceof LinearLayout);
            LinearLayout row = (LinearLayout) child;

            final int realColumnCount = row.getChildCount();
            assertEquals(realColumnCount, columnCount);
            assertEquals(rowCount * columnCount, alphabetCount);

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

    private void setOnTouchEventListener()
    {
        Resources resources = getResources();
        LinearLayout rows = (LinearLayout)findViewById(R.id.rows);

        rows.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                assertTrue(v instanceof ImageButton);

                int actionCode = event.getAction() & MotionEvent.ACTION_MASK;
                switch (actionCode)
                {
                    case MotionEvent.ACTION_DOWN:
                        swipeStart(v);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        swipeEnd(v);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                    case MotionEvent.ACTION_OUTSIDE:
                        // No need to release.
                    default:
                        // Don't care.
                        break;
                }
                return true;
            }
        });
    }

    private void swipeStart(View view)
    {
        assertNull(heldButtonIndex);

        int index = (int)view.getTag();
        heldButtonIndex = index;
        view.setPressed(true);
    }

    private void swipeEnd(View view)
    {
        assertNotNull(heldButtonIndex);

        int releaseButtonIndex = (int)view.getTag();
        addInput(heldButtonIndex, releaseButtonIndex);
        heldButtonIndex = null;
        view.setPressed(false);
    }

    private void addInput(int firstIndex, int secondIndex)
    {
        int index = firstIndex * alphabetCount + secondIndex;
        input.add(index);
    }

    public void onClick(View view)
    {
        Object tag = view.getTag();
        assertTrue(tag instanceof Integer);
        int index = (int)tag;
        addInput(index, index);

        textView.append("*");
        assertEquals(textView.length(), input.size());

        toast.cancel();
    }

    public void clear(View view)
    {
        input = new ArrayList<>();
        textView.setText("");

        toast.setText("Cleared.");
        toast.show();
        // Somehow this line is necessary for the TextView to update
    }

    public void enter(View view)
    {
        String result;
        if (matches())
            result = "Success!";
        else
            result = "Password mismatched.";

        toast.setText(result);
        toast.show();
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

