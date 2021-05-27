/*
 *   ToyDelayDataBuf.java
 *
 *   Created by Artem Khlyupin on 27/05/21
 *   Copyright © 2021 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

import java.nio.ByteBuffer;

/* example usage:
    * ToyDelayDataBuf ddb = new ToyDelayDataBuf((byte)0xFF); // delay = 255ms
    * dataBufs.add(1, ddb);
 */
public class ToyDelayDataBuf extends HiFiToyDataBuf {

    public ToyDelayDataBuf(byte delayMS) {
        super((byte)0xFF,  ByteBuffer.allocate(1).put(delayMS));
    }
}
