package com.hifitoy.hifitoyobjects;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class BinaryOperationTest {
    @Test
    public void testCopyOfRange() {
        byte[] data = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
        ByteBuffer b = ByteBuffer.wrap(data);
        ByteBuffer b0 = BinaryOperation.copyOfRange(b, 0, 4);
        ByteBuffer b1 = BinaryOperation.copyOfRange(b, 4, 8);

        assertEquals(b0.capacity(), 4);
        for (int i= 0; i < 4; i++)  assertEquals(b0.get(i), i);

        assertEquals(b1.capacity(), 4);
        for (int i= 4; i < 8; i++)  assertEquals(b1.get(i - 4), i);
    }

    @Test
    public void testRightBorder() {
        byte[] data = new byte[]{0, 1, 2, 3};
        ByteBuffer b = ByteBuffer.wrap(data);
        ByteBuffer bb = BinaryOperation.copyOfRange(b, 0, 16);

        assertEquals(bb.capacity(), 4);
        for (int i= 0; i < 4; i++)  assertEquals(bb.get(i), i);
    }
}