package com.hifitoy.activities.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

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

        if (scaleTypeX) {
            float sx = scale.x * deltaScale;
            if ((sx > 16) || (sx < 0.0625f)) return;

            scale.x = sx;
        } else {
            float sy = scale.y * deltaScale;
            if ((sy > 16) || (sy < 0.0625f)) return;

            scale.y = sy;
        }

        updateRect();
    }

    public void setTranslate(PointF deltaTranslate) {
        if (bitmap == null) return;

        translate.x += deltaTranslate.x;
        translate.y += deltaTranslate.y;

        updateRect();
    }

    private void updateRect() {
        if (bitmap == null) return;

        int padX = (int)((bitmap.getWidth() - bitmap.getWidth() / scale.x) / 2);
        int padY = (int)((bitmap.getHeight() - bitmap.getHeight() / scale.y) / 2);

        src.left    = (int)translate.x;
        src.right   = bitmap.getWidth() - 2 * padX + (int)translate.x;
        src.top     = (int)translate.y;
        src.bottom  = bitmap.getHeight() - 2 * padY + (int)translate.y;
    }

    public void drawInRect(Canvas c, Rect dst) {
        if ((bitmap == null) || (src == null) || (dst == null)) return;

        c.drawBitmap(bitmap, src, dst, new Paint());
    }
}
