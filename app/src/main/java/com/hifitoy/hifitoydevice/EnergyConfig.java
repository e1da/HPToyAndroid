/*
 *   EnergyConfig.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoydevice;

import com.hifitoy.hifitoycontrol.CommonCommand;
import com.hifitoy.hifitoycontrol.HiFiToyControl;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EnergyConfig implements Serializable {
    private final float MAX_THRESHOLD_DB = 0.0f;
    private final float MIN_THRESHOLD_DB = -120.0f;

    private float   highThresholdDb;
    private float   lowThresholdDb;
    private short   auxTimeout120ms;
    private short   usbTimeout120ms;

    public EnergyConfig() {
        setDefault();
    }

    public void setDefault() {
        highThresholdDb = 0;    // 0
        lowThresholdDb  = -55;  // -55
        auxTimeout120ms = 2500; // 2500 * 120ms = 300s = 5min
        usbTimeout120ms = 0;    // not used
    }

    public ByteBuffer getBinary() {
        ByteBuffer b = ByteBuffer.allocate(12);
        b.order(ByteOrder.LITTLE_ENDIAN);

        b.putFloat(highThresholdDb);
        b.putFloat(lowThresholdDb);
        b.putShort(auxTimeout120ms);
        b.putShort(usbTimeout120ms);

        return b;
    }

    public void parseBinary(ByteBuffer b) {
        highThresholdDb = b.getFloat();
        lowThresholdDb = b.getFloat();
        auxTimeout120ms = b.getShort();
        usbTimeout120ms = b.getShort();
    }

    public void setValues(byte[] data) {
        if (data.length == 12) {
            ByteBuffer b = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

            highThresholdDb = b.getFloat();
            lowThresholdDb = b.getFloat();
            auxTimeout120ms = b.getShort();
            usbTimeout120ms = b.getShort();
        }
    }

    public void setLowThresholdDb(float lowThresholdDb) {
        if (lowThresholdDb > MAX_THRESHOLD_DB) lowThresholdDb = MAX_THRESHOLD_DB;
        if (lowThresholdDb < MIN_THRESHOLD_DB) lowThresholdDb = MIN_THRESHOLD_DB;

        this.lowThresholdDb = lowThresholdDb;
    }
    public float getLowThresholdDb() {
        return lowThresholdDb;
    }
    //percent = 0.0 .. 1.0
    public void setLowThresholdDbPercent(float percent) {
        if (percent > 1.0f) percent = 1.0f;
        if (percent < 0.0f) percent = 0.0f;

        this.lowThresholdDb = (MAX_THRESHOLD_DB - MIN_THRESHOLD_DB) * percent + MIN_THRESHOLD_DB;
    }
    public float getLowThresholdDbPercent() {
        return (lowThresholdDb - MIN_THRESHOLD_DB) / (MAX_THRESHOLD_DB - MIN_THRESHOLD_DB);
    }

    public void setHighThresholdDb(float highThresholdDb) {
        if (highThresholdDb > MAX_THRESHOLD_DB) highThresholdDb = MAX_THRESHOLD_DB;
        if (highThresholdDb < MIN_THRESHOLD_DB) highThresholdDb = MIN_THRESHOLD_DB;

        this.highThresholdDb = highThresholdDb;
    }
    public float getHighThresholdDb() {
        return highThresholdDb;
    }
    public void setHighThresholdDbPercent(float percent) {
        if (percent > 1.0f) percent = 1.0f;
        if (percent < 0.0f) percent = 0.0f;

        this.highThresholdDb = (MAX_THRESHOLD_DB - MIN_THRESHOLD_DB) * percent + MIN_THRESHOLD_DB;
    }
    public float getHighThresholdDbPercent() {
        return (highThresholdDb - MIN_THRESHOLD_DB) / (MAX_THRESHOLD_DB - MIN_THRESHOLD_DB);
    }

    public void sendToDsp(){
        HiFiToyControl.getInstance().sendDataToDsp(getBinary(), true);
    }

    public void readFromDsp() {
        byte[] d = {CommonCommand.GET_ENERGY_CONFIG, 0, 0, 0, 0};
        HiFiToyControl.getInstance().sendDataToDsp(d, true);
    }
}
