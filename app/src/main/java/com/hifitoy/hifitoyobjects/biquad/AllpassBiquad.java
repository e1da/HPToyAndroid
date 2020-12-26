/*
 *   Allpass1stBiquad.java
 *
 *   Created by Artem Khlyupin on 20/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;

import android.support.annotation.NonNull;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class AllpassBiquad extends PassBiquad {

    public AllpassBiquad(Biquad b) {
        super(b);
    }

    public AllpassBiquad(byte addr, byte bindAddr) {
        super(addr, bindAddr);
    }

    public AllpassBiquad(byte addr) {
        super(addr);
    }

    public AllpassBiquad() {
        super();
    }

    @NonNull
    @Override
    public AllpassBiquad clone() throws CloneNotSupportedException{
        //TODO : check this method
        AllpassBiquad ap = (AllpassBiquad) super.clone();
        return ap;
    }

    @Override
    public boolean equals(Object o) {
        //TODO : check this method
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllpassBiquad ap = (AllpassBiquad) o;
        return (freq == ap.freq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(freq);
    }

    //setters getters
    @Override
    public byte getType() {
        return Type.BIQUAD_ALLPASS;
    }

    @Override
    public byte getOrder() {
        return Order.BIQUAD_ORDER_1;
    }

    @Override
    public void setCoefs(float b0, float b1, float b2, float a1, float a2) {
        super.setCoefs(b0, b1, b2, a1, a2);

        if (a1 > 0) {
            float w0 = (float)(-Math.log10(a1) / Math.log10(2.7));
            freq = (short)Math.round(w0 * FS / (2 * Math.PI));
        }

    }

    @Override
    protected void update() {
        float w0 = 2 * (float)Math.PI * freq / FS;

        a1 = (float)Math.pow(2.7, -w0);
        a2 = 0;
        b0 = -a1;
        b1 = 1.0f;
        b2 = 0;
    }
}
