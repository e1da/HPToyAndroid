/*
 *   PresetImportFragment.java
 *
 *   Created by Artem Khlyupin on 05/01/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.import_fragment;

import android.animation.ValueAnimator;
import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.ToyPreset;
import com.hifitoy.hifitoyobjects.Filters;

public class FilterImportFragment extends Fragment implements View.OnTouchListener {
    private final String TAG = "HiFiToy";

    private final FilterCollection filterCollection = new FilterCollection();
    private PresetIconCollectionView presetCollectionView;

    private long prevTime = 0;
    private Point firstTap = new Point(0, 0);

    private Filters tempFilters;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        presetCollectionView = new PresetIconCollectionView(getActivity(), filterCollection);
        presetCollectionView.setOnTouchListener(this);

        return presetCollectionView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            String presetName = HiFiToyControl.getInstance().getActiveDevice().getActiveKeyPreset();
            filterCollection.setActiveIndex(presetName);
            presetCollectionView.requestLayout();

            try {
                tempFilters = filterCollection.getActiveFilter().clone();

            } catch (CloneNotSupportedException e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Point currentTap = new Point(0, 0);

        //action up and down handlers
        if (event.getAction() == MotionEvent.ACTION_UP){
            v.performClick();

            //find index and translate to center of new preset
            int oldIndex = filterCollection.getActiveIndex();
            int newIndex = presetCollectionView.getBiggerViewIndex();
            int trans = presetCollectionView.getViewCenter(newIndex).x - presetCollectionView.getWidth() / 2;

            if (oldIndex != newIndex) {
                filterCollection.setActiveIndex(newIndex);
                updateFilters(filterCollection.getActiveFilter());
            }

            //smooth animation
            ValueAnimator animator = ValueAnimator.ofInt(trans, 0);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    filterCollection.setTranslateX((int)animation.getAnimatedValue());
                    presetCollectionView.requestLayout();
                }
            });
            animator.start();
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN){

            currentTap.x = (int)event.getX();
            currentTap.y = (int)event.getY();
            firstTap.x = currentTap.x;
            firstTap.y = currentTap.y;
        }

        //moved handler
        if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (event.getEventTime() - prevTime > 40){//time period should be >= 40ms
                prevTime = event.getEventTime();

                currentTap.x = (int)event.getX();
                currentTap.y = (int)event.getY();
                Point translation = new Point(currentTap.x - firstTap.x,currentTap.y - firstTap.y);

                filterCollection.setTranslateX(translation.x);
                presetCollectionView.requestLayout();
            }
        }
        return true;
    }

    private void updateFilters(Filters f) {
        ToyPreset preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();
        preset.setFilters(f);
        f.sendToPeripheral(true);
    }

    public void cancelUpdateFilters() {
        updateFilters(tempFilters);
    }

}
