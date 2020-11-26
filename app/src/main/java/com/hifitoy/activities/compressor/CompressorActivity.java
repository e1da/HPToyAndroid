/*
 *   CompressorActivity.java
 *
 *   Created by Artem Khlyupin on 04/19/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.compressor;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.filters.FiltersActivity;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyPreset;
import com.hifitoy.hifitoyobjects.Biquad;
import com.hifitoy.hifitoyobjects.PassFilter;
import com.hifitoy.hifitoyobjects.drc.Drc;
import com.hifitoy.hifitoyobjects.drc.DrcCoef;
import java.util.List;
import java.util.Locale;

public class CompressorActivity extends Activity implements SeekBar.OnSeekBarChangeListener, View.OnTouchListener {
    private static String TAG = "HiFiToy";

    private CompressorView  compressorView;
    private TextView        compressorLabel_outl;
    private SeekBar         compressorSeekBar_outl;

    private Drc drc;

    private GestureDetector mDetector;
    private boolean xHysteresisFlag = false;
    private boolean yHysteresisFlag = false;
    private Point prevTranslation;
    private Point firstTap = new Point(0, 0);
    private PointF delta = new PointF(0.0f,0.0f);
    long prevTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compressor);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();

    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);

        drc = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getDrc();
        setupOutlets();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compressor_menu, menu);
        return true;
    }


    //back button handler
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.time_const_outl:
                Intent intentActivity = new Intent(this, TimeConstActivity.class);
                startActivity(intentActivity);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }


    private void initOutlets() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        compressorView = findViewById(R.id.compressor_view_outl);
        compressorView.setLayoutParams(new LinearLayout.LayoutParams(size.x, size.x));
        compressorView.activePoint = 2;

        compressorView.setOnTouchListener(this);
        mDetector = new GestureDetector(this, new TapGestureListener());

        compressorLabel_outl = findViewById(R.id.compressorLabel_outl);
        compressorSeekBar_outl = findViewById(R.id.compressorSeekBar_outl);

        compressorSeekBar_outl.setOnSeekBarChangeListener(this);
    }

    private void setupOutlets() {
        compressorLabel_outl.setText(String.format(Locale.getDefault(), "%d%%",
                                    (int)(drc.getEnabledChannel((byte)0) * 100)));
        setSeekBar(compressorSeekBar_outl, drc.getEnabledChannel((byte)0));

        updateViews();
    }

    public void setTitleInfo() {
        DrcCoef.DrcPoint p = drc.getCoef17().getPoints().get(compressorView.activePoint);

        float inputDb = ((p.getInputDb() < -23.9f) && (p.getInputDb() > -24.1f)) ?
                                                            0.0f : p.getInputDb() + 24.0f;
        float outputDb = ((p.getOutputDb() < -23.9f) && (p.getOutputDb() > -24.1f)) ?
                                                            0.0f : p.getOutputDb() + 24.0f;

        setTitle(String.format(Locale.getDefault(), "in: %.1fdB  out: %.1fdB",
                            inputDb, outputDb));
    }

    public void updateViews() {
        setTitleInfo();
        compressorView.invalidate();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.equals(compressorSeekBar_outl)){
            drc.setEnabled(getSeekBarPercent(seekBar), (byte)0);
            drc.setEnabled(getSeekBarPercent(seekBar), (byte)1);

            compressorLabel_outl.setText(String.format(Locale.getDefault(), "%d%%",
                                        (int)(drc.getEnabledChannel((byte)0) * 100)));

            drc.sendEnabledToPeripheral((byte)0, false);
            drc.sendEnabledToPeripheral((byte)1, false);
        }
    }

    private float getSeekBarPercent(SeekBar seekBar){//percent=[0.0 .. 1.0]
        return (float)seekBar.getProgress() / seekBar.getMax();
    }
    private void setSeekBar(SeekBar seekBar, float percent){//percent=[0.0 .. 1.0]
        int percentValue = (int)(percent * seekBar.getMax());
        seekBar.setProgress(percentValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Point currentTap = new Point(0, 0);

        //action up and down handlers
        if (event.getAction() == MotionEvent.ACTION_UP){
            //Log.d(TAG, "ACTION_UP");
            v.performClick();
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            //Log.d(TAG, "ACTION_DOWN");

            currentTap.x = (int)event.getX();
            currentTap.y = (int)event.getY();
            firstTap.x = currentTap.x;
            firstTap.y = currentTap.y;
            prevTranslation = new Point(0, 0);
            delta.x = 0;
            delta.y = 0;

            xHysteresisFlag = false;
            yHysteresisFlag = false;
        }

        //run double tap and longpress handler
        mDetector.onTouchEvent(event);

        //moved handler
        if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (event.getEventTime() - prevTime > 40){//time period should be >= 40ms
                //Log.d(TAG, "ACTION_MOVE");
                prevTime = event.getEventTime();

                currentTap.x = (int)event.getX();
                currentTap.y = (int)event.getY();
                Point translation = new Point(currentTap.x - firstTap.x,
                                                currentTap.y - firstTap.y);

                //delta.x -= (int)delta.x;
                //delta.y -= (int)delta.y;

                if (((Math.abs(translation.x) > compressorView.width * 0.05) || (xHysteresisFlag)) &&
                        (!yHysteresisFlag)) {

                    xHysteresisFlag = true;

                    delta.x += (float)(translation.x -  prevTranslation.x) / 12;
                    delta.y = 0;

                } else if (((Math.abs(translation.y) > compressorView.height * 0.05) || (yHysteresisFlag)) &&
                        (!xHysteresisFlag)){

                    yHysteresisFlag = true;

                    delta.x = 0;
                    delta.y += (float)(translation.y -  prevTranslation.y) / 12;

                } else {
                    delta.x = 0;
                    delta.y = 0;

                }

                DrcCoef.DrcPoint np;

                switch (compressorView.activePoint) {
                    case 0:
                        np = updateDrcPoint(drc.getCoef17().getPoint0(), delta);
                        drc.getCoef17().setPoint0WithCheck(np);
                        break;
                    case 1:
                        np = updateDrcPoint(drc.getCoef17().getPoint1(), delta);
                        drc.getCoef17().setPoint1WithCheck(np);
                        break;
                    case 2:
                        np = updateDrcPoint(drc.getCoef17().getPoint2(), delta);
                        drc.getCoef17().setPoint2WithCheck(np);
                        break;
                    case 3:
                        np = updateDrcPoint(drc.getCoef17().getPoint3(), delta);
                        drc.getCoef17().setPoint3WithCheck(np);
                        break;
                }

                prevTranslation = translation;

                drc.getCoef17().sendToPeripheral(false);
                updateViews();
            }
        }
        return true;
    }

    public DrcCoef.DrcPoint updateDrcPoint(DrcCoef.DrcPoint p, PointF delta) {
        float newPX = compressorView.dbToPixelX(p.getInputDb()) + delta.x;
        float newPY = compressorView.dbToPixelY(p.getOutputDb()) + delta.y;

        DrcCoef.DrcPoint np = new DrcCoef.DrcPoint(compressorView.pixelXToDb(newPX),
                                                    compressorView.pixelYToDb(newPY));

        delta.x = newPX - compressorView.dbToPixelX(np.getInputDb());
        delta.y = newPY - compressorView.dbToPixelY(np.getOutputDb());

        //add magnet for point = [-24, -24]
        //if ((np.getInputDb() < -23.95f) && (np.getInputDb() > -24.05f)) np.setInputDb(-24.0f);
        //if ((np.getOutputDb() < -23.95f) && (np.getOutputDb() > -24.05f)) np.setOutputDb(-24.0f);

        return np;
    }

    class TapGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent e) {
            //Log.d(TAG, "onLongPress " + e.getX() + " " + e.getY());
            selectPoint(new Point((int)e.getX(), (int)e.getY()));
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //Log.d(TAG, "onDoubleTap " + e.getX() + " " + e.getY());
            selectPoint(new Point((int)e.getX(), (int)e.getY()));
            return true;
        }

        private void selectPoint(Point tapPoint) {
            //Log.d(TAG, String.format("%d %d", tapPoint.x, tapPoint.y));

            List<DrcCoef.DrcPoint> points = drc.getCoef17().getPoints();

            int counter = compressorView.activePoint + 1;
            for (int i = 0; i < points.size(); i++) {
                if (counter > points.size() - 1) counter = 0;


                if (checkCrossPoint(points.get(counter), tapPoint)) {
                    compressorView.activePoint = counter;
                    updateViews();
                    break;

                }
                counter++;
            }
        }

        private boolean checkCrossPoint(DrcCoef.DrcPoint drcPoint, Point tapPoint) {
            int inputDbPix = (int)compressorView.dbToPixelX(drcPoint.getInputDb());
            int outputDbPix = (int)compressorView.dbToPixelY(drcPoint.getOutputDb());

            return ((Math.abs(tapPoint.x - inputDbPix) < 100) &&
                    (Math.abs(tapPoint.y - outputDbPix) < 100));

        }

    }

}
