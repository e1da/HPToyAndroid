/*
 *   Checksummer.java
 *
 *   Created by Artem Khlyupin on 13/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoynumbers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    //we have checksum(CS) value of data with originalLength
    //func recalculate CS subtracting first data[]
    public static short subtractData(short checksum, int originalLength, byte[] data) {
        ByteBuffer b = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(0, checksum);
        byte sum = b.get();
        byte fib = b.get();

        for (int i = 0; i < data.length; i++) {
            sum -= data[i];
            fib -= data[i] * (originalLength - i);
        }

        short checkSum = (short)(sum & 0xFF);
        checkSum |= (short)((fib << 8) & 0xFF00);
        return checkSum;
    }
}
