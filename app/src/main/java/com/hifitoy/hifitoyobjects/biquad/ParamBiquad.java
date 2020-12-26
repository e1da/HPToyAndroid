/*
 *   ParamBiquad.java
 *
 *   Created by Artem Khlyupin on 20/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;

import android.support.annotation.NonNull;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.FloatUtility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

public class ParamBiquad extends Biquad implements IFreq {
    //macro param
    private short freq;
    private float q;
    private float dbVolume;

    //border property
    private short maxFreq;
    private short minFreq;
    private float maxQ;
    private float minQ;
    private float maxDbVolume;
    private float minDbVolume;

    public ParamBiquad(Biquad b) {
        super(b);
    }

    public ParamBiquad(byte addr, byte bindAddr) {
        super(addr, bindAddr);
        init();
    }

    public ParamBiquad(byte addr) {
        super(addr, (byte)0);
    }

    public ParamBiquad() {
        this((byte)0, (byte)0);
    }

    private void init() {
        setBorderFreq((short)20000, (short)20);
        setBorderQ(10.0f, 0.1f);
        setBorderDbVolume(12.0f, -36.0f);

        freq = 100;
        q = 1.41f;
        dbVolume = 0.0f;
        update();
    }

    @NonNull
    @Override
    public ParamBiquad clone() throws CloneNotSupportedException{
        return (ParamBiquad) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        ParamBiquad pb = (ParamBiquad) o;
        return (freq == pb.freq) &&
                (FloatUtility.isFloatDiffLessThan(q, pb.q, 0.02f) &&
                        (FloatUtility.isFloatDiffLessThan(dbVolume, pb.dbVolume, 0.02f)) );
    }

    //setters getters
    @Override
    public void setFreq(short freq) {
        if (freq < minFreq) freq = minFreq;
        if (freq > maxFreq) freq = maxFreq;

        this.freq = freq;
        update();
    }

    @Override
    public short getFreq() {
        return freq;
    }

    public void setFreqPercent(float percent) {
        setFreq((short)Math.pow(10, percent * (Math.log10(maxFreq) - Math.log10(minFreq)) + Math.log10(minFreq)));
    }
    public float getFreqPercent() {
        return (float)((Math.log10(freq) - Math.log10(minFreq)) / (Math.log10(maxFreq) - Math.log10(minFreq)));
    }

    public void setQ(float q) {
        if (q < minQ) q = minQ;
        if (q > maxQ) q = maxQ;

        this.q = q;
        update();
    }
    public float getQ() {
        return q;
    }

    public void setDbVolume(float dbVolume) {
        if (dbVolume < minDbVolume) dbVolume = minDbVolume;
        if (dbVolume > maxDbVolume) dbVolume = maxDbVolume;

        this.dbVolume = dbVolume;
        update();
    }
    public float getDbVolume() {
        return dbVolume;
    }


    //border setter function
    public void setBorderFreq(short maxFreq, short minFreq) {
        this.maxFreq = maxFreq;
        this.minFreq = minFreq;
    }
    public void setBorderQ(float maxQ, float minQ) {
        this.maxQ = maxQ;
        this.minQ = minQ;
    }
    public void setBorderDbVolume(float maxDbVolume, float minDbVolume) {
        this.maxDbVolume = maxDbVolume;
        this.minDbVolume = minDbVolume;
    }

    @Override
    public void setCoefs(float b0, float b1, float b2, float a1, float a2) {
        super.setCoefs(b0, b1, b2, a1, a2);

        float arg = a1 / (b0 + b2);
        if ((arg > 1.0f) || (arg < -1.0f)) return;

        float w0 = (float)Math.acos(arg);
        freq = (short)Math.round(w0 * FS / (2 * Math.PI));

        arg = (float)((b0 * 2 * Math.cos(w0) - a1) / (2 * Math.cos(w0) - a1));
        if (arg < 0.0) return;

        double ampl = Math.sqrt(arg);
        dbVolume = (float)(40 * Math.log10(ampl));

        double alpha = (2 * Math.cos(w0) / a1 - 1) * ampl;
        q = (float)(Math.sin(w0) / (2 * alpha));
    }

    private void update() {
        float w0 = 2 * (float)Math.PI * freq / FS;
        float ampl, alpha, a0;

        float s = (float)Math.sin(w0), c = (float)Math.cos(w0);

        ampl = (float)Math.pow(10, dbVolume / 40);
        alpha = s / (2 * q);
        a0 =  1 + alpha / ampl;
        a1 =  2 * c / a0;
        a2 =  (1 - alpha / ampl) / (-a0);
        b0 =  (1 + alpha * ampl) / a0;
        b1 =  (2 * c) / (-a0);
        b2 =  (1 - alpha * ampl) / a0;
    }

    @Override
    public void sendToPeripheral(boolean response) {
        ByteBuffer b = ByteBuffer.allocate(14).order(ByteOrder.LITTLE_ENDIAN);
        b.put(addr);
        b.put(bindAddr);
        b.put(Order.BIQUAD_ORDER_2);
        b.put(isEnabled() ? Type.BIQUAD_PARAMETRIC : Type.BIQUAD_OFF);

        b.putShort(freq);
        b.putFloat(q);
        b.putFloat(dbVolume); // volume = 0db

        HiFiToyControl.getInstance().sendDataToDsp(b, response);
    }

    @NonNull
    @Override
    public String toString() {
        if (isEnabled()) {
            return String.format(Locale.getDefault(),
                    "%dHz Q:%.2f dB:%.1f", freq, q, dbVolume);
        }
        return "Biquad disabled.";
    }

}
