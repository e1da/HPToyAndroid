/*
 *   FiltersFragment.java
 *
 *   Created by Artem Khlyupin on 04/07/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.filter_fragment;

import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import com.hifitoy.R;
import com.hifitoy.activities.filters.ViewUpdater;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.biquad.AllpassBiquad;
import com.hifitoy.hifitoyobjects.biquad.IFreq;
import com.hifitoy.hifitoyobjects.biquad.ParamBiquad;
import com.hifitoy.hifitoyobjects.biquad.Type;
import com.hifitoy.hifitoyobjects.filter.Filter;
import com.hifitoy.hifitoyobjects.biquad.Biquad;
import com.hifitoy.hifitoyobjects.filter.HighpassFilter;
import com.hifitoy.hifitoyobjects.filter.LowpassFilter;

import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_ALLPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_PARAMETRIC;

public class FiltersFragment extends Fragment implements View.OnTouchListener, ViewUpdater.IFilterUpdateView {
    private static String TAG = "HiFiToy";

    ViewGroup.LayoutParams  lp;

    private Filter filters;
    private FilterView filterView;
    private GestureDetector mDetector;
    private ScaleGestureDetector mScaleDetector;

    private boolean scaleInProccess = false;
    long prevTime = 0;

    private boolean xHysteresisFlag = false;
    private boolean yHysteresisFlag = false;
    private Point prevTranslation;
    private Point firstTap = new Point(0, 0);
    private double deltaFreq;

    OnSetBackgroundListener setBackgroundListener;

    public interface OnSetBackgroundListener {
        void onSetBackground();
        void onFilterImport();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filters = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getFilters();

        filterView = new FilterView(getActivity(), filters);
        filterView.setOnTouchListener(this);
        registerForContextMenu(filterView);

        mDetector = new GestureDetector(getActivity(), new TapGestureListener());
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
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.filters_popupmenu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_background:
                if (setBackgroundListener != null) {
                    setBackgroundListener.onSetBackground();
                }
                break;

            case R.id.clear_background:
                FiltersBackground.getInstance().clearBitmap();
                ViewUpdater.getInstance().update();
                break;

            case R.id.filter_import:
                if (setBackgroundListener != null) {
                    setBackgroundListener.onFilterImport();
                }
                break;

        }
        return super.onContextItemSelected(item);
    }

    public void setEnabled(boolean enabled) {
        filterView.setEnabled(enabled);
    }

    public void setFilters(Filter f) {
        filters = f;
        filterView.setFilters(f);
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

            xHysteresisFlag = false;
            yHysteresisFlag = false;
            deltaFreq = 0;
        }

        //run scale detector
        mScaleDetector.onTouchEvent(event);
        //return if scale active
        if (scaleInProccess){
            return true;
        }
        //run double tap and longpress handler
        mDetector.onTouchEvent(event);

        //moved handler
        if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (event.getEventTime() - prevTime > 40){//time period should be >= 40ms
                prevTime = event.getEventTime();

                currentTap.x = (int)event.getX();
                currentTap.y = (int)event.getY();
                Point translation = new Point(currentTap.x - firstTap.x,currentTap.y - firstTap.y);

                if (filters.isActiveNullLP()) {
                    float dy = translation.y - prevTranslation.y;

                    if (dy > 300) {
                        LowpassFilter lp = new LowpassFilter(filters);
                        lp.upOrder();
                        prevTranslation.y = translation.y;
                    }

                } else if (filters.isActiveNullHP()) {
                    float dy = translation.y - prevTranslation.y;

                    if (dy > 300) {
                        HighpassFilter hp = new HighpassFilter(filters);
                        hp.upOrder();
                        prevTranslation.y = translation.y;
                    }

                } else {
                    moved(filters.getActiveBiquad(), translation);
                }

                ViewUpdater.getInstance().update();
            }
        }
        return true;
    }

    private void moved(Biquad biquad, Point translation) {
        byte type = Type.getType(biquad);

        IFreq iFreq;
        if (IFreq.class.isAssignableFrom(biquad.getClass())) {
            iFreq = ((IFreq)biquad);
        } else {
            return;
        }

        float dx = (float)(translation.x -  prevTranslation.x) / 4;
        float dy = translation.y -  prevTranslation.y;

        //update freq
        if (((Math.abs(translation.x) > filterView.getWidth() * 0.05) || (xHysteresisFlag)) && (!yHysteresisFlag)) {
            xHysteresisFlag = true;


            deltaFreq -= (int)deltaFreq; //get fraction part
            float freqPix = filterView.freqToPixel(iFreq.getFreq());
            deltaFreq += filterView.pixelToFreq(freqPix + dx) - iFreq.getFreq();


            if (Math.abs(deltaFreq) >= 1.0) {

                if (type == BIQUAD_HIGHPASS) {
                    HighpassFilter hp = new HighpassFilter(filters);
                    hp.setFreq((short)(hp.getFreq() + deltaFreq));
                    hp.sendToPeripheral(false);

                } else if (type == BIQUAD_LOWPASS) {
                    LowpassFilter lp = new LowpassFilter(filters);
                    lp.setFreq((short)(lp.getFreq() + deltaFreq));
                    lp.sendToPeripheral(false);

                } else { // parametric allpass
                    short oldFreq = iFreq.getFreq();
                    iFreq.setFreq((short)(oldFreq + deltaFreq));
                    //ble send
                    biquad.sendToPeripheral(false);
                }
            }

        }
        prevTranslation.x = translation.x;

        // update order or volume
        if ((type == BIQUAD_HIGHPASS) && (!xHysteresisFlag)) {
            HighpassFilter hp = new HighpassFilter(filters);
            if (dy < -300 ) {
                hp.downOrder();

                yHysteresisFlag = true;
                prevTranslation.y = translation.y;

            } else if (dy > 300 ) {
                hp.upOrder();

                yHysteresisFlag = true;
                prevTranslation.y = translation.y;
            }

        } else if ((type == BIQUAD_LOWPASS) && (!xHysteresisFlag)) {
            LowpassFilter lp = new LowpassFilter(filters);
            if (dy < -300 ) {
                lp.downOrder();

                yHysteresisFlag = true;
                prevTranslation.y = translation.y;

            } else if (dy > 300 ) {
                lp.upOrder();

                yHysteresisFlag = true;
                prevTranslation.y = translation.y;
            }

        } else if (type == BIQUAD_PARAMETRIC) {

            if (((Math.abs(translation.y) > filterView.getHeight() * 0.1) || (yHysteresisFlag)) && (!xHysteresisFlag)){
                yHysteresisFlag = true;

                ParamBiquad pb = (ParamBiquad)biquad;
                float newVolInPix = filterView.dbToPixel(pb.getDbVolume()) + dy / 4.0f;
                pb.setDbVolume(filterView.pixelToDb(newVolInPix));

                //ble send
                pb.sendToPeripheral(false);

            }
            prevTranslation.y = translation.y;
        }
    }

    class TapGestureListener extends GestureDetector.SimpleOnGestureListener {
        boolean update = false;

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown");
            update = false;
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            //Log.d(TAG, "onLongPress " + e.getX() + " " + e.getY());
            longPressHandler(new Point((int)e.getX(), (int)e.getY()));
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //Log.d(TAG, "onDoubleTap " + e.getX() + " " + e.getY());
            selectActiveFilter(new Point((int)e.getX(), (int)e.getY()));
            return true;
        }

        private void longPressHandler(Point tapPoint) {
            if ( (filters.isActiveNullHP()) || (filters.isActiveNullLP()) ) return;
            Biquad b = filters.getActiveBiquad();
            byte bIndex = filters.getBiquadIndex(b);

            if ( (!update) && (checkCross(b, tapPoint)) ) {
                byte type = Type.getType(b);
                byte addr = b.getAddress();
                byte bindAddr = b.getBindAddr();

                if (type == BIQUAD_PARAMETRIC) {
                    short freq = ((ParamBiquad)b).getFreq();
                    b = new AllpassBiquad(addr, bindAddr);
                    filters.setBiquad(bIndex, b);

                    b.setEnabled(true);
                    ((AllpassBiquad)b).setFreq(freq);

                    ((AllpassBiquad)b).sendToPeripheral(true);
                    ViewUpdater.getInstance().update();
                    update = true;

                } else if (type == BIQUAD_ALLPASS) {
                    short freq = ((AllpassBiquad)b).getFreq();
                    b = new ParamBiquad(addr, bindAddr);
                    filters.setBiquad(bIndex, b);

                    b.setEnabled(filters.isBiquadEnabled(BIQUAD_PARAMETRIC));
                    ((ParamBiquad)b).setFreq(freq);

                    ((ParamBiquad)b).sendToPeripheral(true);
                    ViewUpdater.getInstance().update();
                    update = true;
                }
            } else {

                if (Build.VERSION.SDK_INT < 24) {
                    filterView.performLongClick();
                } else {
                    filterView.performLongClick(tapPoint.x, tapPoint.y);
                }

            }

        }

        private void selectActiveFilter(Point tapPoint) {
            byte tempIndex = filters.getActiveBiquadIndex();

            LowpassFilter lpf = new LowpassFilter(filters);
            if (lpf.isEmpty()) {
                PointF p = new PointF(filterView.freqToPixel(filterView.maxFreq),
                        filterView.dbToPixel(filters.getAFR(filterView.maxFreq)));

                //check cross
                if ( (Math.abs(p.x - tapPoint.x) < 100) && (Math.abs(p.y - tapPoint.y) < 100) ) {
                    filters.setActiveNullLP(true);
                    ViewUpdater.getInstance().update();
                    return;
                } else {
                    filters.setActiveNullLP(false);
                }
            }

            HighpassFilter hpf = new HighpassFilter(filters);
            if (hpf.isEmpty()) {
                PointF p = new PointF(filterView.freqToPixel(filterView.minFreq),
                        filterView.dbToPixel(filters.getAFR(filterView.minFreq)));

                //check cross
                if ( (Math.abs(p.x - tapPoint.x) < 100) && (Math.abs(p.y - tapPoint.y) < 100) ) {
                    filters.setActiveNullHP(true);
                    ViewUpdater.getInstance().update();
                    return;
                } else {
                    filters.setActiveNullHP(false);
                }
            }


            for (int u = 0; u < filters.getBiquadLength(); u++){
                filters.nextActiveBiquadIndex();
                Biquad b = filters.getActiveBiquad();

                if (checkCross(b, tapPoint)) {
                    ViewUpdater.getInstance().update();
                    return;
                }
            }

            filters.setActiveBiquadIndex(tempIndex);
        }

        //utility methods
        private boolean checkCrossFilters(short freq, float pointX) {
            return (Math.abs(pointX - filterView.freqToPixel(freq)) < 50);
        }

        private boolean checkCrossPassFilters(int startX, int endX, Point tapPoint) {

            for (int pX = startX; pX < endX; pX += 8){
                float ampl = filters.getAFR(filterView.pixelToFreq(pX));
                float pY = filterView.dbToPixel(filterView.amplToDb(ampl));

                if (Math.sqrt(Math.pow(tapPoint.x - pX, 2) + Math.pow(tapPoint.y - pY, 2)) < 50) {
                    return true;
                }
            }

            return false;
        }

        private boolean checkCross(Biquad biquad, Point tapPoint) {
            byte type = Type.getType(biquad);

            if (type == BIQUAD_HIGHPASS) {
                int startX = filterView.freqToPixel(filterView.minFreq);
                int endX = filterView.getHPBorderPixel();
                return checkCrossPassFilters(startX, endX, tapPoint);

            } else if (type == BIQUAD_LOWPASS) {
                int startX = filterView.getLPBorderPixel();
                int endX = filterView.freqToPixel(filterView.maxFreq);
                return checkCrossPassFilters(startX, endX, tapPoint);

            } else if (IFreq.class.isAssignableFrom(biquad.getClass())){
                short freq = ((IFreq)biquad).getFreq();
                return checkCrossFilters(freq, tapPoint.x);

            }

            return false;
        }
    }

    class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            scaleInProccess = true;
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            //Log.d(TAG, "onScale");

            if (Type.getType(filters.getActiveBiquad()) != BIQUAD_PARAMETRIC) return true;
            ParamBiquad pb = (ParamBiquad)filters.getActiveBiquad();

            float q = pb.getQ() / detector.getScaleFactor();
            pb.setQ(q);
            pb.sendToPeripheral(false);

            ViewUpdater.getInstance().update();
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

    public void setBackgroundListener(OnSetBackgroundListener backgroundListener) {
        this.setBackgroundListener = backgroundListener;
    }


}
