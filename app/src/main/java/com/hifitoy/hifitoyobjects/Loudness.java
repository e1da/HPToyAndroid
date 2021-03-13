/*
 *   Loudness.java
 *
 *   Created by Artem Khlyupin on 12/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

import android.util.Log;

import com.hifitoy.ble.BlePacket;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.FloatUtility;
import com.hifitoy.hifitoynumbers.Number523;
import com.hifitoy.hifitoyobjects.biquad.BandpassBiquad;
import com.hifitoy.tas5558.TAS5558;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class Loudness implements HiFiToyObject, Cloneable, Serializable {
    private static final String TAG = "HiFiToy";

    private BandpassBiquad biquad;

    private float LG;
    private float LO;
    private float gain; // 0..1 off/on
    private float offset;

    public Loudness() {
        LG      = -0.5f;
        LO      = 0.0f;
        gain    = 0.0f;
        offset  = 0.0f;

        biquad = new BandpassBiquad(TAS5558.LOUDNESS_BIQUAD_REG);
        biquad.setBorderFreq((short)200, (short)30);
        biquad.setFreq((short)60);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loudness loudness = (Loudness) o;
        return FloatUtility.isFloatDiffLessThan(loudness.LG, LG, 0.02f) &&
                FloatUtility.isFloatDiffLessThan(loudness.LO, LO, 0.02f) &&
                FloatUtility.isFloatDiffLessThan(loudness.gain, gain, 0.02f) &&
                FloatUtility.isFloatDiffLessThan(loudness.offset, offset, 0.02f) &&
                Objects.equals(biquad, loudness.biquad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(biquad);
    }

    @Override
    public Loudness clone() throws CloneNotSupportedException{
        Loudness l = (Loudness) super.clone();
        l.biquad = biquad.clone();
        return l;
    }

    //setters / getters
    public void setGain(float gain) {
        if (gain < 0.0f) gain = 0.0f;
        if (gain > 2.0f) gain = 2.0f;

        this.gain = gain;
    }
    public float getGain() {
        return gain;
    }
    public void setFreq(short freq){
        biquad.setFreq(freq);
    }
    public short getFreq() {
        return biquad.getFreq();
    }
    public BandpassBiquad getBiquad() {
        return biquad;
    }

    @Override
    public byte getAddress() {
        return TAS5558.LOUDNESS_LOG2_GAIN_REG;
    }

    public String getFreqInfo() {
        return String.format(Locale.getDefault(), "%dHz", getFreq());
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%d%%", (int)(gain * 100));
    }

    public void sendFreqToPeripheral(boolean response) {
        biquad.sendToPeripheral(response);
    }

    @Override
    public void sendToPeripheral(boolean response) {
        BlePacket p = new BlePacket(getMainDataBuf().getBinary(), 20, response);
        HiFiToyControl.getInstance().sendDataToDsp(p);
    }

    private HiFiToyDataBuf getMainDataBuf() {
        ByteBuffer b = ByteBuffer.allocate(16);
        b.put(Number523.get523BigEnd(LG));
        b.put(Number523.get523BigEnd(LO));
        b.put(Number523.get523BigEnd(gain));
        b.put(Number523.get523BigEnd(offset));

        return new HiFiToyDataBuf(getAddress(), b);
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        List<HiFiToyDataBuf> data = biquad.getDataBufs();
        data.add(getMainDataBuf());

        return data;
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        if (!biquad.importFromDataBufs(dataBufs)) return false;

        for (int i = 0; i < dataBufs.size(); i++) {
            HiFiToyDataBuf buf = dataBufs.get(i);

            if ((buf.getAddr() == getAddress()) && (buf.getLength() == 16)) {
                ByteBuffer b = buf.getData().order(ByteOrder.BIG_ENDIAN);

                LG = Number523.toFloat(BinaryOperation.copyOfRange(b, 0, 4));
                LO = Number523.toFloat(BinaryOperation.copyOfRange(b, 4, 8));
                gain = Number523.toFloat(BinaryOperation.copyOfRange(b, 8, 12));
                offset = Number523.toFloat(BinaryOperation.copyOfRange(b, 12, 16));

                Log.d(TAG, "Loudness import success.");
                return true;
            }
        }

        return false;
    }

}
