package com.hifitoy.hifitoynumbers;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtility {
    private static final String TAG = "HiFiToy";

    public static String toString(byte b) {
        ByteBuffer bb = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
        bb.put(b);
        bb.put((byte)0);
        short s = bb.getShort(0);

        return Short.toString(s);
    }

    public static byte parse(String s) {
        try {
            short num = Short.parseShort(s);
            if ( (num >= 0) && (num < 256) ) {
                return (byte)num;
            }
        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        }

        return 0;
    }
}
