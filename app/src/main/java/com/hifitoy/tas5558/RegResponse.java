/*
 *   ReqResponse.java
 *
 *   Created by Artem Khlyupin on 27/05/21
 *   Copyright © 2021 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.tas5558;

import android.support.annotation.NonNull;

import java.util.Arrays;

public class RegResponse {
    RegRequest req;  // length = 3
    byte[] data;        // length = 17


    public RegResponse(byte[] bin) throws Exception {
        if (bin.length < 20) {
            throw new Exception("DspRegResponse init error.");
        }
        this.req = new RegRequest(bin);
        data = Arrays.copyOfRange(bin, 3, 20);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Register " + req.getAddr() +
                ": range [" + req.getFrom() + ", " + req.getTo() + "]:");

        for (int i = req.getFrom(); i < req.getTo(); i++) {
            sb.append(" ").append(data[i - req.getFrom()]);
        }

        return sb.toString();
    }
}
