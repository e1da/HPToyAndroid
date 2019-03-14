package com.hifitoy.hifitoynumbers;

import org.junit.Test;

import static org.junit.Assert.*;

public class ChecksummerTest {

    @Test
    public void testCalc() {
        assertTrue(Checksummer.calc(new byte[]{0x01, 0x01}) == (short)0x0302);
        assertTrue(Checksummer.calc(new byte[]{(byte)0x80, (byte)0x80}) == (short)0x8000);
        assertTrue(Checksummer.calc(new byte[]{(byte)0x40, (byte)0x40}) == (short)0xC080);
    }
}