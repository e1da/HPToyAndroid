/*
 *   PassFilter.java
 *
 *   Created by Artem Khlyupin on 10/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

import com.hifitoy.hifitoycontrol.HiFiToyControl;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_OFF;

public class PassFilter implements Cloneable, Serializable {
    public final static byte PASSFILTER_ORDER_0      = 0;
    public final static byte PASSFILTER_ORDER_2      = 1;
    public final static byte PASSFILTER_ORDER_4      = 2;
    public final static byte PASSFILTER_ORDER_8      = 3;
    public final static byte PASSFILTER_ORDER_UNK    = 4;

    private List<Biquad> biquads;

    public PassFilter(List<Biquad> biquads, byte type) {
        short freq;
        type = (type == BIQUAD_LOWPASS) ? BIQUAD_LOWPASS : BIQUAD_HIGHPASS;

        if ((biquads != null) && (biquads.size() > 0)) {
            freq = biquads.get(0).getParams().getFreq();

            Biquad b;
            switch (biquads.size()) {
                case 1:
                    b = biquads.get(0);
                    b.getParams().setTypeValue(type);
                    b.getParams().setFreq(freq);
                    b.getParams().setQFac(0.71f);
                    break;

                case 2:
                    b = biquads.get(0);
                    b.getParams().setTypeValue(type);
                    b.getParams().setFreq(freq);
                    b.getParams().setQFac(0.54f);

                    b = biquads.get(1);
                    b.getParams().setTypeValue(type);
                    b.getParams().setFreq(freq);
                    b.getParams().setQFac(1.31f);
                    break;

                case 4:
                    b = biquads.get(0);
                    b.getParams().setTypeValue(type);
                    b.getParams().setFreq(freq);
                    b.getParams().setQFac(0.90f);

                    b = biquads.get(1);
                    b.getParams().setTypeValue(type);
                    b.getParams().setFreq(freq);
                    b.getParams().setQFac(2.65f);

                    b = biquads.get(2);
                    b.getParams().setTypeValue(type);
                    b.getParams().setFreq(freq);
                    b.getParams().setQFac(0.51f);

                    b = biquads.get(3);
                    b.getParams().setTypeValue(type);
                    b.getParams().setFreq(freq);
                    b.getParams().setQFac(0.60f);
                    break;

                default:
                    biquads = null;
            }
        } else {
            freq = (type == BIQUAD_LOWPASS) ? (short)20000 : (short)20;
        }

        this.biquads = biquads;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassFilter that = (PassFilter) o;
        return Objects.equals(biquads, that.biquads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(biquads);
    }

    @Override
    public PassFilter clone() throws CloneNotSupportedException{
        PassFilter p = (PassFilter) super.clone();
        p.biquads = new ArrayList<>();

        for (int i = 0; i < biquads.size(); i++) {
            p.biquads.add(biquads.get(i).clone());
        }
        return p;
    }

    // getter/setter
    public byte getOrder() {
        if (biquads != null) {
            switch (biquads.size()) {
                case 0:
                    return PASSFILTER_ORDER_0;
                case 1:
                    return PASSFILTER_ORDER_2;
                case 2:
                    return PASSFILTER_ORDER_4;
                case 4:
                    return PASSFILTER_ORDER_8;
                default:
                    return PASSFILTER_ORDER_UNK;
            }
        }
        return PASSFILTER_ORDER_0;
    }
    public void setType(byte type) {
        type = (type == BIQUAD_LOWPASS) ? BIQUAD_LOWPASS : BIQUAD_HIGHPASS;

        if (biquads != null) {
            for (int i = 0; i < biquads.size(); i++) {
                biquads.get(i).getParams().setTypeValue(type);
            }
        }
    }
    public byte getType() {
        if ((biquads != null) && (biquads.size() > 0)) {
            return biquads.get(0).getParams().getTypeValue();
        }
        return BIQUAD_OFF;
    }
    public void setFreq(short freq) {
        if (biquads != null) {
            for (int i = 0; i < biquads.size(); i++) {
                biquads.get(i).getParams().setFreq(freq);
            }
        }
    }
    public short getFreq() {
        if ((biquads != null) && (biquads.size() > 0)) {
            return biquads.get(0).getParams().getFreq();
        }
        return 0;
    }

    //info string
    public String getInfo() {
        int[] dbOnOctave = new int[]{0, 12, 24, 48};// db/oct
        int index = getOrder();
        if (index < 4) {
            return String.format(Locale.getDefault(), "%ddb/oct; Freq:%dHz", dbOnOctave[index], getFreq());
        }
        return String.format(Locale.getDefault(), "???db/oct; Freq:%dHz", getFreq());
    }

    //send to dsp
    public void sendToPeripheral(boolean response) {
        if ( (biquads == null) || (biquads.size() == 0) ) return;

        ByteBuffer b = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < 4; i++) {
            if (i < biquads.size()) {
                b.put(biquads.get(i).getAddress());
                b.put(biquads.get(i).getAddress1());
            } else {
                b.put((byte)0);
                b.put((byte)0);
            }
        }
        b.put(getOrder());
        b.put(getType());
        b.putShort(getFreq());
        byte nc = 0;
        b.put(nc);

        HiFiToyControl.getInstance().sendDataToDsp(b, response);
    }
}
