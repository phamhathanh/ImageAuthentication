package com.authpro.imageauthentication;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
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

    private View initialButton = null;
    private PointF originalLocation = null,
                    lastLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textView = (TextView)findViewById(R.id.textView);
        setupButtons();

        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        setupCanvasLineDrawing();
    }

    private void setupButtons()
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

                return false;
            }
        });

        view.setOnDragListener(new View.OnDragListener()
        {
            @Override
            public boolean onDrag(final View v, DragEvent event)
            {

                final Handler handler = new Handler();
                final Runnable dragHeld = new Runnable()
                {
                    public void run()
                    {
                        v.setPressed(true);
                    }
                };

                int action = event.getAction();
                switch (action)
                {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        handler.postDelayed(dragHeld, 1000);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        handler.removeCallbacks(dragHeld);
                        break;
                    case DragEvent.ACTION_DROP:
                        assertNotNull(initialButton);
                        //handler.removeCallbacks(dragHeld);

                        int firstIndex = (int) initialButton.getTag(),
                            secondIndex = (int) v.getTag();
                        addInput(firstIndex, secondIndex);

                        toast.setText(initialButton.getTag() + " - " + v.getTag());
                        toast.show();

                        v.setPressed(false);
                        initialButton.setPressed(false);
                        initialButton = null;

                        v.playSoundEffect(SoundEffectConstants.CLICK);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        if ((initialButton != null) && (v == initialButton))
                        {
                            v.setPressed(false);
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

    private void setupCanvasLineDrawing()
    {
        View grid = findViewById(R.id.rows);
        grid.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                assertTrue(v instanceof LinearLayout);

                int action = event.getAction() & MotionEvent.ACTION_MASK;
                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                        assertNull(originalLocation);
                        originalLocation = new PointF(event.getX(), event.getY());
                        assertNull(lastLocation);
                        lastLocation = new PointF(event.getX(), event.getY());
                    case MotionEvent.ACTION_MOVE:
                        if (initialButton == null)
                            break;
                        final PointF newLocation = new PointF(event.getX(), event.getY());
                        final float squareDistance =  (newLocation.x - lastLocation.x) * (newLocation.x - lastLocation.x)
                                + (newLocation.y - lastLocation.y) * (newLocation.y - lastLocation.y),
                            squareThreshold = 9;
                        if (squareDistance < squareThreshold)
                            break;
                        lastLocation = newLocation;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
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

