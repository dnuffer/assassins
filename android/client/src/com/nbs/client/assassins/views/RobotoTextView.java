package com.nbs.client.assassins.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class RobotoTextView extends TextView {

    public RobotoTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setTypeface(Typeface
                .createFromAsset(context.getAssets(), "fonts/roboto/Roboto-Condensed.ttf"));
	}

    public RobotoTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(Typeface
                .createFromAsset(context.getAssets(), "fonts/roboto/Roboto-Condensed.ttf"));
	}
    
	public RobotoTextView(Context context) {
        super(context);
        setTypeface(Typeface
                .createFromAsset(context.getAssets(), "fonts/roboto/Roboto-Condensed.ttf"));

    }
}