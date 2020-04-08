/*
 *   BackConfigFragment.java
 *
 *   Created by Artem Khlyupin on 04/08/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.filter_fragment;

import android.app.Fragment;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import com.hifitoy.activities.filters.ViewUpdater;

public class BackConfigFragment extends Fragment implements View.OnTouchListener, ViewUpdater.IFilterUpdateView  {
    private static String TAG = "HiFiToy";

    ViewGroup.LayoutParams  lp;

    private FilterView filterView;
    private ScaleGestureDetector mScaleDetector;

    private boolean scaleInProccess = false;
    long prevTime = 0;

    private Point prevTranslation;
    private Point firstTap = new Point(0, 0);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filterView = new FilterView(getActivity());
        filterView.filters = null;
        filterView.drawFilterEnabled = false;

        filterView.setOnTouchListener(this);
        registerForContextMenu(filterView);

        mScaleDetector = new ScaleGestureDetector(getActivity(), new ScaleGestureListener());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return filterView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (lp != null) {
            getView().setLayoutParams(lp);
        }

        ViewUpdater.getInstance().addUpdateView(this);
        ViewUpdater.getInstance().update();

    }

    @Override
    public void onPause() {
        super.onPause();
        ViewUpdater.getInstance().removeUpdateView(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Point currentTap = new Point(0, 0);

        //action up and down handlers
        if (event.getAction() == MotionEvent.ACTION_UP){
            v.performClick();

            scaleInProccess = false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN){

            currentTap.x = (int)event.getX();
            currentTap.y = (int)event.getY();
            firstTap.x = currentTap.x;
            firstTap.y = currentTap.y;
            prevTranslation = new Point(0, 0);

        }

        //run scale detector
        mScaleDetector.onTouchEvent(event);
        //return if scale active
        if (scaleInProccess){
            return true;
        }

        //moved handler
        if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (event.getEventTime() - prevTime > 40){//time period should be >= 40ms
                prevTime = event.getEventTime();

                currentTap.x = (int)event.getX();
                currentTap.y = (int)event.getY();
                Point translation = new Point(currentTap.x - firstTap.x,currentTap.y - firstTap.y);

                movedBackground(translation);

            }
            updateView();
        }

        return true;
    }

    private void movedBackground(Point translation) {
        if ((Math.abs(translation.x) > filterView.width * 0.05) || (Math.abs(translation.y) > filterView.height * 0.05)) {
            float dx = translation.x - prevTranslation.x;
            float dy = translation.y - prevTranslation.y;

            FiltersBackground.getInstance().setTranslate(new PointF(dx, dy));
        }

        prevTranslation = translation;
    }

    class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            scaleInProccess = true;
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            //Log.d(TAG, "onScale");
            FiltersBackground.getInstance().setScale(detector.getScaleFactor());

            updateView();
            return true;
        }

    }

    @Override
    public void updateView() {
        filterView.invalidate();
    }

    public void setLayoutParams(ViewGroup.LayoutParams lp) {
        this.lp = lp;

        if (getView() != null) {
            getView().setLayoutParams(lp);
        }
    }

}
