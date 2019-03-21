package com.hifitoy.hifitoydevice;


import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.HiFiToyObject;
import com.hifitoy.hifitoyobjects.Volume;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;


public class HiFiToyPresetTest {
    private HiFiToyPreset p0;
    private HiFiToyPreset p1;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        p0 = new HiFiToyPreset();
        System.out.println(String.format(Locale.getDefault(), "%f", p0.getVolume().getDb()));

        try {
            p1 = p0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testClone() {
        System.out.println("testClone");

        p1.getVolume().setDb(-1.0f);
        System.out.println(String.format(Locale.getDefault(), "%f %f",
                                    p0.getVolume().getDb(), p1.getVolume().getDb()));

        assertTrue(Float.compare(p0.getVolume().getDb(), p0.getVolume().getDb()) == 0);
        assertTrue(Float.compare(p1.getVolume().getDb(), p1.getVolume().getDb()) == 0);
        assertTrue(Float.compare(p0.getVolume().getDb(), p1.getVolume().getDb()) != 0);
    }

    @Test
    public void testEqual() {
        System.out.println("testEqual");

        p1.getVolume().setDb(-1.0f);
        assertEquals(p0, p0);
        assertEquals(p1, p1);
        assertNotEquals(p0, p1);

        p1.getVolume().setDb(0.0f);
        assertEquals(p0, p1);
    }
    @Test
    public void testImport() {
        p0.getVolume().setDb(-1.0f);
        assertNotEquals(p0, p1);

        if (!p1.importFromDataBufs(p0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(p0, p1);

    }
}