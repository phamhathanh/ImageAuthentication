package com.authpro.imageauthentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.provider.Settings;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class RegisterActivity extends Activity implements ICallbackable<HttpResult>
{
    private enum State
    {
        ENTER_PASSWORD,
        CONFIRM_PASSWORD,
        FINISHED
    }

    private State state = State.ENTER_PASSWORD;
    private String password;

    private final int alphabetCount = 30;

    private ArrayList<Integer> input = new ArrayList<>();
    private TextView textView;

    private Toast toast;

    private View initialButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);

        this.textView = (EditText)findViewById(R.id.textView);
        setupButtons();

        this.toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
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
                    throw new IndexOutOfBoundsException("Index is outside of resources array " +
                            "range.");
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

    public void clear(View view)
    {
        clear();
    }

    private void clear()
    {
        input = new ArrayList<>();
        textView.setText("");
    }

    public void enter(View view)
    {
        switch (state)
        {
            case ENTER_PASSWORD:
                password = getInputString();
                state = State.ENTER_PASSWORD;
                break;
            case CONFIRM_PASSWORD:
                String passwordConfirm = getInputString();
                boolean matches = passwordConfirm.equals(password);
                if (!matches)
                {
                    toast.setText("Password mismatched.");
                    toast.show();
                    return;
                }
                else
                    register();
                state = State.FINISHED;
                break;
            case FINISHED:
                return;
            default:
                throw new IllegalStateException();
        }
        clear();
    }

    private void register()
    {
        String deviceIDString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        long deviceID = Long.parseLong(deviceIDString, 16);

        assertNotNull(password);
        String passwordHash = Utils.computeHash(password, deviceID);

        HttpTask.Method method = HttpTask.Method.POST;
        String url = Config.API_URL + deviceID + "/" + passwordHash;

        HttpTask task = new HttpTask(this, method, url);
        task.execute();
    }

    public void callback(HttpResult result)
    {
        String content = result.getContent();

        HttpStatus status = result.getStatus();
        switch (status)
        {
            case CREATED:
                // Registered successfully.
                String message = "Device registered.";
                toast.setText(message);
                toast.show();

                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;
            case FORBIDDEN:
                throw new RuntimeException("Device already registered. Something is wrong.");
            default:
                showErrorDialog(status.getCode());
        }
    }

    private void showErrorDialog(int errorCode)
    {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Connection error");
        errorDialog.setMessage("An error with the connection has occurred. Error code:" + errorCode);
        errorDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();

                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });
        errorDialog.show();
    }

    private String getInputString()
    {
        StringBuilder output = new StringBuilder();
        for (Integer item : this.input)
            output.append(item).append("_");
        return output.toString();
    }
}

