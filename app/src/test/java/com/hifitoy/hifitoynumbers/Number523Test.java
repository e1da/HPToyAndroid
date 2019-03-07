package com.hifitoy.hifitoynumbers;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Locale;

import static org.junit.Assert.*;

public class Number523Test {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testCheckRange() {
        assertEquals(15.9999f, Number523.checkRange(23.0f), 0.0001f);
        assertTrue(16.0f > Number523.checkRange(16.0f));

        assertEquals(15.9f, Number523.checkRange(15.9f), 0.0001f);
        assertEquals(10.0f, Number523.checkRange(10.0f), 0.0001f);
        assertEquals(-10.0f, Number523.checkRange(-10.0f), 0.0001f);

        assertEquals(-16.0f, Number523.checkRange(-20.0f), 0.0001f);
    }

    @Test
    public void testGet523LittleEnd() {
        byte[] b = Number523.get523LittleEnd(16.0f).array();
        //System.out.println(String.format(Locale.getDefault(), "%x %x %x %x", b[0], b[1], b[2], b[3]));

        byte[] bb = new byte[3];
        System.arraycopy(b, 1, bb, 0, 3);
        byte[] a = {(byte)0xff, (byte)0xff, (byte)0x07};

        assertArrayEquals(a, bb);
    }

    @Test
    public void testGet523BigEnd() {
        byte[] b = Number523.get523BigEnd(16.0f).array();

        byte[] bb = new byte[3];
        System.arraycopy(b, 0, bb, 0, 3);
        byte[] a = {(byte)0x07, (byte)0xff, (byte)0xff};

        assertArrayEquals(a, bb);
    }

    @Test
    public void testToFloat() {
        ByteBuffer buf = Number523.get523LittleEnd(16.0f);
        assertTrue(16.0f > Number523.toFloat(buf));
        buf = Number523.get523BigEnd(16.0f);
        assertTrue(16.0f > Number523.toFloat(buf));

        buf = Number523.get523LittleEnd(15.0f);
        assertEquals(15.0f, Number523.toFloat(buf), 0.0001f);
        buf = Number523.get523BigEnd(15.0f);
        assertEquals(15.0f, Number523.toFloat(buf), 0.0001f);

        buf = Number523.get523LittleEnd(-16.0f);
        assertEquals(-16.0f, Number523.toFloat(buf), 0.0001f);
        buf = Number523.get523BigEnd(-16.0f);
        assertEquals(-16.0f, Number523.toFloat(buf), 0.0001f);

        buf = Number523.get523LittleEnd(-20.0f);
        assertTrue(-20.0f < Number523.toFloat(buf));
        buf = Number523.get523BigEnd(-20.0f);
        assertTrue(-20.0f < Number523.toFloat(buf));
    }

}