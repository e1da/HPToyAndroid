/*
 *   BlePacket.java
 *
 *   Created by Artem Khlyupin on 13/11/2018.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.ble;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BlePacket {
    private static final String TAG = "HiFiToy";

    private byte[] data;
    private boolean response;

    public BlePacket(byte[] value, boolean response){
        this.data = value;
        this.response = response;
    }
    public BlePacket(ByteBuffer value, boolean response){
        this(value.array(), response);
    }
    public BlePacket(byte[] value, int length, boolean response){
        this(Arrays.copyOf(value, length), response);
    }
    public BlePacket(ByteBuffer value, int length, boolean response){
        this(value.array(), length, response);
    }

    public byte[] getData() {
        return data;
    }

    public boolean getResponse() {
        return response;
    }

    public void description(){
        String dataStr = "";//new String();


        for (int i = 0; i < data.length; i++){
            dataStr += String.format("%x ", data[i]);
        }
        Log.d(TAG, dataStr);
    }
}
