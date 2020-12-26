package com.hifitoy.hifitoyobjects.biquad;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class ParamBiquadTest {

    @Test
    public void testConversion() {
        ParamBiquad pb = new ParamBiquad((byte)4, (byte)6);
        pb.setFreq((short)250);
        pb.setQ(2.54f);
        pb.setDbVolume(6.0f);

        Biquad b = new Biquad(pb);
        assertNotEquals(pb, b);

        ParamBiquad pb1 = new ParamBiquad(b);
        assertEquals(pb, pb1);
    }

}