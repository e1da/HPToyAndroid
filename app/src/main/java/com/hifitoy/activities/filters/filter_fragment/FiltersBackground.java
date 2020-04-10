/*
 *   FilterBackground.java
 *
 *   Created by Artem Khlyupin on 07/24/2019.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.filter_fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.util.Size;

public class FiltersBackground {
    private static String TAG = "HiFiToy";
    private static FiltersBackground instance;

    private Bitmap  bitmap;
    private boolean scaleTypeX;
    private PointF  scale;
    private PointF  relativeTranslate;


    public FiltersBackground() {
        setBitmap(null);
    }

    public FiltersBackground(Bitmap bitmap) {
        setBitmap(bitmap);
    }

    public static synchronized FiltersBackground getInstance() {
        if (instance == null){
            instance = new FiltersBackground();
        }
        return instance;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;

        scaleTypeX = true;
        scale = new PointF(1.0f, 1.0f);
        relativeTranslate = new PointF(0.0f, 0.0f);
    }

    public void clearBitmap() {
        setBitmap(null);
    }

    public void mirrorX() {
        if (bitmap == null) return;
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public void setScaleTypeX(boolean scaleTypeX) {
        this.scaleTypeX = scaleTypeX;
    }
    public boolean isScaleTypeX() {
        return scaleTypeX;
    }
    public void invertScaleType() {
        scaleTypeX = !scaleTypeX;
    }
    public String getScaleTypeString() {
        return (scaleTypeX) ? "X-Scale" : "Y-Scale";
    }

    public void setScale(float deltaScale) {
        if (bitmap == null) return;

        if (deltaScale > 1) {
            deltaScale = (deltaScale - 1) / 4 + 1;
        } else {
            deltaScale = 1.0f - (1.0f - deltaScale) / 4;
        }
        
        if (scaleTypeX) {
            float sx = scale.x * deltaScale;
            if ((sx > 16) || (sx < 0.0625f)) return;

            scale.x = sx;
        } else {
            float sy = scale.y * deltaScale;
            if ((sy > 16) || (sy < 0.0625f)) return;

            scale.y = sy;
        }
    }

    public void setTranslate(PointF deltaTranslate, Size rectSize) {
        if (bitmap == null) return;

        relativeTranslate.x += deltaTranslate.x / 4 / rectSize.getWidth();
        relativeTranslate.y += deltaTranslate.y / 4 / rectSize.getWidth();

    }

    public Point getTranslate(Size rectSize) {
        return new Point((int)(relativeTranslate.x * rectSize.getWidth()),
                            (int)(relativeTranslate.y * rectSize.getHeight()));
    }

    public void drawInRect(Canvas c, Rect dst, Point baseCenter) {
        if ((bitmap == null) || (dst == null)) return;

        //calc ratio between dstView and bitmap rects
        float ratioX = (float)dst.width() / bitmap.getWidth();
        float ratioY = (float)dst.height() / bitmap.getHeight();

        //matrix transform bitmap to dstView depending on scale and translate params
        Matrix matrix = new Matrix();
        matrix.setScale(ratioX * scale.x, ratioY * scale.y);

        Point translate = getTranslate(new Size(dst.width(), dst.height()));

        Point center = new Point(baseCenter.x - translate.x, baseCenter.y - translate.y);
        Point scaleCenter = new Point((int)(center.x * scale.x), (int)(center.y * scale.y));
        Point deltaCenter = new Point(center.x - scaleCenter.x, center.y - scaleCenter.y);

        matrix.postTranslate(deltaCenter.x, deltaCenter.y);

        Bitmap bb =  Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        //clip bounds
        int left = dst.left + deltaCenter.x + translate.x;
        int top = dst.top + deltaCenter.y + translate.y;
        int w = bb.getWidth();
        int h = bb.getHeight();

        int offsetX = (left < dst.left) ? dst.left - left : 0;
        int offsetY = (top < dst.top) ? dst.top - top : 0;

        left += offsetX;
        w -= offsetX;
        if (w + left - dst.left > dst.width()) {
            w = dst.width() - left + dst.left;
        }

        top += offsetY;
        h -= offsetY;
        if (h + top - dst.top > dst.height()) {
            h = dst.height() - top + dst.top;
        }

        bb =  Bitmap.createBitmap(bb, offsetX, offsetY, w, h);

        //draw
        c.drawBitmap(bb, left, top, new Paint());
    }
}
