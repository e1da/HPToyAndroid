/*
 *   FloatUtility.java
 *
 *   Created by Artem Khlyupin on 07/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoynumbers;

import java.nio.ByteBuffer;

public class FloatUtility {

    public static boolean isFloatEqualWithAccuracy(float arg0, float arg1, int accuracy) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putFloat(arg0);
        int arg0Int = b.getInt(0);

        b.putFloat(0, arg1);
        int arg1Int = b.getInt(0);

        if (arg0Int < 0) arg0Int = 0x80000000 - arg0Int; //float mantis to 2`s complement
        if (arg1Int < 0) arg1Int = 0x80000000 - arg1Int; //float mantis to 2`s complement

        int diff = (arg0Int > arg1Int) ? (arg0Int - arg1Int) : (arg1Int - arg0Int);

        return (diff < accuracy);
    }

    public static boolean isFloatNull(float f) {
        return isFloatEqualWithAccuracy(f, 0.0f, 16);
    }

    public static boolean isFloatDiffLessThan(float f0, float f1, float maxDiff) {
        return Math.abs(f0 - f1) < maxDiff;
    }

    //perc >= 0.0
    public static boolean isFloatDiffLessThanPerc(float f0, float f1, float perc) {
        if (perc < 0.0f) perc = 0.0f;
        return Math.abs(f0 - f1) / Math.max(Math.abs(f0), Math.abs(f1)) < perc / 100.0f;
    }
}
