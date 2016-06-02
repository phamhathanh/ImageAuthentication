package com.authpro.imageauthentication;

import android.app.Fragment;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static junit.framework.Assert.*;

public class InputFragment extends Fragment
{
    private final int alphabetCount = 16;

    private ArrayList<Integer> input = new ArrayList<>();
    private TextView textView;

    private View initialButton = null;

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_input, container, true);

        this.textView = (EditText)view.findViewById(R.id.textView);
        setupButtons(view);

        // Hack.
        this.rootView = view;

        return view;
    }

    public void setupImages(String data)
    {
        final Resources resources = getResources();

        final int columnCountResID = R.integer.gridColumnCount,
                rowCountResID = R.integer.gridRowCount,
                columnCount = resources.getInteger(columnCountResID),
                rowCount = resources.getInteger(rowCountResID);

        final ViewGroup gridLayout = (ViewGroup)rootView.findViewById(R.id.rows);
        final int realRowCount = gridLayout.getChildCount();
        assertEquals(realRowCount, rowCount);

        String[] base64Strings = data.split("\n");
        for (int i = 0; i < rowCount; i++)
        {
            final View child = gridLayout.getChildAt(i);
            assertTrue(child instanceof LinearLayout);
            LinearLayout row = (LinearLayout)child;

            final int realColumnCount = row.getChildCount();
            assertEquals(realColumnCount, columnCount);
            assertEquals(rowCount * columnCount, alphabetCount);

            for (int j = 0; j < columnCount; j++)
            {
                final View cell = ((ViewGroup)row.getChildAt(j)).getChildAt(0);
                assertTrue(cell instanceof ImageButton);
                final ImageButton imageButton = (ImageButton)cell;

                int index = i * columnCount + j;
                Bitmap image = fromBase64(base64Strings[index]);
                imageButton.setImageBitmap(image);
            }
        }
    }

    private Bitmap fromBase64(String base64)
    {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void setupButtons(View view)
    {
        final Resources resources = getResources();

        final int columnCountResID = R.integer.gridColumnCount,
                rowCountResID = R.integer.gridRowCount,
                columnCount = resources.getInteger(columnCountResID),
                rowCount = resources.getInteger(rowCountResID);

        final ViewGroup gridLayout = (ViewGroup)view.findViewById(R.id.rows);
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
                final View cell = ((ViewGroup)row.getChildAt(j)).getChildAt(0);
                assertTrue(cell instanceof ImageButton);
                final ImageButton imageButton = (ImageButton)cell;

                final int index = i * columnCount + j,
                        imageID = images.getResourceId(index, 0);
                if (imageID == 0)
                    throw new IndexOutOfBoundsException("Index is outside of resources array range.");
                imageButton.setTag(index);
                imageButton.setImageResource(imageID);

                setupForDragEvent(cell);
            }
        }
    }

    private void setupForDragEvent(View view)
    {
        view.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                assertTrue(v instanceof ImageButton);

                int action = event.getAction() & MotionEvent.ACTION_MASK;
                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                        assertNull(initialButton);
                        // TODO: Fix the bug when this is not null sometimes.
                        initialButton = v;
                        View.DragShadowBuilder shadow = new View.DragShadowBuilder();
                        v.startDrag(null, shadow, null, 0);
                        v.setPressed(true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }

                return true;
            }
        });

        view.setOnDragListener(new View.OnDragListener()
        {
            @Override
            public boolean onDrag(View v, DragEvent event)
            {

                int action = event.getAction();
                switch (action)
                {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        if (v != initialButton)
                            v.setPressed(true);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        if (v != initialButton)
                            v.setPressed(false);
                        break;
                    case DragEvent.ACTION_DROP:
                        assertNotNull(initialButton);

                        int firstIndex = (int)initialButton.getTag(),
                                secondIndex = (int)v.getTag();
                        addInput(firstIndex, secondIndex);

                        v.setPressed(false);
                        v.playSoundEffect(SoundEffectConstants.CLICK);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        if (v == initialButton)
                        {
                            initialButton.setPressed(false);
                            initialButton = null;
                        }
                        break;
                }

                return true;
            }
        });
    }

    private void addInput(int firstIndex, int secondIndex)
    {
        int index = firstIndex * alphabetCount + secondIndex;
        input.add(index);

        textView.append("*");
        assertEquals(textView.length(), input.size());
    }

    public void clear()
    {
        input = new ArrayList<>();
        textView.setText("");
    }

    public String getInputString()
    // Should use char[] instead, for security reasons.
    {
        StringBuilder output = new StringBuilder();
        for (Integer item: this.input)
            output.append(item).append("_");
        return output.toString();
    }
}
