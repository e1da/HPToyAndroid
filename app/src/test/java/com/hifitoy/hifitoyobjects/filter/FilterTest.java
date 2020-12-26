package com.hifitoy.hifitoyobjects.filter;


import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class FilterTest {
    private Filter f0;
    private Filter f1;

    @Before
    public void setUp() {

        f0 = new Filter((byte)2, (byte)9);
        try {
            f1 = f0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println(e.toString());
        }

    }

    @Test
    public void testCloneAndEquals() {
        LowpassFilter lpf = new LowpassFilter(f1);
        lpf.upOrder();
        lpf.setFreq((short)2500);
        System.out.println(String.format(Locale.getDefault(), "%d", lpf.getFreq()));

        try {
            assertEquals(f0, f0.clone());
            assertEquals(f1, f1);
            assertNotEquals(f0, f1);
        } catch (CloneNotSupportedException e) {
        System.out.println(e.toString());
    }

    }

    @Test
    public void testImport() {
        LowpassFilter lpf = new LowpassFilter(f1);
        lpf.upOrder();
        lpf.setFreq((short)2500);

        if (!f1.importFromDataBufs(f0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(f0, f1);

    }
}