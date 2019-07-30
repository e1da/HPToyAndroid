package com.hifitoy.activities.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

public class FiltersBackground {
    private static String TAG = "HiFiToy";
    private static FiltersBackground instance;

    private Bitmap  bitmap;
    private Rect    src;
    private boolean scaleTypeX;
    private PointF  scale;
    private PointF  translate;


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

        src = (bitmap != null) ? new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()) : null;
        scaleTypeX = true;
        scale = new PointF(1.0f, 1.0f);
        translate = new PointF(0.0f, 0.0f);
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

    public void setTranslate(PointF deltaTranslate) {
        if (bitmap == null) return;

        translate.x += deltaTranslate.x / 4;
        translate.y += deltaTranslate.y / 4;

    }

    public void drawInRect(Canvas c, Rect dst) {
        if ((bitmap == null) || (src == null) || (dst == null)) return;

        //c.drawBitmap(bitmap, src, dst, new Paint());

        Bitmap b = scaleToRect(bitmap, dst);
        if (translate.x > 0) {
            if (translate.y > 0) {
                c.drawBitmap(b, dst.left + translate.x, dst.top + translate.y, new Paint());
            } else {
                c.drawBitmap(b, dst.left + translate.x, dst.top, new Paint());
            }

        } else {

            if (translate.y > 0) {
                c.drawBitmap(b, dst.left, dst.top + translate.y, new Paint());
            } else {
                c.drawBitmap(b, dst.left, dst.top, new Paint());
            }
        }
    }

    private Bitmap scaleToRect(Bitmap b, Rect dstView) {

        float ratioX = (float)dstView.width() / b.getWidth();
        float ratioY = (float)dstView.height() / b.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(ratioX * scale.x, ratioY * scale.y);
        Bitmap bb =  Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);

        float offsetX = 0;
        float offsetY = 0;

        float width = Math.min(bb.getWidth(), Math.max(dstView.width() - translate.x, 1));
        float height = Math.min(bb.getHeight(), Math.max(dstView.height() - translate.y, 1));

        if (translate.x < 0) {
            offsetX = Math.min(Math.abs(translate.x) * scale.x, bb.getWidth() - 1);
            width = Math.max(bb.getWidth() - offsetX, 1);
            if (width > dstView.width()) width = dstView.width();

        }
        if (translate.y < 0) {
            offsetY = Math.min(Math.abs(translate.y) * scale.y, bb.getHeight() - 1);
            height = Math.max(bb.getHeight() - offsetY, 1);
            if (height > dstView.height()) height = dstView.height();

        }

        return Bitmap.createBitmap(bb, (int)offsetX, (int)offsetY, (int)width, (int)height);
    }
}
