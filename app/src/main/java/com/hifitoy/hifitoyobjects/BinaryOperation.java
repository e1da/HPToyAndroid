/*
 *   BinaryOperation.java
 *
 *   Created by Artem Khlyupin on 10/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class BinaryOperation {
    public static byte[] concatData(byte[] data, byte[] appendData){
        byte[] concatData = new byte[data.length + appendData.length];

        System.arraycopy(data, 0, concatData, 0, data.length);
        System.arraycopy(appendData, 0, concatData, data.length, appendData.length);

        return concatData;
    }

    public static float[] concatData(float[] data, float[] appendData){
        float[] concatData = new float[data.length + appendData.length];

        System.arraycopy(data, 0, concatData, 0, data.length);
        System.arraycopy(appendData, 0, concatData, data.length, appendData.length);

        return concatData;
    }

    public static ByteBuffer concatData(ByteBuffer data, ByteBuffer appendData){
        data.position(0);
        appendData.position(0);

        ByteBuffer concatData = ByteBuffer.allocate(data.capacity() + appendData.capacity());
        concatData.put(data);
        concatData.put(appendData);

        return concatData;
    }

    public static ByteBuffer concatData(ByteBuffer data, short shortData){
        ByteBuffer concatData = ByteBuffer.allocate(data.capacity() + 2).order(ByteOrder.LITTLE_ENDIAN);
        concatData.put(data);
        concatData.putShort(shortData);

        return concatData;
    }

    public static ByteBuffer concatData(ByteBuffer data, byte byteData){
        ByteBuffer concatData = ByteBuffer.allocate(data.capacity() + 1);
        concatData.put(data);
        concatData.put(byteData);

        return concatData;
    }

    public static byte[] getBinary(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return null;

        byte[] data = new byte[0];

        for (int i = 0; i < dataBufs.size(); i++) {
            data = concatData(data, dataBufs.get(i).getBinary().array());
        }

        return data;
    }

    public static ByteBuffer copyOfRange(ByteBuffer buf, int from, int to) {
        if (from > buf.capacity())  from = buf.capacity() - 1;
        if (to > buf.capacity())    to = buf.capacity();

        return ByteBuffer.wrap(Arrays.copyOfRange(buf.array(), from, to));
    }
}
