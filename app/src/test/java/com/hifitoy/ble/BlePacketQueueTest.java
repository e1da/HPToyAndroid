package com.hifitoy.ble;

import org.junit.Test;

import static org.junit.Assert.*;

public class BlePacketQueueTest {

    @Test
    public void testAddWithoutResponse() {
        BlePacketQueue queue = new BlePacketQueue();

        queue.add(new BlePacket(new byte[]{1}, false));
        queue.add(new BlePacket(new byte[]{2}, true));

        assertTrue(queue.size() == 2);

        queue.add(new BlePacket(new byte[]{3}, false));
        queue.add(new BlePacket(new byte[]{4}, false));

        assertTrue(queue.size() == 2);

        BlePacket p = queue.get(0);
        byte[] data = p.getData();
        assertTrue(data[0] == 4);

        p = queue.get(1);
        data = p.getData();
        assertTrue(data[0] == 2);

    }

}