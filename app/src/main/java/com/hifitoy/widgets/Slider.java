/*
 *   Slider.java
 *
 *   Created by Artem Khlyupin on 10/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

@SuppressLint("AppCompatCustomView")
public class Slider extends SeekBar {
    public Slider(Context context) {
        super(context);
    }

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPercent(float percent) {
        if (percent > 1.0f) percent = 1.0f;
        if (percent < 0.0f) percent = 0.0f;

        int percentValue = (int)(percent * getMax());
        setProgress(percentValue);
    }
    public float getPercent() {
        return (float)getProgress() / getMax();
    }
}
