/*
 *   Number523.java
 *
 *   Created by Artem Khlyupin on 06/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoynumbers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Number523 {

    private static float getMaxFloatFor523() {
        ByteBuffer b = ByteBuffer.allocate(4).putFloat(16.0f);

        int temp = b.getInt(0) - 1;
        b.putInt(0, temp);

        return b.getFloat(0);
    }

    private static float getMinFloatFor523() {
        return -16.0f;
    }

    public static float checkRange(float num) {
        float max = getMaxFloatFor523();
        float min = getMinFloatFor523();

        if (num > max) num = max;
        if (num < min) num = min;

        return num;
    }

    public static ByteBuffer get523LittleEnd(float num){
        num = checkRange(num);
        int n = (int)(num * 0x800000);
        return (ByteBuffer)ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(n).position(0);
    }

    public static ByteBuffer get523BigEnd(float num){
        num = checkRange(num);
        int n = (int)(num * 0x800000);
        return (ByteBuffer) ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(n).position(0);
    }

    public static float toFloat(ByteBuffer buf) {
        if (buf.array().length == 4) {
            byte head = (buf.order() == ByteOrder.LITTLE_ENDIAN) ? buf.get(3) : buf.get(0);

            if ((head & 0x80) != 0) {
                head |= 0xF0;

                if (buf.order() == ByteOrder.LITTLE_ENDIAN) {
                    buf.put(3, head);
                } else {
                    buf.put(0, head);
                }
            }

            int num = buf.getInt(0);
            return (float)num / 0x800000;

        }

        return 0.0f;
    }

    /*Number523_t reverseNumber523(Number523_t number523){
        Number523_t result;
        uint8_t * pSrc = (uint8_t *)&number523;
        uint8_t * pDest = (uint8_t *)&result;

        pDest[0] = pSrc[3];
        pDest[1] = pSrc[2];
        pDest[2] = pSrc[1];
        pDest[3] = pSrc[0];

        return result;
    }*/

}
