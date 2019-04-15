/*
 *   BlePacketQueue.java
 *
 *   Created by Artem Khlyupin on 13/11/2018.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.ble;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedList;

public class BlePacketQueue extends LinkedList<BlePacket> {
    @Override
    public boolean add(BlePacket packet) {
        if (packet.getResponse()) {
            super.add(packet);
        } else {
            boolean addStatus = false;

            //>0 is true. >=0 is not true, because wecan loose last packet
            for (int i = size() - 1; i > 0; i--) {
                BlePacket p = get(i);

                if (!p.getResponse()) {
                    set(i, packet);
                    addStatus = true;
                    break;
                }
            }

            if (!addStatus) super.add(packet);
        }
        return true;
    }
}
