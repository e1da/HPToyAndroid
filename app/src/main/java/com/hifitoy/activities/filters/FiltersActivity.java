/*
 *   FiltersActivity.java
 *
 *   Created by Artem Khlyupin on 04/15/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.Biquad;
import com.hifitoy.hifitoyobjects.Filters;
import com.hifitoy.hifitoyobjects.PassFilter;

import java.util.Locale;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Order.BIQUAD_ORDER_1;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Order.BIQUAD_ORDER_2;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_ALLPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_OFF;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_PARAMETRIC;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_USER;

public class FiltersActivity extends Activity implements View.OnTouchListener {
    private static String TAG = "HiFiToy";

    private Filters filters;
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

    MenuItem enabledParam_outl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filterView = new FilterView(this);
        filterView.setOnTouchListener(this);

        filters = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getFilters();
        filterView.filters = filters;
        setContentView(filterView);

        mDetector = new GestureDetector(this, new TapGestureListener());
        mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureListener());

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);

        filters = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getFilters();
        updateViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filters_menu, menu);
        enabledParam_outl = menu.findItem(R.id.enabled_parametrics);
        enabledParam_outl.setTitle(filters.isPEQEnabled() ? "PEQ On" : "PEQ Off");

        return true;
    }


    //back button handler
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.enabled_parametrics:
                filters.setPEQEnabled(!filters.isPEQEnabled());
                enabledParam_outl.setTitle(filters.isPEQEnabled() ? "PEQ On" : "PEQ Off");
                updateViews();
                break;

            case R.id.filters_info:
                DialogSystem.getInstance().showDialog("Info",
                        "To select a filter please double tap on it or tap and hold > 1sec. " +
                                "Horizontal slide changes a frequency, vertical one controls PEQ's gain or LPF/HPF's order. " +
                                "Zoomin-zoomout to control Q of PEQ.", "Close");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTitleInfo() {
        Biquad b = filters.getActiveBiquad();
        byte type = b.getParams().getTypeValue();

        if (filters.isActiveNullLP()) {
            setTitle("LP: Off");

        } else if (filters.isActiveNullHP()){
            setTitle("HP: Off");

        } else if (type == BIQUAD_LOWPASS) {
            PassFilter lp = filters.getLowpass();
            setTitle("LP:" + lp.getInfo());

        } else if (type == BIQUAD_HIGHPASS) {
            PassFilter hp = filters.getHighpass();
            setTitle("HP:" + hp.getInfo());

        } else if (type == BIQUAD_PARAMETRIC) {
            setTitle(String.format(Locale.getDefault(), "PEQ%d: %s",
                                    filters.getActiveBiquadIndex() + 1, b.getInfo()));

        } else if (type == BIQUAD_ALLPASS) {
            setTitle(String.format(Locale.getDefault(), "APF%d: %s",
                    filters.getActiveBiquadIndex() + 1, b.getInfo()));

        } else {
            setTitle("Filters menu");
        }
    }

    public void updateViews() {
        setTitleInfo();
        filterView.invalidate();
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

                    if (dy > 400) {
                        filters.upOrderFor(BIQUAD_LOWPASS);
                        prevTranslation.y = translation.y;
                    }

                } else if (filters.isActiveNullHP()) {
                    float dy = translation.y - prevTranslation.y;

                    if (dy > 400) {
                        filters.upOrderFor(BIQUAD_HIGHPASS);
                        prevTranslation.y = translation.y;
                    }

                } else {
                    moved(filters.getActiveBiquad(), translation);
                }

                updateViews();
            }
        }
        return true;
    }

    private void moved(Biquad biquad, Point translation) {
        byte type = biquad.getParams().getTypeValue();
        if ((type == BIQUAD_OFF) || (type == BIQUAD_USER)) return;

        float dx = (float)(translation.x -  prevTranslation.x) / 4;
        float dy = translation.y -  prevTranslation.y;

        //update freq
        if (((Math.abs(translation.x) > filterView.width * 0.05) || (xHysteresisFlag)) && (!yHysteresisFlag)) {
            xHysteresisFlag = true;


            deltaFreq -= (int)deltaFreq; //get fraction part
            float freqPix = filterView.freqToPixel(biquad.getParams().getFreq());
            deltaFreq += filterView.pixelToFreq(freqPix + dx) - biquad.getParams().getFreq();

            //NSLog(@"dx=%f delta=%f", dx, delta_freq);

            if (Math.abs(deltaFreq) >= 1.0) {

                if (type == BIQUAD_HIGHPASS) {
                    PassFilter hp = filters.getHighpass();
                    PassFilter lp = filters.getLowpass();

                    short newFreq = (short)(hp.getFreq() + deltaFreq);
                    if ((lp != null) && (newFreq > lp.getFreq())) newFreq = lp.getFreq();

                    if (hp.getFreq() != newFreq) {
                        hp.setFreq(newFreq);
                        //ble send
                        hp.sendToPeripheral(false);
                    }

                } else if (type == BIQUAD_LOWPASS) {
                    PassFilter hp = filters.getHighpass();
                    PassFilter lp = filters.getLowpass();

                    short newFreq = (short)(lp.getFreq() + deltaFreq);
                    if ((hp != null) && (newFreq < hp.getFreq())) newFreq = hp.getFreq();

                    if (lp.getFreq() != newFreq) {
                        lp.setFreq(newFreq);
                        //ble send
                        lp.sendToPeripheral(false);
                    }

                } else { // parametric allpass
                    short oldFreq = biquad.getParams().getFreq();
                    biquad.getParams().setFreq((short)(oldFreq + deltaFreq));
                    //ble send
                    biquad.sendToPeripheral(false);
                }
            }

        }
        prevTranslation.x = translation.x;

        // update order or volume
        if ((type == BIQUAD_HIGHPASS) || (type == BIQUAD_LOWPASS))  {
            if (!xHysteresisFlag) {
                if (dy < -400 ) {
                    filters.downOrderFor(type); // decrement order

                    yHysteresisFlag = true;
                    prevTranslation.y = translation.y;

                } else if (dy > 400 ) {
                    filters.upOrderFor(type); // increment order

                    yHysteresisFlag = true;
                    prevTranslation.y = translation.y;
                }
            }

        } else if (type == BIQUAD_PARAMETRIC) {

            if (((Math.abs(translation.y) > filterView.height * 0.1) || (yHysteresisFlag)) && (!xHysteresisFlag)){
                yHysteresisFlag = true;

                float newVolInPix = filterView.dbToPixel(biquad.getParams().getDbVolume()) + dy / 4.0f;
                biquad.getParams().setDbVolume(filterView.pixelToDb(newVolInPix));

                //ble send
                biquad.sendToPeripheral(false);

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

            if ( (!update) && (checkCrossParamFilters(b, tapPoint.x)) ) {
                if (b.getParams().getTypeValue() == BIQUAD_PARAMETRIC) {
                    b.setEnabled(true);
                    b.getParams().setOrderValue(BIQUAD_ORDER_1);
                    b.getParams().setTypeValue(BIQUAD_ALLPASS);

                    b.sendToPeripheral(true);
                    updateViews();
                    update = true;

                } else if (b.getParams().getTypeValue() == BIQUAD_ALLPASS) {
                    b.setEnabled(filters.isPEQEnabled());
                    b.getParams().setOrderValue(BIQUAD_ORDER_2);
                    b.getParams().setTypeValue(BIQUAD_PARAMETRIC);
                    b.getParams().setQFac(1.41f);
                    b.getParams().setDbVolume(0.0f);

                    b.sendToPeripheral(true);
                    updateViews();
                    update = true;
                }
            }

        }

        private void selectActiveFilter(Point tapPoint) {
            byte tempIndex = filters.getActiveBiquadIndex();

            if (filters.getLowpass() == null) {
                PointF p = new PointF(filterView.freqToPixel(filterView.maxFreq),
                        filterView.dbToPixel(filters.getAFR(filterView.maxFreq)));

                //check cross
                if ( (Math.abs(p.x - tapPoint.x) < 30) && (Math.abs(p.y - tapPoint.y) < 30) ) {
                    filters.setActiveNullLP(true);
                    updateViews();
                    return;
                } else {
                    filters.setActiveNullLP(false);
                }
            }

            if (filters.getHighpass() == null) {
                PointF p = new PointF(filterView.freqToPixel(filterView.minFreq),
                        filterView.dbToPixel(filters.getAFR(filterView.minFreq)));

                //check cross
                if ( (Math.abs(p.x - tapPoint.x) < 30) && (Math.abs(p.y - tapPoint.y) < 30) ) {
                    filters.setActiveNullHP(true);
                    updateViews();
                    return;
                } else {
                    filters.setActiveNullHP(false);
                }
            }


            for (int u = 0; u < filters.getBiquadLength(); u++){
                filters.nextActiveBiquadIndex();
                Biquad b = filters.getActiveBiquad();

                byte type = b.getParams().getTypeValue();

                if ((type != BIQUAD_LOWPASS) && (type != BIQUAD_HIGHPASS) && (type != BIQUAD_PARAMETRIC) && (type != BIQUAD_ALLPASS)) {
                    continue;
                }


                if (checkCross(b, tapPoint)) {
                    updateViews();
                    return;
                }
            }

            filters.setActiveBiquadIndex(tempIndex);
        }
    }

    class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            scaleInProccess = true;
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            //Log.d(TAG, "onScale");
            Biquad b = filters.getActiveBiquad();
            if (b.getParams().getTypeValue() != BIQUAD_PARAMETRIC) return true;

            float q = b.getParams().getQFac() / detector.getScaleFactor();
            b.getParams().setQFac(q);
            b.sendToPeripheral(false);

            updateViews();
            return true;
        }

    }

    //utility methods
    private boolean checkCrossParamFilters(Biquad biquad, float pointX) {
        return (Math.abs(pointX - filterView.freqToPixel(biquad.getParams().getFreq())) < 50);
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

        if (biquad.getParams().getTypeValue() == BIQUAD_HIGHPASS) {
            int startX = filterView.freqToPixel(filterView.minFreq);
            int endX = filterView.getHPBorderPixel();
            return checkCrossPassFilters(startX, endX, tapPoint);

        } else if (biquad.getParams().getTypeValue() == BIQUAD_LOWPASS) {
            int startX = filterView.getLPBorderPixel();
            int endX = filterView.freqToPixel(filterView.maxFreq);
            return checkCrossPassFilters(startX, endX, tapPoint);

        }

        //parametric, allpass
        return checkCrossParamFilters(biquad, tapPoint.x);
    }

}
