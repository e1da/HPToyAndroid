package com.hifitoy.activities.filters.import_fragment;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.widget.FrameLayout;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyPreset;
import com.hifitoy.hifitoydevice.HiFiToyPresetManager;
import com.hifitoy.hifitoyobjects.Filters;

public class PresetIconCollectionView extends FrameLayout {
    private final String TAG = "HiFiToy";

    private HiFiToyPresetManager presetManager;
    private PresetIconView[] presetViews;

    private Point[] viewCenter;
    private int[] viewWidth;

    private int translateX = 0;

    private int activeIndex;

    public PresetIconCollectionView(Context context) {
        super(context);

        presetManager = HiFiToyPresetManager.getInstance();
        String presetName = HiFiToyControl.getInstance().getActiveDevice().getActiveKeyPreset();
        activeIndex = presetManager.getPresetIndex(presetName);

        setBackgroundColor(0xD0000000);

        presetViews = new PresetIconView[presetManager.size()];
        for (int i = 0; i < presetManager.size(); i++) {

            HiFiToyPreset p = presetManager.getPreset(i);
            presetViews[i] = new PresetIconView(context, p);

            addView(presetViews[i]);
        }


        viewCenter = new Point[presetManager.size()];
        for (int i = 0; i < presetManager.size(); i++) {
            viewCenter[i] = new Point();
        }

        viewWidth = new int[presetManager.size()];

    }

    public void setTranslateX(int translateX) {
        this.translateX = translateX;
        Log.d(TAG, "Trans=" + translateX);
    }
    public int getTranslateX() {
        return translateX;
    }

    public void setActiveIndex(int index) {
        activeIndex = index;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = getWidth();
        int height = getHeight();

        updateViewCenter();
        updateViewWidth();

        int wL = width / 3;


        for (int i = 0; i < presetViews.length; i++) {
            //if (center[i].x + wL / 2 < 0) continue;
            //if (center[i].x - wL / 2 > width) continue;

            presetViews[i].setScale((float)viewWidth[i] / wL);
            presetViews[i].layout(viewCenter[i].x - wL / 2, viewCenter[i].y - height / 2,
                    viewCenter[i].x + wL / 2, viewCenter[i].y + height / 2);
        }

    }

    public int getBiggerViewIndex() {
        updateViewCenter();
        updateViewWidth();

        int index = 0;
        for (int i = 0; i < viewWidth.length; i++) {
            if (viewWidth[index] < viewWidth[i]) {
                index = i;
            }
        }
        return index;
    }

    private void updateViewCenter() {
        for (int i = 0; i < viewCenter.length; i++) {
            viewCenter[i].set(getWidth() / 2 + (i - activeIndex) * getWidth() / 3 + translateX,
                    getHeight() / 2);
        }
    }

    private void updateViewWidth() {
        for (int i = 0; i < viewWidth.length; i++) {
            viewWidth[i] = getIconWidth(getWidth(), viewCenter[i].x);
        }
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
}
