/*
 *   PassFilter.java
 *
 *   Created by Artem Khlyupin on 24/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.filter;

import android.support.annotation.NonNull;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.biquad.Biquad;
import com.hifitoy.hifitoyobjects.biquad.IFreq;
import com.hifitoy.hifitoyobjects.biquad.ParamBiquad;
import com.hifitoy.hifitoyobjects.biquad.PassBiquad;
import com.hifitoy.hifitoyobjects.biquad.Type;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;

import static com.hifitoy.hifitoyobjects.filter.Order.FILTER_ORDER_0;
import static com.hifitoy.hifitoyobjects.filter.Order.FILTER_ORDER_2;
import static com.hifitoy.hifitoyobjects.filter.Order.FILTER_ORDER_4;
import static com.hifitoy.hifitoyobjects.filter.Order.FILTER_ORDER_8;
import static com.hifitoy.hifitoyobjects.filter.Order.FILTER_ORDER_UNK;

public abstract class PassFilter implements IFreq {
    protected Filter f;

    public PassFilter(Filter f) {
        this.f = f;
    }


    protected abstract short getDefaultFreq();
    protected abstract byte getBiquadType();
    protected abstract Biquad createPassBiquad(byte addr, byte bindAddr);

    protected List<Biquad> getBiquads() {

        return f.getBiquads(getBiquadType());
    }

    @Override
    public void setFreq(short freq) {
        List<Biquad> biquads = f.getBiquads(getBiquadType());
        if (biquads != null) {
            for (int i = 0; i < biquads.size(); i++) {
                IFreq iFreq = (IFreq) biquads.get(i);
                iFreq.setFreq(freq);
            }
        }
    }

    @Override
    public short getFreq() {
        List<Biquad> biquads = f.getBiquads(getBiquadType());
        if ((biquads != null) && (biquads.size() > 0)) {
            IFreq iFreq = (IFreq) biquads.get(0);
            return iFreq.getFreq();
        }
        return -1;
    }

    public boolean isFull() {
        return getOrder() >= FILTER_ORDER_4;
    }

    public boolean isEmpty() {
        return getOrder() <= FILTER_ORDER_0;
    }

    public byte getOrder() {
        byte l = (byte)f.getBiquads(getBiquadType()).size();

        if (l > FILTER_ORDER_8) {
            l = FILTER_ORDER_UNK;
        }
        return l;
    }

    public boolean upOrder() {
        if (isFull()) return false;

        //get freq
        short freq = getFreq();
        if (freq == -1) freq = getDefaultFreq();

        //find free biquad
        Biquad b = f.getFreeBiquad();
        if (b == null) return false;
        byte bIndex = f.getBiquadIndex(b);

        //change type to lowpass
        b = createPassBiquad(b.getAddress(), b.getBindAddr());
        f.setBiquad(bIndex, b);

        b.setEnabled(true);
        setFreq(freq);


        //get lp biquads
        List<Biquad> biquads = getBiquads();
        //set active biquad index
        byte index = f.getBiquadIndex(biquads.get(0));
        if (index != -1) {
            f.activeBiquadIndex = index;
            f.activeNullLP = false;
            f.activeNullHP = false;
        }

        //update lp biquads with order
        update();
        return true;
    }

    public void downOrder() {
        byte order = getOrder();
        int newOrder;

        if (order == FILTER_ORDER_8) {
            newOrder = FILTER_ORDER_8;
        } else if (order == FILTER_ORDER_4) {
            newOrder = FILTER_ORDER_2;
        } else if (order == FILTER_ORDER_2) {
            newOrder = FILTER_ORDER_0;
        } else {
            return;
        }

        List<Biquad> biquads = f.getBiquads(getBiquadType());

        //replace to parametric biquad
        for (int i = newOrder; i < biquads.size(); i++) {
            Biquad b = biquads.get(i);
            byte index = f.getBiquadIndex(b);

            short freq = f.getBetterNewFreqForBiquad(b);
            if (freq == -1) freq = 100;

            boolean peqEn = f.isBiquadEnabled(Type.BIQUAD_PARAMETRIC);
            ParamBiquad pb = new ParamBiquad(b.getAddress(), b.getBindAddr());
            pb.setEnabled(peqEn);
            pb.setFreq(freq);

            f.setBiquad(index, pb);

            pb.sendToPeripheral(true);
        }

        //update lp biquads with order
        update();
    }


    protected void update() {
        List<Biquad> passBiquads = f.getBiquads(getBiquadType());
        byte order = getOrder();

        PassBiquad b;

        switch (order) {
            case FILTER_ORDER_2:
                b = (PassBiquad)passBiquads.get(0);
                b.setQ(0.71f);
                break;

            case FILTER_ORDER_4:
                b = (PassBiquad)passBiquads.get(0);
                b.setQ(0.54f);

                b = (PassBiquad)passBiquads.get(1);
                b.setQ(1.31f);
                break;

            case FILTER_ORDER_8:
                b = (PassBiquad)passBiquads.get(0);
                b.setQ(0.90f);

                b = (PassBiquad)passBiquads.get(1);
                b.setQ(2.65f);

                b = (PassBiquad)passBiquads.get(2);
                b.setQ(0.51f);

                b = (PassBiquad)passBiquads.get(3);
                b.setQ(0.60f);
                break;

            default:
        }
        sendToPeripheral(true);
    }

    /* TODO : @Override */
    public void sendToPeripheral(boolean response) {
        if (isEmpty()) return;

        ByteBuffer b = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN);

        List<Biquad> biquads = getBiquads();

        for (int i = 0; i < 4; i++) {
            if (i < getOrder()) {
                b.put(biquads.get(i).getAddress());
                b.put(biquads.get(i).getBindAddr());
            } else {
                b.put((byte)0);
                b.put((byte)0);
            }
        }
        b.put(getOrder());
        b.put(getBiquadType());
        b.putShort(getFreq());
        byte nc = 0;
        b.put(nc);

        HiFiToyControl.getInstance().sendDataToDsp(b, response);
    }


    @NonNull
    @Override
    public String toString() {
        int[] dbOnOctave = new int[]{0, 12, 24, 48};// db/oct
        int index = getOrder();
        if (index < 4) {
            return String.format(Locale.getDefault(), "%ddb/oct; Freq:%dHz", dbOnOctave[index], getFreq());
        }
        return String.format(Locale.getDefault(), "???db/oct; Freq:%dHz", getFreq());
    }
}
