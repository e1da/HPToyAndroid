/*
 *   AudioSource.java
 *
 *   Created by Artem Khlyupin on 21/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import com.hifitoy.hifitoycontrol.CommonCommand;
import com.hifitoy.hifitoycontrol.HiFiToyControl;

import java.io.Serializable;

public class AudioSource implements Serializable {
    public static final byte SPDIF_SOURCE = 0;
    public static final byte USB_SOURCE = 1;
    public static final byte BT_SOURCE = 2;

    private byte source;

    public AudioSource() {
        setDefault();
    }

    public void setDefault() {
        setSource(USB_SOURCE);
    }
    public void setSource(byte source) {
        if (source < SPDIF_SOURCE) source = SPDIF_SOURCE;
        if (source > BT_SOURCE) source = BT_SOURCE;

        this.source = source;
    }

    public void setSourceWithWriteToDsp(byte source) {
        setSource(source);
        sendToDsp();
    }

    public byte getSource() {
        return source;
    }

    public void sendToDsp() {
        byte[] d = {CommonCommand.SET_AUDIO_SOURCE, source, 0, 0, 0};
        HiFiToyControl.getInstance().sendDataToDsp(d, true);
    }

    public void readFromDsp() {
        byte[] d = {CommonCommand.GET_AUDIO_SOURCE, 0, 0, 0, 0};
        HiFiToyControl.getInstance().sendDataToDsp(d, true);
    }
}
