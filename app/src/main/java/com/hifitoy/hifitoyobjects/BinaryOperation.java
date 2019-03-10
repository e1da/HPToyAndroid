/*
 *   BinaryOperation.java
 *
 *   Created by Artem Khlyupin on 10/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

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
}
