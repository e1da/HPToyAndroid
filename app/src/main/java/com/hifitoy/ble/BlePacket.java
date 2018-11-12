package com.hifitoy.ble;

import android.util.Log;

public class BlePacket {
    final static String TAG = "BlePacket";

    public byte[] data;
    public boolean response;

    public BlePacket(byte[] value, boolean response){
        this.data = value;
        this.response = response;
    }

    public void description(){
        String dataStr = "";//new String();


        for (int i = 0; i < data.length; i++){
            dataStr += String.format("%x ", data[i]);
        }
        Log.d(TAG, dataStr);
    }
}
