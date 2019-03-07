package com.hifitoy.hifitoyobjects;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class BiquadTest {

    @Test
    public void testGetBinary() {
        Biquad biquad = new Biquad((byte)0x51, (byte)0x52);

        System.out.println(String.format(Locale.getDefault(), "%d", biquad.getBinary().length));
    }
}