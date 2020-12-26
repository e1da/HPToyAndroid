/*
 *   LPFilter.java
 *
 *   Created by Artem Khlyupin on 21/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.filter;

import com.hifitoy.hifitoyobjects.biquad.Biquad;
import com.hifitoy.hifitoyobjects.biquad.LowpassBiquad;

import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.filter.Order.FILTER_ORDER_0;

public class LowpassFilter extends PassFilter {

    public LowpassFilter(Filter f) {
        super(f);
    }

    @Override
    protected short getDefaultFreq() {
        return 20000;
    }

    @Override
    protected byte getBiquadType() {
        return BIQUAD_LOWPASS;
    }

    @Override
    protected Biquad createPassBiquad(byte addr, byte bindAddr) {
        return new LowpassBiquad(addr, bindAddr);
    }

    @Override
    public void setFreq(short freq) {
        //check freq
        HighpassFilter hpf = new HighpassFilter(f);
        short hpfFreq = hpf.getFreq();
        if ((hpfFreq != -1) && (freq < hpfFreq)) freq = hpfFreq;

        super.setFreq(freq);
    }

    @Override
    public void downOrder() {
        super.downOrder();
        if (getOrder() == FILTER_ORDER_0) {
            f.setActiveNullLP(true);
        }
    }
}
