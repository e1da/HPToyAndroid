/*
 *   BlePacketQueue.java
 *
 *   Created by Artem Khlyupin on 13/11/2018.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.ble;

import java.util.ArrayDeque;
import java.util.Iterator;

public class BlePacketQueue extends ArrayDeque<BlePacket> {
    @Override
    public boolean add(BlePacket packet) {
        if (packet.getResponse()) {
            super.add(packet);
        } else {
            boolean addStatus = false;

            Iterator<BlePacket> iter = descendingIterator();

            do {
                BlePacket p = iter.next();
                if (!p.getResponse()) {
                    p = packet;
                    addStatus = true;
                    break;
                }

            } while (iter.hasNext());


            if (!addStatus) super.add(packet);
        }
        return true;
    }
}
