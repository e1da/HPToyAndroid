/*
 *   Number923.java
 *
 *   Created by Artem Khlyupin on 13/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoynumbers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Number923 {
    public static ByteBuffer get923LittleEnd(float num) {
        int n = (int)(num * 0x800000);
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(n);
    }
    public static ByteBuffer get923BigEnd(float num) {
        int n = (int)(num * 0x800000);
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(n);
    }

    public static float toFloat(ByteBuffer buf){
        int num = buf.getInt(0);
        return (float)num / 0x800000;
    }
}
