package com.hifitoy.hifitoyobjects;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static org.junit.Assert.*;

public class PassFilterTest {
    private PassFilter pf0;
    private PassFilter pf1;

    @Before
    public void setUp() throws Exception {
        List<Biquad> biquads = new ArrayList<Biquad>();
        biquads.add(new Biquad((byte)0x51, (byte)0x52));

        pf0 = new PassFilter(biquads, BIQUAD_HIGHPASS);
        System.out.println(String.format(Locale.getDefault(), "%d", pf0.getFreq()));

        try {
            pf1 = pf0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testClone() {
        pf1.setFreq((short)150);
        System.out.println(String.format(Locale.getDefault(), "%d %d", pf0.getFreq(), pf1.getFreq()));

        assertTrue(pf0.getFreq() != pf1.getFreq());
    }

    @Test
    public void testEqual() {
        pf1.setFreq((short)150);
        assertEquals(pf0, pf0);
        assertEquals(pf1, pf1);
        assertNotEquals(pf0, pf1);

    }
}