/*
 *   PresetIconView.java
 *
 *   Created by Artem Khlyupin on 05/01/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.import_fragment;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class PresetIconView extends FrameLayout {

    private final String TAG = "HiFiToy";

    private View v;
    private float scale = 1.0f;

    public PresetIconView(Context context) {
        super(context);
        init(context, 0xFF000000);
    }

    public PresetIconView(Context context, int color) {
        super(context);
        init(context, color);
    }

    private void init(Context context, int color) {
        v = new View(context);
        v.setBackgroundColor(color);
        addView(v);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int w = (int)(getWidth() * scale);
        int h = w / 2;

        v.layout(centerX - w / 2, centerY - h / 2,
                centerX + w / 2, centerY + h / 2);

    }

    public void setScale(float scale) {
        if (scale > 1.0f) scale = 1.0f;
        if (scale < 0.0f) scale = 0.0f;
        this.scale = scale;
    }
}
