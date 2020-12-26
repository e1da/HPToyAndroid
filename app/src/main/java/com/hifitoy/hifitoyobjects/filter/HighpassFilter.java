/*
 *   HPFilter.java
 *
 *   Created by Artem Khlyupin on 21/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.filter;

import com.hifitoy.hifitoyobjects.biquad.Biquad;
import com.hifitoy.hifitoyobjects.biquad.HighpassBiquad;

import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.filter.Order.FILTER_ORDER_0;

public class HighpassFilter extends PassFilter {

    public HighpassFilter(Filter f) {
        super(f);
    }

    @Override
    protected short getDefaultFreq() {
        return 20;
    }
    @Override
    protected byte getBiquadType() {
        return BIQUAD_HIGHPASS;
    }

    @Override
    protected Biquad createPassBiquad(byte addr, byte bindAddr) {
        return new HighpassBiquad(addr, bindAddr);
    }

    @Override
    public void setFreq(short freq) {
        //check freq
        LowpassFilter lpf = new LowpassFilter(f);
        short lpfFreq = lpf.getFreq();
        if ((lpfFreq != -1) && (freq > lpfFreq)) freq = lpfFreq;

        super.setFreq(freq);
    }

    @Override
    public void downOrder() {
        super.downOrder();
        if (getOrder() == FILTER_ORDER_0) {
            f.setActiveNullHP(true);
        }
    }

}
