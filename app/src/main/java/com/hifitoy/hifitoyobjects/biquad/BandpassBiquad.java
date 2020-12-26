/*
 *   BandpassBiquad.java
 *
 *   Created by Artem Khlyupin on 21/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;

import android.support.annotation.NonNull;

import com.hifitoy.hifitoycontrol.HiFiToyControl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class BandpassBiquad extends PassBiquad {
    public BandpassBiquad(Biquad b) {
        super(b);
    }

    public BandpassBiquad(byte addr, byte bindAddr) {
        super(addr, bindAddr);
    }

    public BandpassBiquad(byte addr) {
        super(addr);
    }

    public BandpassBiquad() {
        super();
    }

    @NonNull
    @Override
    public BandpassBiquad clone() throws CloneNotSupportedException {
        return (BandpassBiquad) super.clone();
    }

    //setters getters
    @Override
    public byte getType() {
        return Type.BIQUAD_BANDPASS;
    }

    @Override
    public void setCoefs(float b0, float b1, float b2, float a1, float a2) {
        super.setCoefs(b0, b1, b2, a1, a2);

        float w0 = (float)(Math.acos(a1 / 2 * (1 + b0 / (1 - b0))));
        freq = (short)Math.round(w0 * (float)FS / (2 * Math.PI));
        //TODO set import bandwidth
    }

    @Override
    protected void update() {
        float w0 = 2 * (float)Math.PI * freq / FS;
        final float bandwidth = 1.41f;
        float a0;

        float s = (float)Math.sin(w0), c = (float)Math.cos(w0);

        float alpha = (float)( s * Math.sinh( 0.3465735902 * bandwidth * w0 / s) );

        a0 =   1 + alpha;
        a1 =   2 * c / a0;
        a2 =   (1 - alpha) / (-a0);
        b0 =   alpha / a0;
        b1 =   0;
        b2 =  -alpha / a0;
    }

}
