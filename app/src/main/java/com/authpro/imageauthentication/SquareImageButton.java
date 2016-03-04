package com.authpro.imageauthentication;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by Rei on 05/03/2016.
 */
public class SquareImageButton extends ImageButton
{
    public SquareImageButton(final Context context)
    {
        super(context);
    }

    public SquareImageButton(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SquareImageButton(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
