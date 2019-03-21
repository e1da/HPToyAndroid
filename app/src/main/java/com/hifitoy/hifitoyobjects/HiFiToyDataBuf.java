/*
 *   HiFiToyDataBuf.java
 *
 *   Created by Artem Khlyupin on 07/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

import java.nio.ByteBuffer;

public class HiFiToyDataBuf {
    private byte addr;
    private ByteBuffer data;

    public HiFiToyDataBuf(byte addr, ByteBuffer data) {
        this.addr = addr;
        this.data = data;
    }
    public HiFiToyDataBuf(ByteBuffer b) {
        parseBinary(b);
    }


    // setters getters
    public void setAddr(byte addr) {
        this.addr = addr;
    }
    public byte getAddr() {
        return addr;
    }
    public byte getLength() {
        return (data == null) ? 0 : (byte)data.array().length;
    }
    public void setData(ByteBuffer data) {
        this.data = data;
    }
    public ByteBuffer getData() {
        return (ByteBuffer) data.position(0);
    }

    public ByteBuffer getBinary() {
        byte length = getLength();
        if (length != 0) {
            ByteBuffer b = ByteBuffer.allocate(2 + length);
            b.put(addr);
            b.put(length);
            b.put(data);

            return b;
        }
        return null;
    }

    public void parseBinary(ByteBuffer b) {
        addr = b.get();

        byte length = b.get();
        byte[] d = new byte[length];

        b.get(d, 0, length);
        data = ByteBuffer.wrap(d);
    }
}
