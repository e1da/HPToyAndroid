package com.hifitoy.hifitoyobjects.filter;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class DFilterTest {
    private DFilter df0;
    private DFilter df1;

    @Before
    public void setUp() {

        df0 = new DFilter();
        try {
            df1 = df0.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println(e.toString());
        }

    }

    @Test
    public void testCloneAndEquals() {
        LowpassFilter lpf = new LowpassFilter(df1.getFilterCh0());
        lpf.upOrder();
        lpf.setFreq((short)2500);
        System.out.println(String.format(Locale.getDefault(), "%d", lpf.getFreq()));

        try {
            assertEquals(df0, df0.clone());
            assertEquals(df1, df1);
            assertNotEquals(df0, df1);
        } catch (CloneNotSupportedException e) {
            System.out.println(e.toString());
        }

    }

    @Test
    public void testImport() {
        LowpassFilter lpf = new LowpassFilter(df1.getFilterCh0());
        lpf.upOrder();
        lpf.setFreq((short)2500);

        if (!df1.importFromDataBufs(df0.getDataBufs())) {
            fail("Import fail");
        }

        assertEquals(df0, df1);

    }

    @Test
    public void testStereo() {
        df0.bindChannels(false);
        assertNotEquals(df0, df1);

        df0.bindChannels(true);
        assertEquals(df0, df1);


    }


}