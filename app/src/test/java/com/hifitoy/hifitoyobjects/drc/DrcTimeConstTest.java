package com.hifitoy.hifitoyobjects.drc;

import com.hifitoy.hifitoyobjects.basstreble.BassTreble;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static com.hifitoy.hifitoyobjects.drc.DrcChannel.DRC_CH_1_7;
import static org.junit.Assert.*;

public class DrcTimeConstTest {
    private DrcTimeConst dt0;
    private DrcTimeConst dt1;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        dt0 = new DrcTimeConst(DRC_CH_1_7, 0.1f, 10.0f, 100.0f);
        System.out.println(String.format(Locale.getDefault(), "%f", dt0.getEnergyMS()));

        try {
            dt1 = dt0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testClone() {
        System.out.println("testClone");

        dt1.setEnergyMS(1.5f);
        System.out.println(String.format(Locale.getDefault(), "%f %f", dt0.getEnergyMS(), dt1.getEnergyMS()));

        assertTrue(Float.compare(dt0.getEnergyMS(), dt0.getEnergyMS()) == 0);
        assertTrue(Float.compare(dt1.getEnergyMS(), dt1.getEnergyMS()) == 0);
        assertTrue(Float.compare(dt0.getEnergyMS(), dt1.getEnergyMS()) != 0);
    }

    @Test
    public void testEqual() {
        System.out.println("testEqual");

        dt1.setEnergyMS(1.15f);
        assertEquals(dt0, dt0);
        assertEquals(dt1, dt1);
        assertNotEquals(dt0, dt1);

        dt1.setEnergyMS(0.11f);
        assertEquals(dt0, dt1);

    }


    @Test
    public void testImport() {
        dt0.setEnergyMS(1.0f);
        assertNotEquals(dt0, dt1);

        if (!dt1.importFromDataBufs(dt0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(dt0, dt1);

        /*dt0.setEnergyMS(1000.0f);
        assertNotEquals(dt0, dt1);

        if (!dt1.importFromDataBufs(dt0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(dt0, dt1);*/

    }


}