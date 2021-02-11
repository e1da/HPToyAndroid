/*
 *   OutputMode.java
 *
 *   Created by Artem Khlyupin on 09/02/21
 *   Copyright © 2021 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoydevice;

import com.hifitoy.hifitoycontrol.CommonCommand;
import com.hifitoy.hifitoycontrol.HiFiToyControl;

import java.io.Serializable;

public class OutputMode implements Serializable {
    public static final byte BALANCE_OUT_MODE           = 0;
    public static final byte UNBALANCE_OUT_MODE         = 1;
    public static final byte UNBALANCE_BOOST_OUT_MODE   = 2;

    private byte value;

    public OutputMode() {
        setDefault();
    }

    public void setDefault() {
        setValue(UNBALANCE_BOOST_OUT_MODE);
    }
    public void setValue(byte value) {
        if (value < BALANCE_OUT_MODE) value = BALANCE_OUT_MODE;
        if (value > UNBALANCE_BOOST_OUT_MODE) value = UNBALANCE_BOOST_OUT_MODE;

        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public void setBoost(short boostValue) {
        if (value == BALANCE_OUT_MODE) return;

        if (boostValue == 0) {
            value = UNBALANCE_OUT_MODE;
        } else {
            value = UNBALANCE_BOOST_OUT_MODE;
        }
    }

    public void sendToDsp() {
        if (value == BALANCE_OUT_MODE) {
            byte[] d = {CommonCommand.SET_OUTPUT_MODE, BALANCE_OUT_MODE, 0, 0, 0};
            HiFiToyControl.getInstance().sendDataToDsp(d, true);

        } else {
            byte[] d = {CommonCommand.SET_OUTPUT_MODE, UNBALANCE_OUT_MODE, 0, 0, 0};
            HiFiToyControl.getInstance().sendDataToDsp(d, true);

        }


        if (value == UNBALANCE_BOOST_OUT_MODE) {
            byte[] d1 = {CommonCommand.SET_TAS5558_CH3_MIXER, 0, 0x40, 0, 0};
            HiFiToyControl.getInstance().sendDataToDsp(d1, true);

        } else {
            byte[] d1 = {CommonCommand.SET_TAS5558_CH3_MIXER, 0, 0, 0, 0};
            HiFiToyControl.getInstance().sendDataToDsp(d1, true);

        }
    }

    public void readFromDsp() {
        byte[] d = {CommonCommand.GET_OUTPUT_MODE, 0, 0, 0, 0};
        HiFiToyControl.getInstance().sendDataToDsp(d, true);

        byte[] d1 = {CommonCommand.GET_TAS5558_CH3_MIXER, 0, 0, 0, 0};
        HiFiToyControl.getInstance().sendDataToDsp(d1, true);
    }
}
