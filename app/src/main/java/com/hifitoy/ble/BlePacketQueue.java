/*
 *   BlePacketQueue.java
 *
 *   Created by Artem Khlyupin on 13/11/2018.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.ble;

import android.util.Log;
import java.util.LinkedList;

public class BlePacketQueue extends LinkedList<BlePacket> {
    private static String TAG = "HiFiToy";
    @Override
    public boolean add(BlePacket packet) {
        if (packet.getResponse()) {
            super.add(packet);
        } else {
            boolean addStatus = false;

            try {
                //>0 is true. >=0 is not true, because wecan loose last packet
                for (int i = size() - 1; i > 0; i--) {
                    BlePacket p = get(i);

                    //check response and addr equal
                    if ((!p.getResponse()) && (packet.getData()[0] == p.getData()[0])) {
                        set(i, packet);
                        addStatus = true;
                        break;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG, "IndexOutOfBoundsException in BlePacket/add().");
            }

            if (!addStatus) super.add(packet);
        }
        return true;
    }
}
