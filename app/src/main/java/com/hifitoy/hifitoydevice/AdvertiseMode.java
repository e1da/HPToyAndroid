package com.hifitoy.hifitoydevice;

import com.hifitoy.hifitoycontrol.CommonCommand;
import com.hifitoy.hifitoycontrol.HiFiToyControl;

import java.io.Serializable;

public class AdvertiseMode implements Serializable {
    public static final byte ALWAYS_ENABLED = 0;
    public static final byte AFTER_1MIN_DISABLED = 1;
    private byte mode;

    public AdvertiseMode() {
        setDefault();
    }

    public void setDefault() {
        setMode(ALWAYS_ENABLED);
    }
    public void setMode(byte mode) {
        if (mode < ALWAYS_ENABLED) mode = ALWAYS_ENABLED;
        if (mode > AFTER_1MIN_DISABLED) mode = AFTER_1MIN_DISABLED;

        this.mode = mode;
    }

    public void setModeWithWriteToDsp(byte mode) {
        setMode(mode);
        sendToDsp();
    }

    public byte getMode() {
        return mode;
    }

    public void sendToDsp() {
        byte[] d = {CommonCommand.SET_ADVERTISE_MODE, mode, 0, 0, 0};
        HiFiToyControl.getInstance().sendDataToDsp(d, true);
    }

    public void readFromDsp() {
        byte[] d = {CommonCommand.GET_ADVERTISE_MODE, 0, 0, 0, 0};
        HiFiToyControl.getInstance().sendDataToDsp(d, true);
    }
}
