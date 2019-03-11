package com.hifitoy.hifitoyobjects;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FiltersTest {
    Filters f0;
    Filters f1;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        f0 = new Filters();

        try {
            f1 = f0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testClone() {
        System.out.println("testClone");

        f1.getBiquad((byte)0).getParams().setFreq((short)150);
        assertTrue(f0.getBiquad((byte)0).getParams().getFreq() != f1.getBiquad((byte)0).getParams().getFreq());

    }

    @Test
    public void testEqual() {
        System.out.println("testEqual");

        f1.getBiquad((byte)0).getParams().setFreq((short)150);
        assertEquals(f0, f0);
        assertEquals(f1, f1);
        assertNotEquals(f0, f1);

    }
}