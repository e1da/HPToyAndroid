/*
 *   OutputMode.java
 *
 *   Created by Artem Khlyupin on 09/02/21
 *   Copyright © 2021 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoydevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hifitoy.ApplicationContext;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.CommonCommand;
import com.hifitoy.hifitoycontrol.HiFiToyControl;

import java.io.Serializable;

import static com.hifitoy.ApplicationContext.EXTRA_DATA;

public class OutputMode implements Serializable {
    private static final String TAG = "HiFiToy";

    private static final short GAIN_CH3_OFFSET          = 0x0A;
    private static final short OUTPUT_TYPE_OFFSET       = 0x1F;

    public static final byte BALANCE_OUT_MODE           = 0;
    public static final byte UNBALANCE_OUT_MODE         = 1;
    public static final byte UNBALANCE_BOOST_OUT_MODE   = 2;

    private byte value;

    private boolean hwSupported;


    public OutputMode() {
        setDefault();
    }

    public void setDefault() {
        setValue(UNBALANCE_BOOST_OUT_MODE);
        setHwSupported(true);
    }
    public void setValue(byte value) {
        if (value < BALANCE_OUT_MODE) value = BALANCE_OUT_MODE;
        if (value > UNBALANCE_BOOST_OUT_MODE) value = UNBALANCE_BOOST_OUT_MODE;

        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public void setHwSupported(boolean hwSupported) {
        this.hwSupported = hwSupported;
    }
    public boolean isHwSupported() {
        return hwSupported;
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

    private short offset;
    private short boost;

    public void readFromDsp() {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        Context c = ApplicationContext.getInstance().getContext();
        c.registerReceiver(broadcastReceiver,
                new IntentFilter(HiFiToyControl.DID_GET_PARAM_DATA));

        offset = GAIN_CH3_OFFSET;
        //send ble command
        HiFiToyControl.getInstance().getDspDataWithOffset(offset);
    }

    private void didGet20Bytes(byte data[]) {
        if (offset == GAIN_CH3_OFFSET) {
            boost = (short)(data[0] + (data[1] << 8));

            offset += 20;
            HiFiToyControl.getInstance().getDspDataWithOffset(offset);

        } else {
            Context c = ApplicationContext.getInstance().getContext();
            c.unregisterReceiver(broadcastReceiver);

            byte bal = data[OUTPUT_TYPE_OFFSET - GAIN_CH3_OFFSET - 20];
            Log.d(TAG, "Boost=" + boost + " Unbalance=" + bal);

            //update value
            value = bal;
            if ((boost != 0) && (value > BALANCE_OUT_MODE)) {
                value = UNBALANCE_BOOST_OUT_MODE;
            }
            Log.d(TAG, toString());

            ApplicationContext.getInstance().setupOutlets();
        }
    }

    //GET_OUTPUT_MODE cmd uses only for check PDV2.1 or PDV2 classic
    //this cmd return incorrect value. bug on hw side.
    public void isSettingsAvailable() {
        byte[] d = {CommonCommand.GET_OUTPUT_MODE, 0, 0, 0, 0};
        HiFiToyControl.getInstance().sendDataToDsp(d, true);
    }

    private transient final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (HiFiToyControl.DID_GET_PARAM_DATA.equals(action)) {
                byte[] data = intent.getByteArrayExtra(EXTRA_DATA);
                didGet20Bytes(data);
            }
        }
    };

    @NonNull
    @Override
    public String toString() {
        switch (value) {
            case BALANCE_OUT_MODE:
                return "Output mode = Balance";
            case UNBALANCE_OUT_MODE:
                return "Output mode = Unbalance";
            case UNBALANCE_BOOST_OUT_MODE:
                return "Output mode = Boost unbalance";
            default:
                return "Output mode = Error";
        }
    }
}
