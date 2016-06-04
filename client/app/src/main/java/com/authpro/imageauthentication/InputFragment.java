package com.authpro.imageauthentication;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Pair;
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
import java.util.Collections;

import static junit.framework.Assert.*;

public class InputFragment extends Fragment implements ICallbackable<HttpResult>
{
    private final int imageCount = 30;
    private int rowCount, columnCount;

    private ArrayList<Pair<Integer, Integer>> input = new ArrayList<>();
    private TextView textView;

    private View initialButton = null;

    private ImageButton[][] imageButtons;
    private String[] imageHashes;
    private Bitmap[] images;
    private ArrayList<Integer> permutation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_input, container, true);

        this.textView = (EditText)view.findViewById(R.id.textView);

        getDimensions();
        setupButtons(view);
        fetchImages();

        return view;
    }

    private void getDimensions()
    {
        Resources resources = getResources();

        int columnCountResID = R.integer.gridColumnCount,
            rowCountResID = R.integer.gridRowCount;
        this.columnCount = resources.getInteger(columnCountResID);
        this.rowCount = resources.getInteger(rowCountResID);
        assertEquals(rowCount * columnCount, imageCount);
    }

    private void setupButtons(View view)
    {
        final ViewGroup gridLayout = (ViewGroup)view.findViewById(R.id.rows);
        final int realRowCount = gridLayout.getChildCount();
        assertEquals(realRowCount, rowCount);

        this.imageButtons = new ImageButton[rowCount][columnCount];
        for (int i = 0; i < rowCount; i++)
        {
            final View child = gridLayout.getChildAt(i);
            assertTrue(child instanceof LinearLayout);
            LinearLayout row = (LinearLayout) child;

            final int realColumnCount = row.getChildCount();
            assertEquals(realColumnCount, columnCount);

            for (int j = 0; j < columnCount; j++)
            {
                final View cell = ((ViewGroup)row.getChildAt(j)).getChildAt(0);
                assertTrue(cell instanceof ImageButton);
                final ImageButton imageButton = (ImageButton)cell;

                final int index = i * columnCount + j;
                imageButton.setTag(index);

                imageButtons[i][j] = imageButton;
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
        input.add(new Pair<>(permutation.get(firstIndex), permutation.get(secondIndex)));

        textView.append("*");
        assertEquals(textView.length(), input.size());
    }

    private void fetchImages()
    {
        HttpMethod method = HttpMethod.GET;
        String url = Config.API_URL + "api/images";

        HttpTask task = new HttpTask(this, method, url);
        task.execute();
    }

    public void callback(HttpResult result)
    {
        HttpStatus status = result.getStatus();
        switch (status)
        {
            case OK:
                String data = result.getContent();
                setImages(data);
                break;
            default:
                // Silently fail.
        }
    }

    private void setImages(String data)
    {
        images = new Bitmap[imageCount];
        imageHashes = new String[imageCount];
        permutation = new ArrayList<>(imageCount);
        String[] base64Strings = data.split("\n");
        for (int i = 0; i < rowCount; i++)
            for (int j = 0; j < columnCount; j++)
            {
                int index = i * columnCount + j;
                permutation.add(index);
                String base64 = base64Strings[index];
                images[index] = fromBase64(base64);
                imageHashes[index] = Utils.computeHash(base64);
            }

        shuffle();
    }

    public void shuffle()
    {
        Collections.shuffle(permutation);
        for (int i = 0; i < rowCount; i++)
            for (int j = 0; j < columnCount; j++)
            {
                ImageButton imageButton = imageButtons[i][j];
                int index = i * columnCount + j;
                Bitmap image = images[permutation.get(index)];
                imageButton.setImageBitmap(image);
            }
    }

    private Bitmap fromBase64(String base64)
    {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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
        for (Pair<Integer, Integer> pair : this.input)
        {
            if (pair.first.equals(pair.second))
                output.append(imageHashes[pair.first]).append("_");
            else
                output.append(imageHashes[pair.first]).append("+").append(imageHashes[pair.second]).append("_");
        }
        return output.toString();
    }
}
