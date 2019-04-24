package com.hifitoy.ble;

import org.junit.Test;

import static org.junit.Assert.*;

public class BlePacketQueueTest {

    @Test
    public void testAddPacket() {
        BlePacketQueue queue = new BlePacketQueue();

        queue.add(new BlePacket(new byte[]{0}, true));
        queue.add(new BlePacket(new byte[]{0}, true));
        queue.add(new BlePacket(new byte[]{0}, true));
        assertTrue(queue.size() == 3);

        queue.clear();
        assertTrue(queue.size() == 0);

        //with response == false, first packet not replace
        queue.add(new BlePacket(new byte[]{0}, false));
        queue.add(new BlePacket(new byte[]{0}, false));
        queue.add(new BlePacket(new byte[]{0}, false));
        assertTrue(queue.size() == 2);

        queue.clear();

        //with response == false, first packet not replace
        queue.add(new BlePacket(new byte[]{0}, false));
        queue.add(new BlePacket(new byte[]{1}, false));
        queue.add(new BlePacket(new byte[]{2}, false));
        assertTrue(queue.size() == 3);

    }

}