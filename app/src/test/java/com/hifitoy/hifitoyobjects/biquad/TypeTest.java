/*
 *   TYpeTest.java
 *
 *   Created by Artem Khlyupin on 26/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;

import com.hifitoy.hifitoyobjects.filter.HighpassFilter;

import org.junit.Test;

import static org.junit.Assert.*;

public class TypeTest {
    @Test
    public void testTypeChecker() {
        ParamBiquad pb = new ParamBiquad((byte)1, (byte)2);
        for (short f = 20; f < 20000; f += 10) {
            for (float q = 0.1f; q < 10.0f; q += 0.5f) {
                for (float db = -36.0f; db < 12.0f; db += 1.0f) {
                    pb.setFreq(f);
                    pb.setQ(q);
                    pb.setDbVolume(db);

                    assertEquals(String.format("%d %f %f", f, q, db), Type.getType(pb), Type.BIQUAD_PARAMETRIC);
                }
            }
        }

        HighpassBiquad hp = new HighpassBiquad((byte)2);
        for (short f = 20; f < 20000; f += 10) {
            for (float q = 0.1f; q < 10.0f; q += 0.5f) {
                hp.setFreq(f);
                hp.setQ(q);

                assertEquals(Type.getType(hp), Type.BIQUAD_HIGHPASS);
            }
        }

        LowpassBiquad lp = new LowpassBiquad((byte)2);
        for (short f = 20; f < 20000; f += 10) {
            for (float q = 0.1f; q < 10.0f; q += 0.5f) {
                lp.setFreq(f);
                lp.setQ(q);

                assertEquals(Type.getType(lp), Type.BIQUAD_LOWPASS);
            }
        }


        BandpassBiquad bp = new BandpassBiquad((byte)4);
        for (short f = 20; f < 20000; f += 10) {
            for (float q = 0.1f; q < 10.0f; q += 0.5f) {
                bp.setFreq(f);
                bp.setQ(q);

                assertEquals(Type.getType(bp), Type.BIQUAD_BANDPASS);
            }
        }

        AllpassBiquad ap = new AllpassBiquad((byte)5);
        for (short f = 20; f < 20000; f += 10) {
            for (float q = 0.1f; q < 10.0f; q += 0.5f) {
                ap.setFreq(f);
                ap.setQ(q);

                assertEquals(Type.getType(ap), Type.BIQUAD_ALLPASS);
            }
        }

        Biquad b = new Biquad((byte)6);
        b.setCoefs(1, 2, 3, 4, 5);
        assertEquals(Type.getType(b), Type.BIQUAD_USER);
    }


}