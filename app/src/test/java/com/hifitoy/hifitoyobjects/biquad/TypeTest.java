/*
 *   TYpeTest.java
 *
 *   Created by Artem Khlyupin on 26/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;

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

                    Biquad b = new Biquad(pb);
                    if (!b.importFromDataBufs(pb.getDataBufs())) fail("pb import");
                    assertEquals(Type.getType(b), Type.BIQUAD_PARAMETRIC);
                }
            }
        }

        HighpassBiquad hp = new HighpassBiquad((byte)2);
        for (short f = 20; f < 20000; f += 10) {
            for (float q = 0.1f; q < 10.0f; q += 0.5f) {
                hp.setFreq(f);
                hp.setQ(q);

                assertEquals(Type.getType(hp), Type.BIQUAD_HIGHPASS);

                Biquad b = new Biquad(hp);
                if (!b.importFromDataBufs(hp.getDataBufs())) fail("hp import");
                assertEquals(String.format("%d %f", f, q), Type.getType(b), Type.BIQUAD_HIGHPASS);
            }
        }

        LowpassBiquad lp = new LowpassBiquad((byte)2);
        for (short f = 20; f < 20000; f += 10) {
            for (float q = 0.1f; q < 10.0f; q += 0.5f) {
                lp.setFreq(f);
                lp.setQ(q);

                assertEquals(Type.getType(lp), Type.BIQUAD_LOWPASS);

                Biquad b = new Biquad(lp);
                if (!b.importFromDataBufs(lp.getDataBufs())) fail("lp import");
                assertEquals(String.format("%d %f", f, q), Type.getType(b), Type.BIQUAD_LOWPASS);
            }
        }


        BandpassBiquad bp = new BandpassBiquad((byte)4);
        for (short f = 20; f < 20000; f += 10) {
            for (float q = 0.1f; q < 10.0f; q += 0.5f) {
                bp.setFreq(f);
                bp.setQ(q);

                assertEquals(Type.getType(bp), Type.BIQUAD_BANDPASS);

                Biquad b = new Biquad(bp);
                if (!b.importFromDataBufs(bp.getDataBufs())) fail("bp import");
                assertEquals(String.format("%d %f", f, q), Type.getType(b), Type.BIQUAD_BANDPASS);
            }
        }

        AllpassBiquad ap = new AllpassBiquad((byte)5);
        for (short f = 20; f < 20000; f += 10) {
            for (float q = 0.1f; q < 10.0f; q += 0.5f) {
                ap.setFreq(f);
                ap.setQ(q);

                assertEquals(Type.getType(ap), Type.BIQUAD_ALLPASS);

                Biquad b = new Biquad(ap);
                if (!b.importFromDataBufs(ap.getDataBufs())) fail("ap import");
                assertEquals(String.format("%d %f", f, q), Type.getType(b), Type.BIQUAD_ALLPASS);
            }
        }

        Biquad b = new Biquad((byte)6);
        b.setCoefs(1, 2, 3, 4, 5);
        assertEquals(Type.getType(b), Type.BIQUAD_USER);

        Biquad b1 = new Biquad(b);
        if (!b1.importFromDataBufs(b.getDataBufs())) fail("ap import");
        assertEquals("User biquad import error.", Type.getType(b), Type.BIQUAD_USER);
    }

    @Test
    public void testHighpassType() {
        Biquad b = new Biquad();
        HighpassBiquad hp = new HighpassBiquad(b.getAddress(), b.getBindAddr());
        hp.setFreq((short)20);


        assertEquals(Type.getType(hp), Type.BIQUAD_HIGHPASS);
    }

    @Test
    public void testLowpassType() {
        LowpassBiquad lp = new LowpassBiquad();
        lp.setFreq((short)2500);
        lp.setQ(0.71f);

        Biquad b = new Biquad();
        b.importFromDataBufs(lp.getDataBufs());

        byte type = Type.getType(b);

        assertEquals(type, Type.BIQUAD_LOWPASS);
    }


}