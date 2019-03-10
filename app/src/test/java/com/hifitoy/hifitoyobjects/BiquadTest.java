package com.hifitoy.hifitoyobjects;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static org.junit.Assert.*;

public class BiquadTest {

    @Test
    public void testGetBinary() {
        Biquad biquad = new Biquad((byte)0x51, (byte)0x52);

        System.out.println(String.format(Locale.getDefault(), "%d", biquad.getBinary().length));
    }

    @Test
    public void testClone() {
        Biquad b = new Biquad((byte)0x51, (byte)0x52);
        System.out.println(String.format(Locale.getDefault(), "%d", b.getParams().getFreq()));

        try {
            Biquad b1 = b.clone();
            b1.setEnabled(false);
            assertTrue(b.isEnabled() != b1.isEnabled());

            b1.getParams().setFreq((short)150);
            System.out.println(String.format(Locale.getDefault(), "%d %d",
                                        b.getParams().getFreq(), b1.getParams().getFreq()));
            assertTrue(b.getParams().getFreq() != b1.getParams().getFreq());

        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }

    }
}