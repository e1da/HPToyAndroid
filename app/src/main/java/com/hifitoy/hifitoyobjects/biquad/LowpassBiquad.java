/*
 *   LowpassBiquad.java
 *
 *   Created by Artem Khlyupin on 19/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;

import android.support.annotation.NonNull;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.FloatUtility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class LowpassBiquad extends PassBiquad {
    public LowpassBiquad(Biquad b) {
        super(b);
        setCoefs(b.getB0(), b.getB1(), b.getB2(), b.getA1(), b.getA2());
    }

    public LowpassBiquad(byte addr, byte bindAddr) {
        super(addr, bindAddr);
    }

    public LowpassBiquad(byte addr) {
        super(addr);
    }

    public LowpassBiquad() {
        super();
    }

    @NonNull
    @Override
    public LowpassBiquad clone() throws CloneNotSupportedException{
        //TODO : check this method
        LowpassBiquad lp = (LowpassBiquad) super.clone();
        return lp;
    }

    @Override
    public boolean equals(Object o) {
        //TODO : check this method
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LowpassBiquad lp = (LowpassBiquad) o;
        return (freq == lp.freq) &&
                FloatUtility.isFloatDiffLessThan(lp.q, q, 0.02f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(freq, q);
    }

    //setters getters
    @Override
    public byte getType() {
        return Type.BIQUAD_LOWPASS;
    }

    @Override
    public void setCoefs(float b0, float b1, float b2, float a1, float a2) {
        super.setCoefs(b0, b1, b2, a1, a2);

        float arg = 2 * b1 / a1 + 1;
        if ((arg < 1.0f) && (arg > -1.0f)) return;

        float w0 = (float)Math.acos(1.0f / arg);
        freq = (short)Math.round(w0 * FS / (2 * Math.PI));
        q = (float)(Math.sin(w0) * a1 / (2 * (2 * Math.cos(w0) - a1)));
    }

    @Override
    protected void update() {
        float w0 = 2 * (float)Math.PI * freq / FS;
        float alpha, a0;

        float s = (float)Math.sin(w0), c = (float)Math.cos(w0);

        alpha = s / (2 * q);
        a0 =  1 + alpha;
        a1 =  2 * c / (a0);
        a2 =  (1 - alpha) / (-a0);
        b0 =  (1 - c) / (2 * a0);
        b1 =  (1 - c) / a0;
        b2 =  (1 - c) / (2 * a0);
    }


}
