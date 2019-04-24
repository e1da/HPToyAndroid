/*
 *   FilterView.java
 *
 *   Created by Artem Khlyupin on 04/16/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import com.hifitoy.hifitoyobjects.BinaryOperation;
import com.hifitoy.hifitoyobjects.Biquad;
import com.hifitoy.hifitoyobjects.Filters;

import java.util.ArrayList;
import java.util.List;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_ALLPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_PARAMETRIC;

public class FilterView extends View {
    private static String TAG = "HiFiToy";

    public Filters filters;
    private int[] drawFreqUnitArray = new int[]{20, 100, 1000, 10000};

    private final int MIN_VIEW_DB = -30;
    private final int MAX_VIEW_DB = 15;
    private final int DELTA_X = 8;

    public int width;
    public int height;

    private int border_left;
    private int border_right;
    private int border_top;
    private int border_bottom;

    private double a_coef;
    private double b_coef;
    private double c_coef;
    private double d_coef;

    public int maxFreq = 20000;
    public int minFreq = 20;


    public FilterView(Context context) {
        super(context);
    }

    public int freqToPixel(int freq){
        return (int)(a_coef * Math.log10(freq) + b_coef);
    }

    public float pixelToFreq(float pix){
        return (float)Math.pow(10, (pix - b_coef) / a_coef);
    }

    public float dbToAmpl(float db) {
        return (float)Math.pow(10, (db / 20));
    }

    public float amplToDb(float ampl) {
        return (float)(20 * Math.log10(ampl));
    }

    public float dbToPixel(float db){
        return (float)(c_coef * db + d_coef);
    }

    public float pixelToDb(float pix) {
        return (float)((pix - d_coef) / c_coef);
    }

    public int getLPBorderPixel() {
        int startPixel = width - border_right;
        double prev_y = filters.getAFR(pixelToFreq(startPixel));
        int extremumPixel = startPixel - 1;

        while (extremumPixel > border_left){
            double y = filters.getAFR(pixelToFreq(extremumPixel));
            if (y < prev_y){
                break;
            }
            prev_y = y;
            extremumPixel -= DELTA_X;
        }
        return extremumPixel;
    }

    public int getHPBorderPixel() {
        int startPixel = border_left;
        double prev_y = filters.getAFR(pixelToFreq(startPixel));
        int extremumPixel = startPixel + 1;

        while (extremumPixel < (width - border_right)){
            double y = filters.getAFR(pixelToFreq(extremumPixel));
            if ( y < prev_y ){
                break;
            }
            prev_y = y;
            extremumPixel += DELTA_X;
        }
        return extremumPixel;
    }


    private void refreshCoef(Canvas canvas){
        width = canvas.getWidth();
        height = canvas.getHeight();

        border_left = 50;
        border_right = 20;
        border_top = 40/*init_height + 10*/;
        border_bottom = 40;

        /*	a_coef*log10(MAX_FREQ)+b_coef = width - border_right
         a_coef*log10(MIN_FREQ)+b_coef = border_left
         =>
         a_coef*log10(MAX_FREQ) + border_left - a_coef*log10(MIN_FREQ) = width - border_right
         a_coef*(log10(MAX_FREQ)-log10(MIN_FREQ)) = width - (border_left + border_right)

         */

        a_coef = (double)(width - (border_left + border_right)) / (Math.log10(maxFreq) - Math.log10(minFreq));
        b_coef = border_left - a_coef * Math.log10(minFreq);

        /*	15*c_coef + d_coef = border_top
         *  -30*c_coef + d_coef = height - border_bottom
         *  =>
         *  15*c_coef + height - border_bottom + 30*c_coef = border_top
         *  c_coef = (border_top + border_bottom - height) / (15 + 30)
         */
        c_coef = (double)(border_top + border_bottom - height) / (MAX_VIEW_DB - MIN_VIEW_DB);
        d_coef = height - border_bottom - MIN_VIEW_DB * c_coef;
    }

    private void drawGrid(Canvas canvas) {
        float[] points = new float[0];

        int weight = 10;
        int freq = minFreq;

        //draw vertical lines
        while (freq <= maxFreq){
            float[] p = new float[]{    freqToPixel(freq), border_top,
                                        freqToPixel(freq), height - border_bottom };
            points = BinaryOperation.concatData(points, p);

            if (freq >= 100) weight = 100;
            if (freq >= 1000) weight = 1000;
            if (freq >= 10000) weight = 10000;

            freq += weight;
        }

        //draw horizontal line
        for (int i = MAX_VIEW_DB; i >= MIN_VIEW_DB; i -= 5){
            float[] p = new float[]{    border_left, dbToPixel(i),
                                        width - border_right, dbToPixel(i) };

            points = BinaryOperation.concatData(points, p);
        }

        Paint paint = new Paint();
        paint.setARGB((int)(1.0f * 255), (int)(0.5f * 255), (int)(0.5f * 255), (int)(0.5f * 255));

        canvas.drawLines(points, paint);
    }

    private Rect getTextBound(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds;
    }

    private void drawGridUnit(Canvas canvas) {
        if (drawFreqUnitArray != null){
            for (int i = 0; i < drawFreqUnitArray.length; i++){
                int drawFreq = drawFreqUnitArray[i];

                //draw freq unit string
                String freqString;
                if (drawFreq >= 1000){
                    freqString = Integer.toString(drawFreq / 1000) + "kHz";
                } else {
                    freqString = Integer.toString(drawFreq) + "Hz";
                }

                Paint p = new Paint();
                p.setColor(Color.GRAY);
                p.setTextSize(0.75f * border_bottom);
                p.setTypeface(Typeface.create("Arial", Typeface.BOLD));

                int textWidth = (i != 0) ? getTextBound(freqString, p).width() : 0;
                canvas.drawText(freqString, freqToPixel(drawFreq) - textWidth / 2, 	height - 0.2f * border_bottom, p);
            }
        }

        //draw horizontal line and db units
        for (int i = MAX_VIEW_DB; i > MIN_VIEW_DB; i -= 5){
            String dbString = Integer.toString(i);

            Paint p = new Paint();
            p.setColor(Color.GRAY);
            p.setTextSize(0.5f * border_bottom);
            p.setTextAlign(Paint.Align.RIGHT);
            p.setTypeface(Typeface.create("Arial", Typeface.BOLD));

            int textHeight = getTextBound(dbString, p).height();
            canvas.drawText(dbString, freqToPixel(minFreq) - 5, dbToPixel(i) + textHeight / 2, p);
        }
    }

    private void drawFilterShadow(Canvas c, Path path) {
        //get last point
        PathMeasure measure = new PathMeasure(path, false);

        float[] lastPoint = new float[]{0.0f, 0.0f};
        measure.getPosTan(measure.getLength(), lastPoint, null);


        if (lastPoint[0] < width - border_right) {
            path.lineTo(lastPoint[0], dbToPixel(MIN_VIEW_DB));
            path.lineTo(width - border_right, dbToPixel(MIN_VIEW_DB));
        }
        path.lineTo(width - border_right, dbToPixel(0.0f));
        path.lineTo(border_left, dbToPixel(0.0f));

        //get first point
        float[] firstPoint = new float[]{0.0f, 0.0f};
        measure.getPosTan(0, firstPoint, null);

        if (firstPoint[0] > border_left) {
            path.lineTo(border_left, dbToPixel(MIN_VIEW_DB));
            path.lineTo(firstPoint[0], dbToPixel(MIN_VIEW_DB));
        }
        path.lineTo(firstPoint[0], firstPoint[1]);

        //draw
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x2000FFFF);

        c.drawPath(path, p);
    }

    private Path getPathFromList(List<Point> points) {
        if (points.size() < 2) return null;

        Path path = new Path();
        path.moveTo(points.get(0).x, points.get(0).y);

        for (int i = 1; i < points.size(); i++) {
            Point p = points.get(i);
            path.lineTo(p.x, p.y);
        }
        return path;
    }

    private void drawFilter(Canvas canvas) {
        int highpass_freq_pix = 0;
        int lowpass_freq_pix = 0;

        byte type = filters.getActiveBiquad().getParams().getTypeValue();
        if (type == BIQUAD_HIGHPASS) highpass_freq_pix = getHPBorderPixel();
        if (type == BIQUAD_LOWPASS) lowpass_freq_pix = getLPBorderPixel();

        List<Point> activePoints = new ArrayList<>();
        List<Point> points = new ArrayList<>();

        //prepare vertices
        for (int i = border_left; i <= width - border_right; i += DELTA_X){
            float ampl = filters.getAFR(pixelToFreq(i));
            float db = amplToDb(ampl);
            if ( (db < MIN_VIEW_DB) || (db > MAX_VIEW_DB) ) continue;

            if ((type == BIQUAD_HIGHPASS) && (i <= highpass_freq_pix)) {
                activePoints.add( new Point(i, (int)dbToPixel(db)) );

                if (i + DELTA_X > highpass_freq_pix) {
                    points.add( new Point(i, (int)dbToPixel(db)) );
                }

            } else if ((type == BIQUAD_LOWPASS) && (i >= lowpass_freq_pix)) {
                activePoints.add( new Point(i, (int)dbToPixel(db)) );

                if (i - DELTA_X < lowpass_freq_pix) {
                    points.add( new Point(i, (int)dbToPixel(db)) );
                }

            } else {
                points.add( new Point(i, (int)dbToPixel(db)) );
            }

        }

        //draw filters shadow
        if ((activePoints.size() > 0) || (points.size() > 0)) {
            List<Point> allPoints;

            if ((activePoints.size() > 0) && (points.size() > 0)) {
                if (points.get(0).x < activePoints.get(0).x) {
                    allPoints = new ArrayList<>(points);
                    allPoints.addAll(activePoints);
                } else {
                    allPoints = new ArrayList<>(activePoints);
                    allPoints.addAll(points);
                }
            } else if (activePoints.size() > 0) {
                allPoints = new ArrayList<>(activePoints);

            } else {
                allPoints = new ArrayList<>(points);
            }

            //draw filters shadow
            Path p = getPathFromList(allPoints);
            drawFilterShadow(canvas, p);
        }

        //draw active stroke
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);

        //draw active stroke
        paint.setARGB((int)(1.0f * 255), (int)(1.0f * 255), (int)(0.5f * 255), (int)(0.0f * 255));
        Path p = getPathFromList(activePoints);
        if (p != null) canvas.drawPath(p, paint);

        //draw normal stroke
        paint.setARGB((int)(1.0f * 255), (int)(0.66f * 255), (int)(0.66f * 255), (int)(0.66f * 255));
        p = getPathFromList(points);
        if (p != null) canvas.drawPath(p, paint);

        drawFreqLineForParametric(canvas);
        drawFreqLineForAllpass(canvas);
        drawPassFilterTap(canvas);
    }

    private void drawFreqLineForParametric(Canvas c) {
        List<Biquad> params = filters.getBiquads(BIQUAD_PARAMETRIC);
        drawFreqForBiquads(c, params, 0xFF996633, 0xFFFF8000);
    }

    private void drawFreqLineForAllpass(Canvas c) {
        List<Biquad> allpass = filters.getBiquads(BIQUAD_ALLPASS);
        drawFreqForBiquads(c, allpass, 0x803380FF, 0xFF3380FF);
    }

    private void drawFreqForBiquads(Canvas c, List<Biquad> biquads, int color, int selColor) {
        if (biquads == null) return;

        for (int i = 0; i < biquads.size(); i++) {
            Biquad b = biquads.get(i);

            if (!b.isEnabled()) continue;

            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setPathEffect(new DashPathEffect(new float[]{30, 30, 30, 30}, 0));

            if ((b == filters.getActiveBiquad()) && (!filters.isActiveNullLP()) && (!filters.isActiveNullHP())){
                p.setStrokeWidth(6);
                p.setColor(selColor);
            } else {
                p.setStrokeWidth(4);
                p.setColor(color);
            }

            Path path = new Path();

            path.moveTo(freqToPixel(b.getParams().getFreq()), border_top);
            path.lineTo(freqToPixel(b.getParams().getFreq()), height - border_bottom);
            c.drawPath(path, p);
        }

    }

    private void drawPassFilterTap(Canvas c) {
        final int TAP_RADIUS = 15;

        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);

        if (filters.getLowpass() == null) {

            if (filters.isActiveNullLP()) {
                p.setColor(0xFFFF8000); // orange
            } else {
                p.setColor(0xFF996633); // brown
            }

            float ampl = filters.getAFR(pixelToFreq(width - border_right));
            float db = amplToDb(ampl);
            if ((db >= MIN_VIEW_DB) && (db <= MAX_VIEW_DB)) {
                //c.drawCircle(width - border_right, dbToPixel(db), TAP_RADIUS, p);
                Path path = new Path();
                path.moveTo(width - border_right, dbToPixel(db) - 20);
                path.lineTo(width - border_right, dbToPixel(db) + 20);
                path.lineTo(width - border_right - 40, dbToPixel(db) + 3);
                path.lineTo(width - border_right - 40, dbToPixel(db) - 3);
                c.drawPath(path, p);
            }
        }

        if (filters.getHighpass() == null) {

            if (filters.isActiveNullHP()) {
                p.setColor(0xFFFF8000); // orange
            } else {
                p.setColor(0xFF996633); // brown
            }

            float ampl = filters.getAFR(pixelToFreq(border_left));
            float db = amplToDb(ampl);
            if ((db >= MIN_VIEW_DB) && (db <= MAX_VIEW_DB)) {
                //c.drawCircle(border_left, dbToPixel(db), TAP_RADIUS, p);
                Path path = new Path();
                path.moveTo(border_left, dbToPixel(db) - 20);
                path.lineTo(border_left, dbToPixel(db) + 20);
                path.lineTo(border_left + 40, dbToPixel(db) + 3);
                path.lineTo(border_left + 40, dbToPixel(db) - 3);
                c.drawPath(path, p);
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        refreshCoef(canvas);
        drawGrid(canvas);
        drawGridUnit(canvas);
        drawFilter(canvas);
    }
}
