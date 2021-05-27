/*
 *   RegRequest.java
 *
 *   Created by Artem Khlyupin on 27/05/21
 *   Copyright © 2021 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.tas5558;

public class RegRequest {
    private final byte addr;
    private final byte from;
    private final byte to;

    public RegRequest(byte addr, byte from, byte to) {
        this.addr = addr;
        this.from = from;
        this.to = to;
    }

    public RegRequest(byte[] bin) throws Exception {
        if (bin.length < 3) {
            throw new Exception("DspRegRequest init error.");
        }
        addr = bin[0];
        from = bin[1];
        to = bin[2];
    }

    public byte[] getBinary() {
        return new byte[]{addr, from, to};
    }

    public byte getAddr() {
        return addr;
    }
    public byte getFrom() {
        return from;
    }
    public byte getTo() {
        return to;
    }
}
