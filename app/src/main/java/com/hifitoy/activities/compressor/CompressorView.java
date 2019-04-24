/*
 *   CompressorView.java
 *
 *   Created by Artem Khlyupin on 04/19/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.compressor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.view.View;
import com.hifitoy.hifitoyobjects.BinaryOperation;

public class CompressorView extends View {
    private final int MIN_VIEW_DB   = -120;
    private final int MAX_VIEW_DB   = 24;
    private final int GRID_STEP     = 12;
    private final int DB_STEP       = 24;

    private int width;
    private int height;

    private int border_left;
    private int border_right;
    private int border_top;
    private int border_bottom;

    private double a_coef;
    private double b_coef;
    private double c_coef;
    private double d_coef;

    public CompressorView(Context context) {
        super(context);
        init();
    }
    public CompressorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        setBackgroundColor(Color.BLACK);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        refreshView(canvas);
        drawGrid(canvas);
        drawGridUnit(canvas);
        //drawFilter(canvas);
    }

    /*----------------------------------- Draw Methods -----------------------------------------*/
    private void drawGrid(Canvas canvas) {
        float[] points = new float[0];

        //draw horizontal line
        for (int i = MAX_VIEW_DB; i >= MIN_VIEW_DB; i -= GRID_STEP){
            float[] p = new float[]{    border_left, (float)dbToPixelY(i),
                                        width - border_right, (float)dbToPixelY(i) };

            points = BinaryOperation.concatData(points, p);

        }

        //draw vertical line
        for (int i = MAX_VIEW_DB; i >= MIN_VIEW_DB; i -= GRID_STEP){
            float[] p = new float[]{    (float)dbToPixelX(i), border_top,
                                        (float)dbToPixelX(i), height - border_bottom };

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
        Paint p = new Paint();
        p.setColor(Color.GRAY);
        p.setTextSize(0.5f * border_bottom);
        p.setTextAlign(Paint.Align.RIGHT);
        p.setTypeface(Typeface.create("Arial", Typeface.BOLD));

        //draw y-axis db units
        for (int i = MAX_VIEW_DB; i > MIN_VIEW_DB; i -= DB_STEP){
            String dbString = Integer.toString(i);

            int textHeight = getTextBound(dbString, p).height();
            canvas.drawText(dbString, dbToPixelX(MIN_VIEW_DB) - 5, dbToPixelY(i) + (int)(textHeight / 2), p);
        }

        //draw x-axis db units
        p.setTextSize(0.5f * border_bottom);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("Arial", Typeface.BOLD));

        for (int i = MAX_VIEW_DB; i > MIN_VIEW_DB; i -= DB_STEP){
            String dbString = Integer.toString(i + 24);

            int textWidth = (i != 0) ? getTextBound(dbString, p).width() : 0;
            canvas.drawText(dbString, dbToPixelX(i) - (int)(textWidth / 2), 	height - 0.5f * border_bottom, p);
        }

    }

    /* ----------------------- math calculation ------------------------*/
    private float dbToPixelX(float db) {
        return (float)(a_coef * db + b_coef);
    }

    private double  pixelXToDb(double pixel) {
        if (pixel > width - border_right) pixel = width - border_right;
        if (pixel < border_left) pixel = border_left;

        return (pixel - b_coef) / a_coef;
    }

    private float  dbToPixelY(double db) {
        return (float)(c_coef * db + d_coef);
    }

    private double  pixelYToDb(double pixel) {
        if (pixel > height - border_bottom) pixel = height - border_bottom;
        if (pixel < border_top) pixel = border_top;

        return (pixel - d_coef) / c_coef;
    }

    /*------------------------------ Draw Calculation --------------------------------------------*/
    private void refreshView(Canvas canvas) {
        border_left     = 60;
        border_right    = 40;
        border_top      = 40;
        border_bottom   = 60;

        height  = canvas.getWidth();
        width   = canvas.getHeight();

        /*    a_coef*log10(MAX_FREQ)+b_coef = width - border_right
         a_coef*log10(MIN_FREQ)+b_coef = border_left
         =>
         a_coef*log10(MAX_FREQ) + border_left - a_coef*log10(MIN_FREQ) = width - border_right
         a_coef*(log10(MAX_FREQ)-log10(MIN_FREQ)) = width - (border_left + border_right)

         */
        a_coef = (double)(width - (border_left + border_right)) / (MAX_VIEW_DB - MIN_VIEW_DB);
        b_coef = border_left - a_coef * MIN_VIEW_DB;

        /*    15*c_coef + d_coef = border_top
         *  -30*c_coef + d_coef = height - border_bottom
         *  =>
         *  15*c_coef + height - border_bottom + 30*c_coef = border_top
         *  c_coef = (border_top + border_bottom - height) / (15 + 30)
         */
        c_coef = (double)(border_top + border_bottom - height) / (MAX_VIEW_DB - MIN_VIEW_DB);
        d_coef = height - border_bottom - c_coef * MIN_VIEW_DB;


    }

}
