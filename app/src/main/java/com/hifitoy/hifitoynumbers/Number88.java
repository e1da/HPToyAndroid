/*
 *   Number88.java
 *
 *   Created by Artem Khlyupin on 13/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoynumbers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Number88 {
    public static ByteBuffer get88LittleEnd(float num) {
        short n = (short)(num * 0x100);
        return (ByteBuffer) ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(n).position(0);
    }
    public static ByteBuffer get88BigEnd(float num) {
        short n = (short)(num * 0x100);
        return (ByteBuffer) ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort(n).position(0);
    }

    public static float toFloat(ByteBuffer buf){
        short num = buf.getShort(0);
        return (float)num / 0x100;
    }
}
