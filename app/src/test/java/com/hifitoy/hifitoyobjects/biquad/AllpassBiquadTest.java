package com.hifitoy.hifitoyobjects.biquad;

import org.junit.Test;

import static org.junit.Assert.*;

public class AllpassBiquadTest {

    @Test
    public void testCloneAndEquals() {
        AllpassBiquad ap = new AllpassBiquad((byte)4, (byte)6);
        ap.setFreq((short)250);
        ap.setQ(2.54f);

        try {
            AllpassBiquad ap1 = ap.clone();
            assertEquals(ap1, ap);

        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }

    }
}