/*
 *   PresetIconView.java
 *
 *   Created by Artem Khlyupin on 05/01/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.import_fragment;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hifitoy.activities.filters.filter_fragment.FilterView;
import com.hifitoy.hifitoydevice.ToyPreset;

public class PresetIconView extends FrameLayout {

    private final String TAG = "HiFiToy";

    private FilterView filterView;
    private TextView filterName;
    private float scale = 1.0f;

    public PresetIconView(Context context) {
        super(context);
        init(context, null);
    }

    public PresetIconView(Context context, ToyPreset preset) {
        super(context);
        init(context, preset);
    }

    private void init(Context context, ToyPreset preset) {
        if (preset != null) {
            filterView = new FilterView(context, preset.getFilters());
            filterView.controlLineVisible = false;
            filterView.unitVisible = false;
            filterView.allFilterActive = true;

            filterName = new TextView(context);
            filterName.setText(preset.getName());

            addView(filterView);
            addView(filterName);

        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int w = (int)(getWidth() * scale);
        int h = w / 2;

        filterView.layout(centerX - w / 2, centerY - h / 2,
                centerX + w / 2, centerY + h / 2);

        int borderLeft = filterView.getBorderLeft();
        int borderRight = filterView.getBorderRight();
        filterName.layout(centerX - w / 2 + borderLeft,
                            centerY + h / 2,
                            centerX + w / 2 - borderRight,
                            centerY + h / 2 + h / 4);

    }

    public void setScale(float scale) {
        if (scale > 1.0f) scale = 1.0f;
        if (scale < 0.0f) scale = 0.0f;
        this.scale = scale;
    }
}
