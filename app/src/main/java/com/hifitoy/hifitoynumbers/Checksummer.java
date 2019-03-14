/*
 *   Checksummer.java
 *
 *   Created by Artem Khlyupin on 13/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoynumbers;

public class Checksummer {
    public static short calc(byte[] data) {
        byte sum = 0;
        byte fibonacci = 0;

        for (int i = 0; i < data.length; i++) {
            sum += data[i];
            fibonacci += sum;
        }

        short checkSum = (short)(sum & 0xFF);
        checkSum |= (short)((fibonacci << 8) & 0xFF00);
        return checkSum;
    }
}
