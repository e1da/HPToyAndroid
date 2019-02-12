/*
 *   EnergyConfig.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoydevice;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EnergyConfig {

    private float   highThresholdDb;
    private float   lowThresholdDb;
    private int     auxTimeout120ms;
    private int     usbTimeout120ms;

    private final short offset = 0x0C;

    public EnergyConfig() {
        setDefault();
    }

    public void setDefault() {
        highThresholdDb = 0;    // 0
        lowThresholdDb  = -55;  // -55
        auxTimeout120ms = 2500; // 2500 * 120ms = 300s = 5min
        usbTimeout120ms = 0;    // not used
    }

    public byte[] getBinary() {
        ByteBuffer b = ByteBuffer.allocate(12);

        b.putFloat(highThresholdDb);
        b.putFloat(lowThresholdDb);
        b.putInt(auxTimeout120ms);
        b.putInt(10, usbTimeout120ms);

        return b.array();
    }

    public void setValues(byte[] data) {
        if (data.length == 12) {
            highThresholdDb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            lowThresholdDb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            auxTimeout120ms = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getShort();
            usbTimeout120ms = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getShort();
        }
    }

    public void sendToDsp(){
        HiFiToyControl.getInstance().sendDataToDsp(getBinary(), true);
    }

    public void readFromDsp() {
        HiFiToyControl.getInstance().getDspDataWithOffset(offset);
    }
}
