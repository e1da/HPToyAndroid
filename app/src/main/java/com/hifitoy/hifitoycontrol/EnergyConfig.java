/*
 *   EnergyConfig.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoycontrol;

import java.nio.ByteBuffer;

public class EnergyConfig {
    private static EnergyConfig instance;

    private float   highThresholdDb;
    private float   lowThresholdDb;
    private int     auxTimeout120ms;
    private int     usbTimeout120ms;

    private final int offset = 0x0C;

    public static synchronized EnergyConfig getInstance() {
        if (instance == null){
            instance = new EnergyConfig();
        }
        return instance;
    }

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

    public void sendToDsp(){
        HiFiToyControl.getInstance().sendDataToDsp(getBinary(), true);
    }

    public void readFromDsp() {
        HiFiToyControl.getInstance().getDspDataWithOffset(offset);
    }
}
