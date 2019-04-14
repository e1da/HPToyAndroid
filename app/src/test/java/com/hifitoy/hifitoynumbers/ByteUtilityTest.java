package com.hifitoy.hifitoynumbers;

import org.junit.Test;

import static org.junit.Assert.*;

public class ByteUtilityTest {
    @Test
    public void testToString() {
        assertEquals(ByteUtility.toString((byte)100), "100");
        assertEquals(ByteUtility.toString((byte)127), "127");
        assertEquals(ByteUtility.toString((byte)128), "128");
        assertEquals(ByteUtility.toString((byte)255), "255");
    }

    @Test
    public void testParse() {
        assertEquals(ByteUtility.parse("100"), (byte)100);
        assertEquals(ByteUtility.parse("127"), (byte)127);
        assertEquals(ByteUtility.parse("128"), (byte)-128);
        assertEquals(ByteUtility.parse("255"), (byte)-1);
    }

}