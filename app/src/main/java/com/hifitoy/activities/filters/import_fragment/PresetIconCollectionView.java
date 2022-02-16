/*
 *   PresetIconCollectionView.java
 *
 *   Created by Artem Khlyupin on 05/01/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.import_fragment;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.widget.FrameLayout;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.ToyPreset;
import com.hifitoy.hifitoydevice.HiFiToyPresetManager;
import com.hifitoy.hifitoyobjects.Filters;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class PresetIconCollectionView extends FrameLayout {
    private final String TAG = "HiFiToy";

    private FilterCollection filterCollection;
    private PresetIconView[] presetViews;

    public PresetIconCollectionView(Context context, FilterCollection filterCollection) {
        super(context);
        this.filterCollection = filterCollection;

        presetViews = new PresetIconView[filterCollection.size()];
        for (int i = 0; i < filterCollection.size(); i++) {
            String name = filterCollection.getNameList().get(i);
            Filters filter = filterCollection.getFilterList().get(i);
            presetViews[i] = new PresetIconView(context, name, filter);

            addView(presetViews[i]);
        }

        setBackgroundColor(0xF0000000);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = getWidth();
        int height = getHeight();

        int wL = width / 3;

        for (int i = 0; i < presetViews.length; i++) {
            //set layout
            presetViews[i].setScale((float)getViewWidth(i) / wL);

            presetViews[i].layout(
                    getViewCenter(i).x - wL / 2,
                    getViewCenter(i).y - height / 2,
                    getViewCenter(i).x + wL / 2,
                    getViewCenter(i).y + height / 2);


            //set alpha
            float alpha = getIconAlpha(getWidth(), getViewCenter(i).x);
            presetViews[i].setAlpha(alpha);
        }

    }

    public int getBiggerViewIndex() {
        int index = 0;

        for (int i = 0; i < filterCollection.size(); i++) {
            if (getViewWidth(index) < getViewWidth(i)) {
                index = i;
            }
        }
        return index;
    }

    public Point getViewCenter(int index) {
        return new Point(
                getWidth() / 2 +
                        (index - filterCollection.getActiveIndex()) * getWidth() / 3 +
                        filterCollection.getTranslateX(),
                getHeight() / 2);
    }

    private int getViewWidth(int index) {
        return getIconWidth(getWidth(), getViewCenter(index).x);
    }

    private int getIconWidth(int width, int x) {
        // x = [0, width]
        // when x0 = width / 6 -> wIcon0 = width / 3 - 2 * padX
        // when x1 = width / 2 -> wIcon1 = width / 3
        // when x2 = 5 * width / 6 -> wIcon2 = wIcon0
        int padX = width / 20;
        int wIcon0 = width / 3 - 2 * padX;
        int wIcon1 = width / 3;
        int x0 = width / 6;
        int x1 = width / 2;
        int x2 = 5 * width / 6;

        // k * x0 + b = wIcon0
        // k * x1 + b = wIcon1
        // b = wIcon0 - k * x0
        // k * x1 = wIcon1 - wIcon0 + k * x0
        // k = (wIcon1 - wIcon0) / (x1 - x0)
        float k0 = (float)(wIcon1 - wIcon0) / (x1 - x0);
        float b0 = wIcon0 - k0 * x0;

        // k * x1 + b = wIcon1
        // k * x2 + b = wIcon2 = wIcon0
        // b = wIcon1 - k * x1
        // k * x2 = wIcon0 - wIcon1 + k * x1
        // k = (wIcon0 - wIcon1) / (x2 - x1)
        float k1 = (float)(wIcon0 - wIcon1) / (x2 - x1);
        float b1 = wIcon1 - k1 * x1;

        return (x > x1) ? (int)(k1 * x + b1) : (int)(k0 * x + b0);

    }

    private float getIconAlpha(int width, int x) {
        // x = [0, width]
        // when x0 = 0 -> alpha0 = 0
        // when x1 = width / 2 -> alpha = 1
        // when x2 = width -> alpha2 = alpha0
        float k0 = 1.0f / (width / 3 - 0);
        float b0 = 0.0f;

        //k1 * x1 + b1 = 1
        //k1 * x2 + b1 = 0
        //b1 = 1 - k1 * x1
        //k1 * x2 + 1 - k1 * x1 = 0
        //k1 = -1 / (x2 - x1)
        float k1 = -1.0f / (width - 2 * width / 3);
        float b1 = 1.0f - k1 * 2 * width / 3;

        if (x < width / 3) return k0 * x + b0;
        if (x > 2 * width / 3) return k1 * x + b1;
        return 1.0f;
    }
}
