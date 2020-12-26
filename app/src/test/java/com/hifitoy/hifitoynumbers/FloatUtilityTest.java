package com.hifitoy.hifitoynumbers;

import org.junit.Test;

import static org.junit.Assert.*;

public class FloatUtilityTest {

    @Test
    public void testIsFloatEqualWithAccuracy() {
        assertFalse(FloatUtility.isFloatEqualWithAccuracy(0.0f, 0.000001f, 16));
        assertFalse(FloatUtility.isFloatEqualWithAccuracy(0.000001f, 0.0f, 16));
        assertTrue(FloatUtility.isFloatEqualWithAccuracy(0.1234567f, 0.1234567f, 16));
        assertTrue(FloatUtility.isFloatEqualWithAccuracy(1.0f, 1.00000095367f, 16));
        assertFalse(FloatUtility.isFloatEqualWithAccuracy(1.0f, 1.0000038147f, 16));
        assertFalse(FloatUtility.isFloatEqualWithAccuracy(2.0f, -4.0f, 16));
        assertFalse(FloatUtility.isFloatEqualWithAccuracy(2.0f, 4.0f, 16));
    }

    @Test
    public void testIsFloatNull() {
        assertTrue(FloatUtility.isFloatNull(0.0f));
        assertFalse(FloatUtility.isFloatNull(0.000000000000000000000000000000000000000001f));
        assertFalse(FloatUtility.isFloatNull(-0.0000000000000000000000000000000000000000001f));

        assertFalse(FloatUtility.isFloatNull(-999999999999999999.0f));
    }

}