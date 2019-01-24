/*
 *   BiquadType.java
 *
 *   Created by Artem Khlyupin on 24/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoyobjects;

public class BiquadType {
    public final static byte BIQUAD_LOWPASS       = 2;
    public final static byte BIQUAD_HIGHPASS      = 1;
    public final static byte BIQUAD_OFF           = 0;
    public final static byte BIQUAD_PARAMETRIC    = 3;
    public final static byte BIQUAD_ALLPASS       = 4;
    public final static byte BIQUAD_BANDPASS      = 5;
    public final static byte BIQUAD_USER          = 6;

    public static byte getDefault() {
        return BIQUAD_PARAMETRIC;
    }
}
