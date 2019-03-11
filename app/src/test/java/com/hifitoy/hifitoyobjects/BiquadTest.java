package com.hifitoy.hifitoyobjects;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static org.junit.Assert.*;

public class BiquadTest {
    Biquad b0;
    Biquad b1;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        b0 = new Biquad((byte)0x51, (byte)0x52);
        System.out.println(String.format(Locale.getDefault(), "%d", b0.getParams().getFreq()));

        try {
            b1 = b0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testGetBinary() {
        System.out.println("testGetBinary");
        System.out.println(String.format(Locale.getDefault(), "%d", b0.getBinary().length));
    }

    @Test
    public void testClone() {
        System.out.println("testClone");

        b1.setEnabled(false);
        assertTrue(b0.isEnabled() != b1.isEnabled());

        b1.getParams().setFreq((short)150);
        System.out.println(String.format(Locale.getDefault(), "%d %d",
                b0.getParams().getFreq(), b1.getParams().getFreq()));
        assertTrue(b0.getParams().getFreq() != b1.getParams().getFreq());

    }

    @Test
    public void testEqual() {
        b1.getParams().setFreq((short)150);
        assertEquals(b0, b0);
        assertEquals(b1, b1);
        assertNotEquals(b0, b1);

    }
}