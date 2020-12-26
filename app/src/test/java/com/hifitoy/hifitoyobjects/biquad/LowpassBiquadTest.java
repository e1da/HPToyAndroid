package com.hifitoy.hifitoyobjects.biquad;

import org.junit.Test;

import static org.junit.Assert.*;

public class LowpassBiquadTest {
    @Test
    public void testCloneAndEquals() {
        LowpassBiquad lp = new LowpassBiquad((byte)4, (byte)6);
        lp.setFreq((short)250);
        lp.setQ(2.54f);

        try {
            LowpassBiquad lp1 = lp.clone();
            assertEquals(lp1.getFreq(), lp.getFreq());

        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }

    }
}