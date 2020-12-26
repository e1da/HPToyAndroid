/*
 *   PassBiquad.java
 *
 *   Created by Artem Khlyupin on 24/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;

import android.support.annotation.NonNull;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.FloatUtility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public abstract class PassBiquad extends Biquad implements IFreq{
    protected short freq;
    protected float q;

    private short maxFreq   = 20000;
    private short minFreq   = 20;
    private float maxQ      = 10;
    private float minQ      = 0.1f;

    public PassBiquad(Biquad b) {
        super(b);
    }

    public PassBiquad(byte addr, byte bindAddr) {
        super(addr, bindAddr);

        setEnabled(true);

        freq = 100;
        q = 1.41f;
        update();
    }

    public PassBiquad(byte addr) {
        this(addr, (byte)0);
    }

    public PassBiquad() {
        this((byte)0, (byte)0);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        PassBiquad pb = (PassBiquad) o;
        return (freq == pb.freq) &&
                (FloatUtility.isFloatDiffLessThan(q, pb.q, 0.02f));
    }


    //setters getters
    public abstract byte getType();
    public byte getOrder() {
        return Order.BIQUAD_ORDER_2;
    }

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

    //border setter function
    public void setBorderFreq(short maxFreq, short minFreq) {
        this.maxFreq = maxFreq;
        this.minFreq = minFreq;
    }
    public void setBorderQ(float maxQ, float minQ) {
        this.maxQ = maxQ;
        this.minQ = minQ;
    }

    protected abstract void update();

    @Override
    public void sendToPeripheral(boolean response) {
        ByteBuffer b = ByteBuffer.allocate(14).order(ByteOrder.LITTLE_ENDIAN);
        b.put(addr);
        b.put(bindAddr);
        b.put(getOrder());
        b.put(isEnabled() ? getType() : Type.BIQUAD_OFF);

        b.putShort(freq);
        b.putFloat(1.41f);
        b.putFloat(0.0f); // volume = 0db

        HiFiToyControl.getInstance().sendDataToDsp(b, response);
    }


    @NonNull
    @Override
    public String toString() {
        if (isEnabled()) {
            return freq + "Hz";
        }
        return "Biquad disabled.";
    }
}
