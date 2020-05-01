/*
 *   PresetImportFragment.java
 *
 *   Created by Artem Khlyupin on 05/01/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.import_fragment;

import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class FilterImportFragment extends Fragment implements View.OnTouchListener {

    PresetIconCollectionView presetCollectionView;

    private long prevTime = 0;
    private Point firstTap = new Point(0, 0);
    private Point prevTranslation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        presetCollectionView = new PresetIconCollectionView(getActivity());
        presetCollectionView.setOnTouchListener(this);

        return presetCollectionView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Point currentTap = new Point(0, 0);

        //action up and down handlers
        if (event.getAction() == MotionEvent.ACTION_UP){
            v.performClick();

            //update active preset
            int index = presetCollectionView.getBiggerViewIndex();
            presetCollectionView.setActiveIndex(index);
            //presetStorage.setActiveIndex(index);

            //redraw preset collection view
            presetCollectionView.setTranslateX(0);
            presetCollectionView.requestLayout();
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN){

            currentTap.x = (int)event.getX();
            currentTap.y = (int)event.getY();
            firstTap.x = currentTap.x;
            firstTap.y = currentTap.y;
            prevTranslation = new Point(0, 0);
        }

        //moved handler
        if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (event.getEventTime() - prevTime > 40){//time period should be >= 40ms
                prevTime = event.getEventTime();

                currentTap.x = (int)event.getX();
                currentTap.y = (int)event.getY();
                Point translation = new Point(currentTap.x - firstTap.x,currentTap.y - firstTap.y);

                presetCollectionView.setTranslateX(translation.x);
                presetCollectionView.requestLayout();

            }
        }
        return true;
    }

}
