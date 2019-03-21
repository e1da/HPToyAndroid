package com.hifitoy.hifitoyobjects.drc;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static com.hifitoy.hifitoyobjects.drc.DrcChannel.DRC_CH_1_7;
import static com.hifitoy.hifitoyobjects.drc.DrcCoef.POINT0_INPUT_DB;
import static com.hifitoy.hifitoyobjects.drc.DrcCoef.POINT3_INPUT_DB;
import static org.junit.Assert.*;

public class DrcCoefTest {
    private DrcCoef dc0;
    private DrcCoef dc1;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp");

        dc0 = new DrcCoef(DRC_CH_1_7,
                new DrcCoef.DrcPoint(POINT0_INPUT_DB, -120.0f),
                new DrcCoef.DrcPoint(-72.0f, -72.0f),
                new DrcCoef.DrcPoint(-24.0f, -24.0f),
                new DrcCoef.DrcPoint(POINT3_INPUT_DB, -24.0f));

        System.out.println(String.format(Locale.getDefault(), "%s", dc0.getPoint0().getInfo()));

        try {
            dc1 = dc0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Clone not supported!");
        }
    }

    @Test
    public void testClone() {
        System.out.println("testClone");

        dc1.getPoint0().setOutputDb(-60.0f);
        System.out.println(String.format(Locale.getDefault(), "%s", dc0.getPoint0().getInfo()));

        assertTrue(Float.compare(dc0.getPoint0().getOutputDb(), dc0.getPoint0().getOutputDb()) == 0);
        assertTrue(Float.compare(dc1.getPoint0().getOutputDb(), dc1.getPoint0().getOutputDb()) == 0);
        assertTrue(Float.compare(dc0.getPoint0().getOutputDb(), dc1.getPoint0().getOutputDb()) != 0);
    }

    @Test
    public void testEqual() {
        System.out.println("testEqual");

        dc1.getPoint0().setOutputDb(-60.0f);
        assertEquals(dc0, dc0);
        assertEquals(dc1, dc1);
        assertNotEquals(dc0, dc1);

        dc1.getPoint0().setOutputDb(-120.1f);
        assertEquals(dc0, dc1);

    }


    @Test
    public void testDrcParam() {
        DrcCoef.DrcParam p0 = new DrcCoef.DrcParam(new DrcCoef.DrcPoint(POINT0_INPUT_DB, -120.0f),
                                                    new DrcCoef.DrcPoint(-72.0f, -72.0f),
                                                    new DrcCoef.DrcPoint(-24.0f, -24.0f),
                                                    new DrcCoef.DrcPoint(POINT3_INPUT_DB, -24.0f));

        DrcCoef.DrcParam  p1 = new DrcCoef.DrcParam(p0.getBinary());
        assertEquals(p0, p1);

    }
    @Test
    public void testImport() {
        dc0.getPoint0().setOutputDb(-60.0f);
        assertNotEquals(dc0, dc1);

        if (!dc1.importFromDataBufs(dc0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(dc0, dc1);


    }


}